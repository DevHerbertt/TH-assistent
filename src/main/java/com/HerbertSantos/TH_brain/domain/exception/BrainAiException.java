package com.HerbertSantos.TH_brain.domain.exception;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BrainAiException extends RuntimeException{

    private String details;

    public BrainAiException(String mensagem,String detail) {
        super(mensagem);
        this.details = detail;

    }
}
