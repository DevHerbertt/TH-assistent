package com.HerbertSantos.TH_brain.infrastructure.gateway.dto;

public record OllamaRequest(
        String model,
        String prompt,
        boolean stream
) {
}
