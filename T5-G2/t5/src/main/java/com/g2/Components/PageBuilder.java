package com.g2.Components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import com.g2.Interfaces.ServiceManager;

@Service
public class PageBuilder {
    // Lista di componenti di pagina
    private final List<GenericLogicComponent> LogicComponents;
    private final List<GenericObjectComponent> ObjectComponents;
    /*
    * Mappa che associa codici di errore a pagine di errore
    * Così si può personalizzare il comportamento della pagina 
    * in termini di redirect 
     */
    private final Map<String, String> errorPageMap = new HashMap<>();
    // Manager dei servizi,
    private final ServiceManager serviceManager;
    // Modello per il rendering della pagina
    private final Model model_html;
    // Nome della pagina (template) da utilizzare
    private final String PageName;

    //COSTRUTTORI 
    /**
     * @param serviceManager Gestisce la chiamate REST ai vari task
     * @param PageName nome della pagina da implementare
     * @param pageComponents lista dei componenti che fanno parte della pagina
     */
    public PageBuilder(ServiceManager serviceManager, String PageName, Model model_html, List<GenericObjectComponent> ObjectComponents, List<GenericLogicComponent> LogicComponents) {
        this.serviceManager = serviceManager;
        this.ObjectComponents = ObjectComponents;
        this.LogicComponents = LogicComponents;
        this.PageName = PageName;
        this.model_html = model_html;
        setStandardErrorPage();
    }

    public PageBuilder(ServiceManager serviceManager, String PageName, Model model_html) {
        this.serviceManager = serviceManager;
        this.ObjectComponents = new ArrayList<>();
        this.LogicComponents = new ArrayList<>();
        this.PageName = PageName;
        this.model_html = model_html;
        setStandardErrorPage();
    }

    //HANDLE PAGE REQUEST 
    // Metodo per eseguire la logica di tutti i componenti
    private List<String> executeComponentsLogic() {
        // Lista per raccogliere eventuali errori
        List<String> errorCodes = new ArrayList<>(); 

        for (GenericLogicComponent Component : LogicComponents) {
            if (!Component.executeLogic()) {
                System.out.println("Logica fallita per il componente: " + Component.getClass().getSimpleName());
                errorCodes.add(Component.getErrorCode()); // Aggiunge il codice d'errore alla lista
            }
        }
        return errorCodes;
    }

    // Metodo per costruire la mappa combinata dei dati per il modello
    /* 
        Assicurati che i dati restituiti da getModel non sovrascrivano informazioni importanti.
        Se due componenti restituiscono dati con la stessa chiave, 
        l'ultimo componente che aggiorna la mappa sovrascriverà i dati precedenti.
     */
    private Map<String, Object> buildModel() {
        Map<String, Object> combinedModel = new HashMap<>();

        for (GenericObjectComponent component : ObjectComponents) {
            Map<String, Object> model = component.getModel();
            model.forEach((key, value) -> {
                if (combinedModel.put(key, value) != null) {
                    System.err.println("[PAGEBULDER] individuate chiavi duplicate: " + key);
                    // Puoi decidere se lanciare un'eccezione o gestire la duplicazione come preferisci
                }
            });
        }
        return combinedModel;
    }

    // Metodo principale flusso per una richiesta di pagina
    // Esegue la logica di ogni componente, poi elabora i dati da inserire nel template
    public String handlePageRequest() {
        String return_page_error = null;
        if (LogicComponents != null && !LogicComponents.isEmpty()) {
            // Esegui la logica di tutti i componenti
            List<String> ErrorCode = executeComponentsLogic();
            // Gestisco le situazioni d'errore
            return_page_error = ExecuteError(ErrorCode);
        }
        if (ObjectComponents != null && !ObjectComponents.isEmpty()) {
            // Costruisci la mappa combinata dei dati dei componenti
            Map<String, Object> combinedModel = buildModel();
            model_html.addAllAttributes(combinedModel);
        }
        // Restituisco il nome del template da usare
        if(return_page_error != null) return return_page_error;
        return this.PageName;
    }

    //COMPONENTI 
    // Questo metodo serve per attivare l'autenticazione per la pagina
    public void SetAuth(String jwt) {
        if (serviceManager != null){
            setLogicComponents(new AuthComponent(serviceManager, jwt));
        }        
    }

    //CODICI D'ERRORE 
    // Metodo per permettere la personalizzazione della mappa
    // il metodo put di una HashMap sovrascrive il valore associato a
    // una chiave esistente se la chiave è già presente nella mappa.
    public void setErrorPage(String errorCode, String pageName) {
        errorPageMap.put(errorCode, pageName);
    }
    //overload nel caso in cui l'utente fornisce una lista intera 
    public void setErrorPage(Map<String, String> userErrorPageMap) {
        errorPageMap.putAll(userErrorPageMap);
    }
    //Qui setto il comportamento Standard agli errori 
    private void setStandardErrorPage() {
        errorPageMap.put("Auth_error", "redirect:/login");
        errorPageMap.put("default", "redirect:/error");
    }
    // Metodo per ottenere i messaggi d'errore basati sui codici d'errore
    private String ExecuteError(List<String> errorCodes) {
        if (errorCodes != null && !errorCodes.isEmpty()){
            for (String errorCode : errorCodes) {
                // per ora si ferma al primo errore, ma qui posso implementare ogni tipo di logica 
                // anche in base alla combinazione o if-else
                return errorPageMap.getOrDefault(errorCode, errorPageMap.get("default"));
            }
        }
        return null;
    }
    //GET E SET 
    public List<GenericLogicComponent> getLogicComponents() {
        return new ArrayList<>(LogicComponents); // Ritorna una copia per evitare modifiche esterne
    }

    public List<GenericObjectComponent> getObjectComponents() {
        return new ArrayList<>(ObjectComponents); // Ritorna una copia per evitare modifiche esterne
    }

    public void setObjectComponents(List<GenericObjectComponent> pageComponents) {
        this.ObjectComponents.addAll(pageComponents);
    }

    public void setObjectComponents(GenericObjectComponent... components) {
        this.ObjectComponents.addAll(Arrays.asList(components));
    }

    public void setLogicComponents(List<GenericLogicComponent> pageComponents) {
        this.LogicComponents.addAll(pageComponents);
    }

    public void setLogicComponents(GenericLogicComponent... components) {
        this.LogicComponents.addAll(Arrays.asList(components));
    }

}