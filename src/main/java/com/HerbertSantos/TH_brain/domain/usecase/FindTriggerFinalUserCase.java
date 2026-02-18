package com.HerbertSantos.TH_brain.domain.usecase;

import com.HerbertSantos.TH_brain.domain.model.TriggerResponse;
import com.HerbertSantos.TH_brain.domain.model.User;

import java.time.LocalDateTime;

public class FindTriggerFinalUserCase {
    private User user = new User();

    public FindTriggerFinalUserCase(User user) {
        this.user = user;
    }

    private TriggerResponse execute(String text){
        if (text.equalsIgnoreCase("tchau th") || text.equalsIgnoreCase("exit")){
           return new TriggerResponse(
                   true,
                   "Finalizando",
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
