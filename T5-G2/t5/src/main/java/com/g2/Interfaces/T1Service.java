/*
 *   Copyright (c) 2024 Stefano Marano https://github.com/StefanoMarano80017
 *   All rights reserved.

 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at

 *   http://www.apache.org/licenses/LICENSE-2.0

 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.g2.Interfaces;

import com.g2.Model.Achievement;
import com.g2.Model.AchievementProgress;
import com.g2.Model.Statistic;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import com.g2.Model.ClassUT;

@Service
public class T1Service extends BaseService {

    private static final String BASE_URL = "http://manvsclass-controller-1:8080";

    public T1Service(RestTemplate restTemplate) {
        super(restTemplate, BASE_URL);

        // Registrazione delle azioni
        registerAction("getStatistics", new ServiceActionDefinition(
                params -> getStatistics()
        ));

        registerAction("getAchievements", new ServiceActionDefinition(
                params -> getAchievements()
        ));

        registerAction("getClasses", new ServiceActionDefinition(
                params -> getClasses() //Metodo senza argomenti
        ));

        registerAction("getClassUnderTest", new ServiceActionDefinition(
                params -> getClassUnderTest((String) params[0]),
                String.class
        ));
    }

    // Metodi effettivi
    private List<Statistic> getStatistics() {
        try {
            return callRestGET("/statistics/list", null, new ParameterizedTypeReference<List<Statistic>>() {
            });
        } catch (Exception e) {
            throw new IllegalArgumentException("getStatistics fallimento errore:" + e.getMessage());
        }
    }

    private List<Achievement> getAchievements() {
        try {
            return callRestGET("/achievements/list", null, new ParameterizedTypeReference<List<Achievement>>() {
            });
        } catch (Exception e) {
            throw new IllegalArgumentException("getAchievements fallimento errore:" + e.getMessage());
        }
    }

    private List<ClassUT> getClasses() {
        try {
            return callRestGET("/home", null, new ParameterizedTypeReference<List<ClassUT>>() {
            });
        } catch (Exception e) {
            throw new IllegalArgumentException("GetClasses fallimento errore:" + e.getMessage());
        }
    }

    private String getClassUnderTest(String nomeCUT) {
        try {
            byte[] result = callRestGET("/downloadFile/" + nomeCUT, null, byte[].class);
            return removeBOM(convertToString(result));
        } catch (Exception e) {
            throw new IllegalArgumentException("getClassUnderTest fallimento errore:" + e.getMessage());
        }
    }

}
