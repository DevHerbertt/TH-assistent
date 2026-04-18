package com.HerbertSantos.TH_brain.domain.usecase;

import com.HerbertSantos.TH_brain.domain.model.TriggerResponse;
import com.HerbertSantos.TH_brain.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FindTriggerFinalUserCaseTest {

    private GetTimeBasedGreetingUseCase greetingUseCase;
    private FindTriggerFinalUserCase finalUserCase;

    @BeforeEach
    void setUp(){
        greetingUseCase = mock(GetTimeBasedGreetingUseCase.class);

        finalUserCase = new FindTriggerFinalUserCase(greetingUseCase);

    }

    @Test
    void deveResponderQuandoRespostaForTchauTh(){
        //Creating user Test

        User user = new User("the tester");
        user.setText("tchau th");

        //Creating the response of greetingTest
        when(greetingUseCase.execute(any())).thenReturn("boa tarde");

        //Creating Tests
        TriggerResponse response = finalUserCase.execute(user);


        assertTrue(response.trigger());
        String message = "Finalizando, tenha um boa tarde";
        assertEquals(message.toLowerCase(),response.menssger().toLowerCase());


    }

}