package com.HerbertSantos.TH_brain.infrastructure.gateway;

import com.HerbertSantos.TH_brain.domain.exception.BrainAiException;
import com.HerbertSantos.TH_brain.domain.gateway.AiGateway;
import com.HerbertSantos.TH_brain.domain.model.AiResponse;
import com.HerbertSantos.TH_brain.domain.model.User;
import com.HerbertSantos.TH_brain.infrastructure.config.AiPromptConfig;
import com.HerbertSantos.TH_brain.infrastructure.gateway.dto.OllamaRequest;
import com.HerbertSantos.TH_brain.infrastructure.gateway.dto.OllamaResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache em memória da última troca por usuário (temporário até persistir no BD).
 * Guarda a última leva de mensagens do usuário + resposta da TH para o Llama não perder o fio da conversa.
 */
@Component
public class OllamaAiGateway implements AiGateway {

    private static final int MAX_CONTEXT_LENGTH = 1_500;

    private final RestTemplate restTemplate;
    private OllamaRequest dataRequest;

    /** Última troca por usuário (from): "Usuário: ...\nTH: ..." — temporário até BD. */
    private static final ConcurrentHashMap<String, String> lastTurnByUser = new ConcurrentHashMap<>();

    public OllamaAiGateway(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public AiResponse conversation(User user) {
        if (!user.isConversationActivate()) {
            return null;
        }
        String url = "http://localhost:11434/api/generate";
        String userKey = user.getFrom() != null ? user.getFrom() : "unknown";
        String userContent = user.getEffectiveText();

        String previousContext = lastTurnByUser.get(userKey);
        StringBuilder prompt = new StringBuilder();
        prompt.append(AiPromptConfig.getSystemRules());
        if (previousContext != null && !previousContext.isBlank()) {
            String trimmed = previousContext.length() > MAX_CONTEXT_LENGTH
                    ? previousContext.substring(previousContext.length() - MAX_CONTEXT_LENGTH)
                    : previousContext;
            prompt.append("\n\nConversa anterior (para não perder o fio da meada):\n").append(trimmed);
        }
        prompt.append("\n\nMensagens atuais do usuário (responda uma única vez considerando tudo):\n").append(userContent);

        dataRequest = new OllamaRequest("llama3.2", prompt.toString(), false);

        try {
            OllamaResponse response = restTemplate.postForObject(url, dataRequest, OllamaResponse.class);
            if (response == null) {
                return null;
            }
            String thResponse = response.response();
            String newTurn = "Usuário: " + userContent + "\nTH: " + thResponse;
            lastTurnByUser.put(userKey, newTurn);

            return new AiResponse(thResponse, response.model(), true);
        } catch (Exception e) {
            throw new BrainAiException("Erro ao se conectar com ollama \n version : " + dataRequest.model()
                    + " Horario " + LocalDateTime.now(),
                    e.getMessage());
        }
    }

    /** Limpa o contexto da última troca para o usuário (ex.: quando ele diz "tchau th"). Temporário até BD. */
    public static void clearContextForUser(String userKey) {
        if (userKey != null) {
            lastTurnByUser.remove(userKey);
        }
    }
}
