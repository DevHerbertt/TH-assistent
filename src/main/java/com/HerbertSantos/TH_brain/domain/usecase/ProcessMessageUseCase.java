package com.HerbertSantos.TH_brain.domain.usecase;

import com.HerbertSantos.TH_brain.domain.gateway.AiGateway;
import com.HerbertSantos.TH_brain.domain.model.AiResponse;
import com.HerbertSantos.TH_brain.domain.model.User;
import org.springframework.stereotype.Service;

@Service
public class ProcessMessageUseCase {
    private final FindTriggerInitUserCase triggerInit;
    private final FindTriggerFinalUserCase triggerFinal;
    private final AiGateway aiGateway;

    public ProcessMessageUseCase(FindTriggerInitUserCase triggerInit, FindTriggerFinalUserCase triggerFinal, AiGateway aiGateway) {
        this.triggerInit = triggerInit;
        this.triggerFinal = triggerFinal;
        this.aiGateway = aiGateway;
    }

    public AiResponse execute(User user) {
        // 1. Se disser tchau/exit, desliga e retorna aqui (estado será persistido no controller)
        var responseFinal = triggerFinal.execute(user);
        if (responseFinal.trigger()) {
            user.setConversationActivate(false);
            return new AiResponse(responseFinal.menssger(), "System-Th-brain-automatic", true);
        }

        // 2. Se conversa não está ativa, só reage a "ola th" / "th"; caso contrário silêncio (null)
        if (!user.isConversationActivate()) {
            var responseInit = triggerInit.execute(user);
            if (responseInit.trigger()) {
                user.setConversationActivate(true);
                return new AiResponse(responseInit.menssger(), "System-Th-brain-automatic", true);
            }
            return null;
        }

        // 3. Conversa ativa: chama a IA
        return aiGateway.conversation(user);
    }

}
