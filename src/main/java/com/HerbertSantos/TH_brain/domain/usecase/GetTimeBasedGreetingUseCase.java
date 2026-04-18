package com.HerbertSantos.TH_brain.domain.usecase;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class GetTimeBasedGreetingUseCase {

    public String execute(Long timeStamp) {
        if (timeStamp == null) {
            timeStamp = System.currentTimeMillis();
        }
        // 1. Converte os milissegundos para um ponto na linha do tempo
        Instant instant = Instant.ofEpochMilli(timeStamp);

        // 2. Converte para a data/hora local (considerando o fuso horário do sistema)
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

        // 3. Pega apenas a hora do dia (0 a 23)
        int hora = dateTime.getHour();

        // 4. Lógica de saudação
        if (hora >= 5 && hora < 12) {
            return "um Bom dia";
        } else if (hora >= 12 && hora < 18) {
            return " Boa tarde";
        } else {
            return " Boa noite";
        }
    }
}
