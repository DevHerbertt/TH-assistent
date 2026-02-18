package com.HerbertSantos.TH_brain.domain.usecase;

import com.HerbertSantos.TH_brain.domain.model.TriggerResponse;
import com.HerbertSantos.TH_brain.domain.model.User;

import java.time.LocalDateTime;

public class FindTriggerInitUserCase {
    private User user = new User();

    public FindTriggerInitUserCase(User user) {
        this.user = user;
    }

    private TriggerResponse execute(String text){
        if (text.equalsIgnoreCase("ola th") || text.equalsIgnoreCase("th")){
           return new TriggerResponse(
                   true,
                   "Olá! Sou o TH, Uma assistência inteligente.",
                   LocalDateTime.now()
           );
        }
        return new TriggerResponse(false,null,null);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
