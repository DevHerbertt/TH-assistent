package com.HerbertSantos.TH_brain.domain.usecase;

import com.HerbertSantos.TH_brain.domain.model.TriggerResponse;
import com.HerbertSantos.TH_brain.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.stereotype.Service;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class FindTriggerInitUserCaseTest {

   private FindTriggerInitUserCase userCase;
   private GetTimeBasedGreetingUseCase greetingUseCase;

    @BeforeEach
    void setUp(){
        greetingUseCase = mock(GetTimeBasedGreetingUseCase.class);

        userCase = new FindTriggerInitUserCase(greetingUseCase);
    }

    @Test
    void deveAtivarQuandoTextoForOlaTh(){
        //Creating the user Test
        User user = new User("usuario aleatorio");
        user.setText("ola th");

        // when the class with any value be activated , you should return BOA TARDE
        when(greetingUseCase.execute(any())).thenReturn("Boa Tarde");

        // activate test

        TriggerResponse response = userCase.execute(user);

        // what should print for user
        assertTrue(response.trigger(), "Should vereficated the wordKey");

        String message = "Olá! Boa tarde Sou o TH, Uma assistência inteligente.";
        assertEquals(message.toLowerCase(),response.menssger().toLowerCase());







    }
}