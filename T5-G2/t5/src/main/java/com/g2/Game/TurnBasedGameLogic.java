package com.g2.Game;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.g2.Interfaces.ServiceManager;

public class TurnBasedGameLogic extends GameLogic {

    private int currentTurn;
    private int userScore;
    private int robotScore;
    private int totalTurns = 10;
    private Boolean GameOVer = false;

    public TurnBasedGameLogic(ServiceManager serviceManager, String PlayerID, String ClasseUT,
                                String type_robot, String difficulty) {
        super(serviceManager, PlayerID, ClasseUT, type_robot, difficulty);
        currentTurn = 0;
    }

    @Override
    public void playTurn(int userScore, int robotScore) {
        String Time = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
        currentTurn++;
        //CreateTurn(Time, userScore);
        System.out.println("Turn " + currentTurn + " played. User Score: " + userScore + ", Robot Score: " + robotScore);
    }

    @Override
    public Boolean isGameEnd() {
        return false; //il giocatore può fare quanti turni vuole quindi ritorno sempre True
    }

    @Override
    public int GetScore(int coverage) {
        // Se loc è 0, il punteggio è sempre 0
        if (coverage == 0) {
            return 0;
        }

        // Calcolo della percentuale della posizione
        double locPerc = ((double) coverage) / 100;
        // Penalità crescente per ogni turno aggiuntivo
        double penaltyFactor = Math.pow(0.9, currentTurn);
        // Calcolo del punteggio
        double score = locPerc * 100 * penaltyFactor;
        return (int) Math.ceil(score);
    }

}