package com.HerbertSantos.TH_brain.domain.model;

import java.time.LocalDateTime;

public record TriggerResponse(
        boolean trigger,
        String menssger,
        LocalDateTime dateTime
) {}
