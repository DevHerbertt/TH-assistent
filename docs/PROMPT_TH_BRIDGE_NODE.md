# Prompt para o agente de IA: th-bridge (Node) conversando com o TH-brain

Use o texto abaixo ao pedir para um agente de IA (ou desenvolvedor) implementar a parte que falta no **th-bridge** (Node.js) para conversar com o **TH-brain** (backend Spring Boot).

---

## Prompt

Implemente no projeto **th-bridge** (Node.js) a integração com o backend **TH-brain** (Spring Boot) para que mensagens recebidas (ex.: WhatsApp) sejam enviadas ao cérebro e a resposta seja devolvida ao usuário.

### Contrato do TH-brain (backend já pronto)

- **URL base (desenvolvimento):** `http://localhost:8080`
- **Endpoint:** `POST /api/v1/brain/process`
- **Content-Type:** `application/json`

**Corpo da requisição (RequestBody) – modelo `User`:**

```json
{
  "conversationActivate": false,
  "from": "número ou id do remetente",
  "text": "texto da mensagem recebida",
  "messages": ["msg1", "msg2", "msg3"],
  "timeStamp": 1730000000000,
  "name": "nome do usuário",
  "menssageId": "id da mensagem no WhatsApp/origem",
  "isgroup": false,
  "messageType": "conversation",
  "deviceType": "android"
}
```

**Campos obrigatórios para o TH processar:**

- `text` OU `messages` – uma mensagem em `text` **ou** várias em `messages`. Se enviar `messages`, o backend junta todas e a TH responde **uma única vez** considerando o contexto completo (evita ela responder a cada mensagem e se confundir).
- `text` – uma única mensagem (comportamento clássico).
- `timeStamp` – número (milissegundos, ex.: `Date.now()`).
- `from` – identificador do remetente.
- `conversationActivate` – boolean; o backend usa para saber se a conversa já foi iniciada (pode começar como `false`).

**`messageType`** deve ser um dos valores: `conversation`, `extendedText`, `imageMessage`, `videoMessage`, `audioMessage`, `documentMessage`, `stickerMessage`, `other`.

**Respostas do TH-brain:**

1. **200 OK** – corpo é um objeto `AiResponse`:
   ```json
   {
     "dateTime": "2026-02-21T14:30:00",
     "response": "Texto que o TH gerou (resposta ao usuário)",
     "model": "llama3.2",
     "isSucess": true
   }
   ```
   Usar o campo `response` para enviar a resposta de volta ao usuário (ex.: enviar no WhatsApp).

2. **204 No Content** – o TH não respondeu (ex.: conversa inativa). Não enviar nenhuma mensagem de volta ou enviar uma mensagem neutra, conforme regra de negócio do bridge.

### O que implementar no th-bridge (Node)

1. **Ao receber uma mensagem** (do WhatsApp ou do canal que o bridge escuta):
   - Montar o objeto no formato do `User` acima (mapeando campos da mensagem recebida para `from`, `text`, `timeStamp`, `name`, `menssageId`, `isgroup`, `messageType`, `deviceType`, `conversationActivate`).
   - Fazer um `POST` para `http://localhost:8080/api/v1/brain/process` com esse JSON.

2. **Tratar a resposta:**
   - Se for **200**: ler o JSON, pegar `response` e enviar esse texto de volta para o usuário (no mesmo canal em que a mensagem chegou).
   - Se for **204**: não enviar resposta do TH (ou tratar conforme regra do bridge).
   - Em caso de erro de rede ou 5xx: logar e, se fizer sentido, enviar uma mensagem de fallback ao usuário (ex.: "TH temporariamente indisponível").

3. **Estado de conversa (opcional mas recomendado):**
   - O backend usa `conversationActivate`: ao receber "ola th" / "th" o TH ativa; ao receber "tchau th" / "exit" desativa.
   - O bridge pode manter por remetente (`from`) um estado local `conversationActivate` e enviá-lo no próximo request, ou sempre enviar `false` e deixar o backend decidir (conforme implementação atual do TH-brain).

4. **Debounce (recomendado):** Para a TH não responder a cada mensagem e se confundir, o bridge deve **esperar o usuário terminar de mandar as mensagens** e só então chamar o backend uma vez:
   - Ao receber uma mensagem, não chame o backend na hora. Guarde em um buffer por usuário (`from`).
   - Inicie (ou reinicie) um timer de 2–3 segundos. Se chegar outra mensagem do mesmo usuário antes do timer vencer, adicione ao buffer e reinicie o timer.
   - Quando o timer vencer (usuário parou de mandar), envie **uma única** requisição com o campo `messages` preenchido com o array de textos (ex.: `"messages": ["oi", "qual o tempo?", "obrigado"]`). O backend responde uma vez considerando todas. Depois limpe o buffer daquele usuário.
   - Assim a TH recebe o contexto completo e não fica se explicando ou respondendo no meio da sequência.

5. **Configuração:**
   - Deixar a URL base do TH-brain configurável (variável de ambiente, ex.: `TH_BRAIN_URL=http://localhost:8080`), para desenvolvimento e produção.

### Resumo

- **th-bridge** recebe mensagem → monta `User` → `POST /api/v1/brain/process` → lê `response` em 200 ou trata 204 → envia a resposta ao usuário no canal de origem.

Implemente essa integração no th-bridge em Node.js (com o framework que o projeto já usar: Express, Fastify, Nest, etc.), garantindo o contrato acima e tratamento de erros e 204.
