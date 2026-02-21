package com.HerbertSantos.TH_brain.domain.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiResponse {
    private LocalDateTime dateTime;
    private String response;
    private String model;
    private Boolean isSucess;


    public AiResponse(String response, String model,Boolean isSucess) {
        this.dateTime = LocalDateTime.now();
        this.response = response;
        this.model = model;
        this.isSucess = isSucess;
    }


}
