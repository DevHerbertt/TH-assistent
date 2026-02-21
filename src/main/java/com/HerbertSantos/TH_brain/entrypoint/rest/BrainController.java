package com.HerbertSantos.TH_brain.entrypoint.rest;

import com.HerbertSantos.TH_brain.domain.model.AiResponse;
import com.HerbertSantos.TH_brain.domain.model.User;
import com.HerbertSantos.TH_brain.domain.usecase.ProcessMessageUseCase;
import com.HerbertSantos.TH_brain.infrastructure.gateway.OllamaAiGateway;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/brain")
public class BrainController {

    private final ProcessMessageUseCase processMessageUseCase;
    /** Estado da conversa por usuário (from): true = ativa, false = desligada. Persiste entre requisições. */
    private static final Map<String, Boolean> conversationStateByUser = new ConcurrentHashMap<>();

    public BrainController(ProcessMessageUseCase processMessageUseCase) {
        this.processMessageUseCase = processMessageUseCase;
    }

    @PostMapping("/process")
    public ResponseEntity<AiResponse> handleWhatsAppMessage(@RequestBody User user) {
        String userKey = user.getFrom() != null ? user.getFrom() : "unknown";
        user.setConversationActivate(conversationStateByUser.getOrDefault(userKey, false));

        AiResponse response = processMessageUseCase.execute(user);

        conversationStateByUser.put(userKey, user.isConversationActivate());

        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        if (!user.isConversationActivate()) {
            OllamaAiGateway.clearContextForUser(userKey);
        }
        return ResponseEntity.ok(response);
    }
}