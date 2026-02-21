package com.HerbertSantos.TH_brain.domain.usecase;

import com.HerbertSantos.TH_brain.domain.model.TriggerResponse;
import com.HerbertSantos.TH_brain.domain.model.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class FindTriggerFinalUserCase {

    private final GetTimeBasedGreetingUseCase getTimeBasedGreetingUseCase;

    public FindTriggerFinalUserCase(GetTimeBasedGreetingUseCase getTimeBasedGreetingUseCase) {
        this.getTimeBasedGreetingUseCase = getTimeBasedGreetingUseCase;
    }

    public TriggerResponse execute(User user){
        String text = user.getEffectiveText();
        if (text == null) return new TriggerResponse(false, null, null);
        for (String line : text.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.equalsIgnoreCase("tchau th") || trimmed.equalsIgnoreCase("exit")) {
                return new TriggerResponse(
                        true,
                        "Finalizando, tenha um " + getTimeBasedGreetingUseCase.execute(user.getTimeStamp()),
                        LocalDateTime.now()
                );
            }
        }
        return new TriggerResponse(false, null, null);
    }


}
