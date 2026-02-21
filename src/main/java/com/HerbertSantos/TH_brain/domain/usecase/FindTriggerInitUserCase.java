package com.HerbertSantos.TH_brain.domain.usecase;

import com.HerbertSantos.TH_brain.domain.model.TriggerResponse;
import com.HerbertSantos.TH_brain.domain.model.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class FindTriggerInitUserCase {

    private final GetTimeBasedGreetingUseCase getTimeBasedGreetingUseCase;

    public FindTriggerInitUserCase(GetTimeBasedGreetingUseCase getTimeBasedGreetingUseCase) {
        this.getTimeBasedGreetingUseCase = getTimeBasedGreetingUseCase;
    }

    public TriggerResponse execute(User user){
        String text = user.getEffectiveText();
        if (text == null) return new TriggerResponse(false, null, null);
        for (String line : text.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.equalsIgnoreCase("ola th") || trimmed.equalsIgnoreCase("th")) {
                return new TriggerResponse(
                   true,
                   "Olá!" + getTimeBasedGreetingUseCase.execute(user.getTimeStamp()) + " Sou o TH, Uma assistência inteligente.",
                   LocalDateTime.now()
                );
            }
        }
        return new TriggerResponse(false, null, null);
    }


}
