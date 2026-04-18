# TH-assistent-brain

**Backend do cérebro da TH (Tecnologia Humana)** — assistente pessoal com IA local via [Ollama](https://ollama.com), pensada para integração com WhatsApp e outros canais através de um orquestrador (ex.: [th-bridge](https://github.com) em Node.js).

---

## Índice

- [Sobre o projeto](#sobre-o-projeto)
- [Funcionalidades](#funcionalidades)
- [Stack tecnológica](#stack-tecnológica)
- [Arquitetura](#arquitetura)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Pré-requisitos](#pré-requisitos)
- [Instalação e execução](#instalação-e-execução)
- [Configuração](#configuração)
- [API](#api)
- [Comportamento e estado](#comportamento-e-estado)
- [Integração (th-bridge)](#integração-th-bridge)
- [Roadmap](#roadmap)
- [Autor e licença](#autor-e-licença)

---

## Sobre o projeto

O **TH-brain** é o núcleo de processamento da assistente **TH (Tecnologia Humana)**. Ele:

- Recebe mensagens de usuários (via API REST).
- Aplica regras de **triggers** (início e fim de conversa).
- Mantém **estado de conversa** por usuário em memória.
- Envia o conteúdo para um modelo de linguagem rodando localmente (**Ollama**, ex.: Llama 3.2) e devolve uma única resposta.

O front de conversa (WhatsApp, Telegram, etc.) fica em um serviço separado (o **th-bridge**), que chama o TH-brain via HTTP. Assim, o “cérebro” fica desacoplado do canal.

---

## Funcionalidades

| Funcionalidade | Descrição |
|----------------|-----------|
| **Triggers de conversa** | "ola th" / "th" ativa a conversa; "tchau th" / "exit" desativa e limpa contexto. |
| **Levas de mensagens** | Suporte a `text` (uma mensagem) ou `messages` (várias); a TH responde uma vez considerando todas. |
| **Contexto da última troca** | Em memória, guarda a última leva do usuário + resposta da TH para o modelo não “esquecer” o assunto (temporário até persistência em BD). |
| **Estado por usuário** | `conversationActivate` e contexto são mantidos por identificador (`from`). |
| **Personalidade configurável** | System prompt em `AiPromptConfig` (tom, gírias, tamanho da resposta, etc.). |
| **Sem persistência (por enquanto)** | Tudo em memória; preparado para futura migração para banco de dados. |

---

## Stack tecnológica

- **Java 21**
- **Spring Boot 4.0.5** (Web MVC, Validation, Actuator, RestClient)
- **Ollama** (modelo local, ex.: `llama3.2`)
- **Lombok**
- **Maven**

Não utiliza JPA nem banco de dados na versão atual.

---

## Arquitetura

O projeto segue **Clean Architecture**: domínio no centro, casos de uso orquestrando, infraestrutura e entrada/saída nas pontas.

```
                    ┌─────────────────────────────────────────┐
                    │           BrainController                │  ← Entrypoint (REST)
                    │  POST /api/v1/brain/process              │
                    └────────────────────┬────────────────────┘
                                         │
                                         ▼
                    ┌─────────────────────────────────────────┐
                    │       ProcessMessageUseCase             │  ← Domain / Application
                    │  (triggers init/final → AiGateway)       │
                    └────────────────────┬────────────────────┘
                                         │
              ┌──────────────────────────┼──────────────────────────┐
              ▼                          ▼                          ▼
    FindTriggerInitUserCase    FindTriggerFinalUserCase      AiGateway (port)
    GetTimeBasedGreetingUseCase                                      │
                                                                     ▼
                    ┌─────────────────────────────────────────┐
                    │         OllamaAiGateway                 │  ← Infrastructure
                    │  (RestTemplate → Ollama API)            │
                    └─────────────────────────────────────────┘
```

- **Domain**: modelos (`User`, `AiResponse`, `TriggerResponse`, `MessageType`), portas (`AiGateway`), exceções.
- **Use cases**: orquestração dos triggers e da chamada à IA; dependem apenas da interface `AiGateway`.
- **Infrastructure**: implementação de `AiGateway` (Ollama), DTOs, configuração (prompt, `RestTemplate`).
- **Entrypoint**: controller REST que recebe o `User`, aplica estado em memória e chama o use case.

---

## Estrutura do projeto

```
TH-brain/
├── src/main/java/com/HerbertSantos/TH_brain/
│   ├── ThBrainApplication.java
│   ├── domain/
│   │   ├── exception/       # BrainAiException
│   │   ├── gateway/         # AiGateway (port)
│   │   ├── model/           # User, AiResponse, TriggerResponse, MessageType
│   │   └── usecase/         # ProcessMessage, FindTriggerInit/Final, GetTimeBasedGreeting
│   ├── entrypoint/rest/
│   │   └── BrainController.java
│   └── infrastructure/
│       ├── config/          # AiPromptConfig, RestTemplateConfig
│       └── gateway/         # OllamaAiGateway, dto (OllamaRequest, OllamaResponse)
├── src/main/resources/
│   └── application.properties
├── docs/
│   └── PROMPT_TH_BRIDGE_NODE.md   # Contrato e sugestões para o th-bridge
├── pom.xml
└── README.md
```

---

## Pré-requisitos

- **JDK 21** (ou superior compatível)
- **Maven 3.8+**
- **Ollama** instalado e em execução, com pelo menos um modelo (ex.: `llama3.2`)

Para instalar o Ollama e baixar um modelo:

```bash
# Windows (winget)
winget install Ollama.Ollama

# Depois, no terminal:
ollama pull llama3.2
ollama run llama3.2   # opcional: testar no CLI
```

---

## Instalação e execução

### Build

```bash
cd TH-brain
mvn clean install
```

### Executar

```bash
mvn spring-boot:run
```

A aplicação sobe em **http://localhost:8080**. O Ollama deve estar rodando em **http://localhost:11434** (padrão).

### Testar o endpoint

```bash
curl -X POST http://localhost:8080/api/v1/brain/process \
  -H "Content-Type: application/json" \
  -d "{\"from\":\"user1\",\"text\":\"ola th\",\"timeStamp\":1730000000000}"
```

Resposta esperada (200): JSON com `response`, `model`, `dateTime`, `isSucess`.

---

## Configuração

| Propriedade | Descrição | Padrão |
|-------------|-----------|--------|
| `spring.application.name` | Nome da aplicação | `TH-brain` |
| `spring.docker.compose.enabled` | Habilita subida automática de Docker Compose | `false` |
| `ollama.model` | Nome do modelo no Ollama | `llama3.2` |
| `ollama.base-url` | URL base da API do Ollama | `http://localhost:11434` |

**TH mais esperta:** o system prompt já orienta a TH a usar contexto e raciocinar. Para respostas ainda melhores, use um modelo mais capaz no Ollama e configure em `application.properties`:

- `ollama.model=llama3.1:8b` — melhor raciocínio que o 3.2 3B
- `ollama.model=qwen2.5:7b` — muito bom em português e tarefas
- `ollama.model=mistral` ou `mixtral` — modelos fortes (exigem mais RAM)

Baixe o modelo com `ollama pull <nome>` antes de alterar a propriedade.

---

## API

### `POST /api/v1/brain/process`

Processa uma (ou várias) mensagem(ns) do usuário e retorna a resposta da TH ou 204.

#### Request body (JSON)

| Campo | Tipo | Obrigatório | Descrição |
|-------|------|-------------|-----------|
| `from` | string | Sim | Identificador único do usuário (ex.: número WhatsApp). |
| `text` | string | Condicional | Uma única mensagem. Ignorado se `messages` for enviado. |
| `messages` | array de string | Condicional | Várias mensagens em sequência; a TH responde uma vez considerando todas. |
| `timeStamp` | number | Sim | Timestamp em milissegundos (ex.: `Date.now()`). |
| `conversationActivate` | boolean | Não | Estado da conversa; o backend mantém por `from` e pode sobrescrever. |
| `name` | string | Não | Nome do usuário. |
| `menssageId` | string | Não | ID da mensagem no canal de origem. |
| `isgroup` | boolean | Não | Indica se é grupo. |
| `messageType` | string | Não | `conversation`, `extendedText`, `imageMessage`, etc. |
| `deviceType` | string | Não | Ex.: `android`. |

É necessário enviar **`text`** ou **`messages`** (ou ambos; nesse caso, com `messages` preenchido, o texto efetivo é a junção de `messages`).

#### Respostas

- **200 OK** — Corpo: `AiResponse` (`response`, `model`, `dateTime`, `isSucess`). Enviar o campo `response` ao usuário no canal.
- **204 No Content** — TH em silêncio (conversa inativa ou mensagem que não ativa trigger nem IA). Não enviar resposta ao usuário ou tratar conforme regra do bridge.

#### Exemplo mínimo

```json
{
  "from": "5511999999999",
  "text": "qual a capital do Brasil?",
  "timeStamp": 1730000000000
}
```

---

## Comportamento e estado

- **Triggers**
  - **Início**: se o texto efetivo contiver uma linha exatamente `ola th` ou `th`, a conversa é ativada e a TH responde com saudação (horário + apresentação).
  - **Fim**: se contiver `tchau th` ou `exit`, a conversa é desativada, a TH se despede e o contexto em memória daquele usuário é limpo.
- **Estado em memória**
  - `conversationStateByUser`: por `from`, guarda se a conversa está ativa.
  - `lastTurnByUser` (no `OllamaAiGateway`): última troca (mensagens do usuário + resposta da TH) para montar o contexto da próxima chamada ao Ollama.
- Tudo é **temporário**; a persistência em banco de dados está no roadmap.

---

## Integração (th-bridge)

O cliente que recebe mensagens (ex.: WhatsApp) e envia para o TH-brain é o **th-bridge** (Node.js). O contrato da API e sugestões de implementação (debounce, uso de `messages`, etc.) estão em:

- **[docs/PROMPT_TH_BRIDGE_NODE.md](docs/PROMPT_TH_BRIDGE_NODE.md)**

Resumo para o bridge:

1. Montar o payload no formato do `User` (com `from`, `text` ou `messages`, `timeStamp`, etc.).
2. Fazer `POST` em `http://localhost:8080/api/v1/brain/process`.
3. Se 200, enviar `response` ao usuário; se 204, não enviar resposta da TH.
4. Recomendado: **debounce** (ex.: 2–3 s) por usuário e envio de uma única requisição com `messages` contendo a leva de mensagens.

---

## Roadmap

- [ ] Persistência em banco de dados (histórico de conversas e estado por usuário).
- [ ] Configuração da URL do Ollama e do modelo via `application.properties` ou env.
- [ ] Suporte a múltiplos modelos ou troca dinâmica.
- [ ] Métricas e health check específico para o Ollama (Actuator já incluído).

---

## Autor e licença

- **TH (Tecnologia Humana)** — assistente criada por **Herbert Matheus Oliveira Santos**.
- Projeto em desenvolvimento; licença a definir.

Para dúvidas ou contribuições, abra uma issue ou entre em contato com o autor.
