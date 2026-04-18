package com.HerbertSantos.TH_brain.infrastructure.gateway;

import com.HerbertSantos.TH_brain.domain.exception.BrainAiException;
import com.HerbertSantos.TH_brain.domain.gateway.AiGateway;
import com.HerbertSantos.TH_brain.domain.model.AiResponse;
import com.HerbertSantos.TH_brain.domain.model.User;
import com.HerbertSantos.TH_brain.infrastructure.config.AiPromptConfig;
import com.HerbertSantos.TH_brain.infrastructure.gateway.dto.OllamaRequest;
import com.HerbertSantos.TH_brain.infrastructure.gateway.dto.OllamaResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

    private static final Logger log = LoggerFactory.getLogger(OllamaAiGateway.class);
    private static final int MAX_CONTEXT_LENGTH = 2_000;

    private final RestTemplate restTemplate;
    private final String ollamaModel;
    private final String baseUrl;
    private OllamaRequest dataRequest;


    private static final ConcurrentHashMap<String, String> lastTurnByUser = new ConcurrentHashMap<>();

    public OllamaAiGateway(RestTemplate restTemplate,
                           @Value("${ollama.model:llama3.2}") String ollamaModel,
                           @Value("${ollama.base-url:http://localhost:11434}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.ollamaModel = ollamaModel;
        this.baseUrl = baseUrl;
    }

    @Override
    public AiResponse conversation(User user) {
        String userKey = user.getFrom() != null ? user.getFrom() : "unknown";

        if (!user.isConversationActivate()) {
            log.debug("[Ollama] Conversa inativa para user={}, ignorando chamada", userKey);
            return null;
        }

        String url = baseUrl + "/api/generate";
        String userContent = user.getEffectiveText();
        log.info("[Ollama] Iniciando conversa | user={} | model={} | url={}", userKey, ollamaModel, url);

        String previousContext = lastTurnByUser.get(userKey);
        StringBuilder prompt = new StringBuilder();
        prompt.append(AiPromptConfig.getSystemRules());
        if (previousContext != null && !previousContext.isBlank()) {
            String trimmed = previousContext.length() > MAX_CONTEXT_LENGTH
                    ? previousContext.substring(previousContext.length() - MAX_CONTEXT_LENGTH)
                    : previousContext;
            prompt.append("\n\nConversa anterior (para não perder o fio da meada):\n").append(trimmed);
            log.debug("[Ollama] Contexto anterior presente para user={}, tamanho={} (max={})",
                    userKey, trimmed.length(), MAX_CONTEXT_LENGTH);
        }
        prompt.append("\n\nMensagens atuais do usuário (responda uma única vez considerando tudo):\n").append(userContent);

        int promptLength = prompt.length();
        log.debug("[Ollama] Prompt montado | user={} | tamanho={} caracteres", userKey, promptLength);

        dataRequest = new OllamaRequest(ollamaModel, prompt.toString(), false);

        try {
            log.debug("[Ollama] Chamando API | user={} | model={}", userKey, ollamaModel);
            OllamaResponse response = restTemplate.postForObject(url, dataRequest, OllamaResponse.class);

            if (response == null) {
                log.warn("[Ollama] Resposta nula da API | user={} | url={}", userKey, url);
                return null;
            }

            String thResponse = response.response();
            String newTurn = "Usuário: " + userContent + "\nTH: " + thResponse;
            lastTurnByUser.put(userKey, newTurn);

            log.info("[Ollama] Resposta recebida | user={} | model={} | resposta length={}",
                    userKey, response.model(), thResponse != null ? thResponse.length() : 0);
            return new AiResponse(thResponse, response.model(), true);

        } catch (Exception e) {
            log.error("[Ollama] Erro ao chamar API | user={} | model={} | url={} | erro={}",
                    userKey, ollamaModel, url, e.getMessage(), e);
            throw new BrainAiException("Erro ao se conectar com ollama \n version : " + dataRequest.model()
                    + " Horario " + LocalDateTime.now(),
                    e.getMessage());
        }
    }

    /** Limpa o contexto da última troca para o usuário (ex.: quando ele diz "tchau th"). Temporário até BD. */
    public static void clearContextForUser(String userKey) {
        if (userKey != null) {
            lastTurnByUser.remove(userKey);
            log.debug("[Ollama] Contexto limpo para user={}", userKey);
        }
    }
}
