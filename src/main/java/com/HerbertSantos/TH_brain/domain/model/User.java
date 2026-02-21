package com.HerbertSantos.TH_brain.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class User {
    private boolean conversationActivate;
    private String from;
    private String text;
    /** Várias mensagens enviadas em sequência (bridge faz debounce e manda de uma vez). Se preenchido, o fluxo usa isso em vez de text. */
    private List<String> messages;
    private Long timeStamp;
    private String name;
    private String menssageId;
    private boolean isgroup;
    private MessageType messageType;
    private String deviceType;

    /** Texto efetivo para o processamento: se tiver messages, junta todas; senão usa text. */
    public String getEffectiveText() {
        if (messages != null && !messages.isEmpty()) {
            return String.join("\n", messages);
        }
        return text != null ? text : "";
    }

    public User(boolean conversationActivate, String from, String text, Long timeStamp, String name, String menssageId, boolean isgroup, MessageType messageType, String deviceType) {
        this.conversationActivate = conversationActivate;
        this.from = from;
        this.text = text;
        this.timeStamp = timeStamp;
        this.name = name;
        this.menssageId = menssageId;
        this.isgroup = isgroup;
        this.messageType = messageType;
        this.deviceType = deviceType;
    }

}
