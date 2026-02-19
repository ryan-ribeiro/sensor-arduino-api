
# Sensor Arduino API

## 📋 Descrição

Uma API REST robusta desenvolvida com **Spring Boot 3.5.9** e **Java 17** para integração com projetos Arduino. O principal objetivo é **alimentar projetos de Arduino para persistência de dados em banco de dados PostgreSQL**, facilitando a coleta, armazenamento e recuperação de dados de sensores através de uma interface segura e bem estruturada.

A API oferece funcionalidades para:
- Registro e gerenciamento de usuários
- Autenticação segura via JWT (OAuth 2.0) ou Basic Auth
- Autorização baseada em roles (ADMIN, USER)
- Persistência de eventos de sensores
- Gerenciamento de mensagens (Bipes) entre usuários
- Integração com dispositivos Arduino

---

## 🚀 Tecnologias Utilizadas

- **Java 17**
- **Spring Boot 3.5.9**
- **Spring Security** (OAuth 2.0 / JWT)
- **Spring Data JPA**
- **PostgreSQL**
- **Hibernate**
- **Maven**
- **Docker** (opcional)

---

## 🔧 Instalação e Setup

### Pré-requisitos

- Java 17 ou superior
- Maven 3.8+
- PostgreSQL 12+
- Git

### 1. Clonar o Repositório

```bash
git clone <repository-url>
cd sensor-arduino-api/sensor-api
```

### 2. Configurar o Banco de Dados

Crie um banco PostgreSQL:

```sql
CREATE DATABASE sensor_api;
```

### 3. Gerar Chaves RSA para JWT

Para gerar as chaves pública e privada necessárias para assinatura de tokens JWT:

```bash
cd sensor-api
javac GenerateKeys.java
java GenerateKeys
```

Isso gerará dois arquivos:
- `app.pub` - Chave pública (salvar em `src/main/resources/`)
- `app.key` - Chave privada (salvar em `src/main/resources/`)

### 4. Configurar application.properties

Edite `src/main/resources/application.properties`:

```properties
# JWT Keys
jwt.public.key=classpath:app.pub
jwt.private.key=classpath:app.key

# Aplicação
spring.application.name=sensor-presenca-api

# Banco de Dados PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/sensor_api
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.jdbc.time_zone=UTC
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

### 5. Executar a Aplicação

```bash
mvn clean install
mvn spring-boot:run
```

A API estará disponível em `http://localhost:8080`

### 6. Build em Produção

```bash
mvn clean package
java -jar target/sensor-api-1.0.0-SNAPSHOT.jar
```

---

## 🔐 Autenticação e Autorização

### Tipos de Autenticação

#### 1. **JWT (JSON Web Token) - Recomendado para Web**

Após fazer login, você recebe um token JWT que deve ser enviado no header:

```
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### 2. **Basic Auth - Recomendado para Arduino**

Para projetos Arduino com conectividade limitada, use Basic Auth:

```
Authorization: Basic base64(username:password)
```

Exemplo em Arduino com WiFi:

```cpp
String auth = base64_encode(String(username + ":" + password).c_str());
http.addHeader("Authorization", "Basic " + auth);
```

### Roles e Permissões

A API possui dois roles:

| Role | Descrição | Permissões |
|------|-----------|-----------|
| **ADMIN** | Administrador do sistema | Acesso a `/admin/**` |
| **USER** | Usuário regular | Criar eventos, enviar bipes, acessar seus próprios dados |

---

## 👤 Registro de Usuários

### 1. Registrar Novo Usuário

**Endpoint:** `POST /users`

**Autenticação:** ❌ Não requerida (público)

**Body:**
```json
{
  "username": "seu_usuario",
  "password": "sua_senha",
  "email": "seu_email@example.com"
}
```

**Resposta:**
```
Status: 201 Created
```

### Exemplo com cURL:

```bash
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "arduino_user",
    "password": "senha123",
    "email": "sensor@example.com"
  }'
```

---

## 🔑 Login e Autenticação

### 1. Fazer Login

**Endpoint:** `POST /login`

**Autenticação:** ❌ Não requerida (público)

**Body:**
```json
{
  "username": "seu_usuario",
  "password": "sua_senha"
}
```

**Resposta:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 3600
}
```

### Exemplo com cURL:

```bash
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "arduino_user",
    "password": "senha123"
  }'
```

### Usar o Token

Copie o `access_token` e inclua em todas as requisições subsequentes:

```bash
curl -X GET http://localhost:8080/eventos \
  -H "Authorization: Bearer seu_token_aqui"
```

---

## 📡 Rotas da API

### **Autenticação**

| Método | Rota | Descrição | Autenticação |
|--------|------|-----------|--------------|
| POST | `/login` | Fazer login e obter token JWT | ❌ Pública |
| POST | `/users` | Registrar novo usuário | ❌ Pública |

### **Usuários**

| Método | Rota | Descrição | Autenticação | Permissão |
|--------|------|-----------|--------------|-----------|
| GET | `/admin/users` | Listar todos os usuários | ✅ JWT | ADMIN |
| GET | `/users/update-bipe-info` | Atualizar informações de bipe do usuário | ✅ JWT | USER |

**Parâmetros para `/users/update-bipe-info`:**
- `username` (string) - Nome do usuário
- `local` (string) - Localização do sensor
- `arduino` (string) - ID do Arduino

### **Eventos (Dados de Sensores)**

| Método | Rota | Descrição | Autenticação | Permissão |
|--------|------|-----------|--------------|-----------|
| GET | `/eventos/admin/all` | Listar todos os eventos (admin) | ✅ JWT | ADMIN |
| GET | `/eventos` | Listar eventos do usuário logado | ✅ JWT | USER |
| GET | `/eventos/{id}` | Obter evento específico por ID | ✅ JWT | USER |
| POST | `/eventos/salvar` | Criar novo evento de sensor | ✅ JWT | USER |

#### Criar Evento (POST /eventos/salvar)

**Body:**
```json
{
  "temperaturaC": 25.5,
  "umidadeRelativa": 65.3,
  "local": "Sala 01",
  "arduino": "arduino_001",
  "data": "2024-02-19T10:30:00Z"
}
```

**Resposta:**
```json
{
  "id": 1,
  "userId": "uuid-do-usuario",
  "temperaturaC": 25.5,
  "umidadeRelativa": 65.3,
  "local": "Sala 01",
  "arduino": "arduino_001",
  "data": "2024-02-19T10:30:00Z"
}
```

**Status:** 201 Created

### **Bipes (Mensagens)**

| Método | Rota | Descrição | Autenticação | Permissão |
|--------|------|-----------|--------------|-----------|
| POST | `/bipes/enviarBipe` | Enviar um bipe para outro usuário | ✅ JWT | USER |
| GET | `/bipes/ultimo-bipe` | Obter última mensagem recebida | ✅ JWT | USER |
| GET | `/bipes/id-ultimo-bipe` | Obter ID do último bipe recebido | ✅ JWT | USER |
| GET | `/bipes/hora-ultimo-bipe` | Obter hora do último bipe recebido | ✅ JWT | USER |

#### Enviar Bipe (POST /bipes/enviarBipe)

**Body:**
```json
{
  "receiverId": "uuid-do-receptor",
  "mensagem": "Mensagem de controle do Arduino",
  "local": "Sala 01",
  "arduino": "arduino_001"
}
```

**Resposta:**
```json
{
  "id": "uuid",
  "senderId": "uuid-do-remetente",
  "receiverId": "uuid-do-receptor",
  "mensagem": "Mensagem de controle do Arduino",
  "local": "Sala 01",
  "arduino": "arduino_001",
  "dataCriacao": "2024-02-19T10:30:00Z"
}
```

**Status:** 201 Created

#### Obter Último Bipe (GET /bipes/ultimo-bipe)

**Parâmetros de Query:**
- `arduino` (string) - ID do Arduino
- `local` (string) - Localização

**Resposta:**
```
"Mensagem de controle do Arduino"
```

**Status:** 200 OK

#### Obter ID do Último Bipe (GET /bipes/id-ultimo-bipe)

**Parâmetros de Query:**
- `arduino` (string) - ID do Arduino
- `local` (string) - Localização

**Resposta:**
```
"id-do-bipe-uuid"
```

**Status:** 200 OK

#### Obter Hora do Último Bipe (GET /bipes/hora-ultimo-bipe)

**Parâmetros de Query:**
- `arduino` (string) - ID do Arduino
- `local` (string) - Localização

**Resposta:**
```
"2024-02-19T10:30:00Z"
```

**Status:** 200 OK

---

## 🎯 Exemplo de Fluxo Completo

### 1. Registrar Usuário

```bash
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "meu_arduino",
    "password": "senha_segura",
    "email": "arduino@sensores.com"
  }'
```

### 2. Fazer Login

```bash
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "meu_arduino",
    "password": "senha_segura"
  }' | jq .
```

Copie o `access_token` retornado.

### 3. Enviar Evento (Dados de Sensor)

```bash
curl -X POST http://localhost:8080/eventos/salvar \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer seu_access_token" \
  -d '{
    "temperaturaC": 28.5,
    "umidadeRelativa": 45.2,
    "local": "Sala Principal",
    "arduino": "DHT11_001",
    "data": "2024-02-19T14:30:00Z"
  }'
```

### 4. Listar Seus Eventos

```bash
curl -X GET http://localhost:8080/eventos \
  -H "Authorization: Bearer seu_access_token"
```

### 5. Enviar Controle (Bipe)

```bash
curl -X POST http://localhost:8080/bipes/enviarBipe \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer seu_access_token" \
  -d '{
    "receiverId": "uuid-do-outro-usuario",
    "mensagem": "Ligar ventilador",
    "local": "Sala Principal",
    "arduino": "DHT11_001"
  }'
```

### 6. Obter Controles Pendentes

```bash
curl -X GET "http://localhost:8080/bipes/ultimo-bipe?arduino=DHT11_001&local=Sala%20Principal" \
  -H "Authorization: Bearer seu_access_token"
```

---

## 🤖 Integração com Arduino

### Exemplo com Arduino (DHT11 + WiFi)

```cpp
#include <WiFi.h>
#include <HTTPClient.h>
#include <DHT.h>

#define DHTPIN 4
#define DHTTYPE DHT11
DHT dht(DHTPIN, DHTTYPE);

const char* ssid = "seu_wifi";
const char* password = "sua_senha";
const char* api_url = "http://seu_servidor:8080";
const char* username = "meu_arduino";
const char* api_password = "senha_segura";

String access_token = "";
unsigned long last_login = 0;
const unsigned long LOGIN_INTERVAL = 3600000; // 1 hora

void setup() {
  Serial.begin(115200);
  dht.begin();
  WiFi.begin(ssid, password);
  
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nWiFi conectado");
  
  // Login inicial
  login();
}

void loop() {
  // Renovar token se expirou
  if (millis() - last_login > LOGIN_INTERVAL) {
    login();
  }
  
  // Coletar dados
  float temperature = dht.readTemperature();
  float humidity = dht.readHumidity();
  
  if (!isnan(temperature) && !isnan(humidity)) {
    sendData(temperature, humidity);
  }
  
  // Verificar controles pendentes
  checkPendingControls();
  
  delay(300000); // A cada 5 minutos
}

void login() {
  if (WiFi.status() != WL_CONNECTED) return;
  
  HTTPClient http;
  http.begin(String(api_url) + "/login");
  http.addHeader("Content-Type", "application/json");
  
  String payload = "{\"username\":\"" + String(username) + "\",\"password\":\"" + String(api_password) + "\"}";
  int httpCode = http.POST(payload);
  
  if (httpCode == 200) {
    String response = http.getString();
    // Parse JSON para extrair access_token
    // Você pode usar uma biblioteca JSON como ArduinoJson
    Serial.println("Login bem-sucedido!");
    last_login = millis();
  } else {
    Serial.print("Login falhou: ");
    Serial.println(httpCode);
  }
  
  http.end();
}

void sendData(float temp, float humidity) {
  if (WiFi.status() != WL_CONNECTED) return;
  
  HTTPClient http;
  http.begin(String(api_url) + "/eventos/salvar");
  http.addHeader("Content-Type", "application/json");
  http.addHeader("Authorization", "Bearer " + access_token);
  
  char payload[256];
  snprintf(payload, sizeof(payload),
    "{\"temperaturaC\":%.2f,\"umidadeRelativa\":%.2f,\"local\":\"Sala 01\",\"arduino\":\"DHT11_001\"}",
    temp, humidity);
  
  int httpCode = http.POST(payload);
  
  if (httpCode == 201) {
    Serial.println("Dados enviados com sucesso!");
  } else {
    Serial.print("Erro ao enviar dados: ");
    Serial.println(httpCode);
  }
  
  http.end();
}

void checkPendingControls() {
  if (WiFi.status() != WL_CONNECTED) return;
  
  HTTPClient http;
  String url = String(api_url) + "/bipes/ultimo-bipe?arduino=DHT11_001&local=Sala%2001";
  
  http.begin(url);
  http.addHeader("Authorization", "Bearer " + access_token);
  
  int httpCode = http.GET();
  
  if (httpCode == 200) {
    String response = http.getString();
    if (response.length() > 0) {
      Serial.print("Comando recebido: ");
      Serial.println(response);
      // Processar comando aqui
      if (response.indexOf("ligar") >= 0) {
        // Ligar equipamento
      } else if (response.indexOf("desligar") >= 0) {
        // Desligar equipamento
      }
    }
  }
  
  http.end();
}
```

### Fluxo de Operação do Arduino

1. **Conectar ao WiFi**
2. **Fazer login** na API para obter token JWT
3. **Coletar dados** do sensor (temperatura, umidade, etc.)
4. **Enviar evento** para a API via `/eventos/salvar`
5. **Verificar controles pendentes** via `/bipes/ultimo-bipe`
6. **Processar mensagens** de controle recebidas
7. **Renovar token** a cada 1 hora (ou conforme necessário)

---

## 🐳 Docker (Opcional)

### Build da Imagem

```bash
cd sensor-api/docker
docker build -t sensor-api:1.0.0 .
```

### Executar com Docker Compose

```bash
docker-compose up -d
```

Edite `docker-compose.yaml` para configurar variáveis de ambiente.

---

## 📊 Estrutura do Banco de Dados

### Tabelas Principais

- **users** - Usuários registrados
- **roles** - Papéis (ADMIN, USER)
- **eventos** - Eventos/medições de sensores
- **bipes** - Mensagens entre usuários

### User

```sql
CREATE TABLE users (
  user_id UUID PRIMARY KEY,
  username VARCHAR(100) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  email VARCHAR(100),
  local VARCHAR(100),
  arduino VARCHAR(100),
  role_id BIGINT,
  criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Evento

```sql
CREATE TABLE eventos (
  id BIGSERIAL PRIMARY KEY,
  user_id UUID NOT NULL,
  temperatura_c DECIMAL(5,2),
  umidade_relativa DECIMAL(5,2),
  local VARCHAR(100),
  arduino VARCHAR(100),
  data TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(user_id)
);
```

### Bipe

```sql
CREATE TABLE bipes (
  id UUID PRIMARY KEY,
  sender_id UUID NOT NULL,
  receiver_id UUID NOT NULL,
  mensagem TEXT,
  local VARCHAR(100),
  arduino VARCHAR(100),
  data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (sender_id) REFERENCES users(user_id),
  FOREIGN KEY (receiver_id) REFERENCES users(user_id)
);
```

---

## 🧪 Testes

Execute os testes com Maven:

```bash
mvn test
```

---

## 📝 Notas Importantes

### Sobre Arduino e Sincronização de Tempo

Para Arduino **sem relógio RTC** em modo datalogger:

1. Se o WiFi falhar, o Arduino não consegue sincronizar a hora
2. Ao reconectar, envie os dados com timestamp aproximado (baseado no clock do MCU)
3. A API pode receber e salvar os eventos com os timestamps recebidos
4. Considere usar um **módulo RTC** (DS3231) para melhor precisão

### Segurança

- Sempre use HTTPS em produção
- Mantenha as chaves privadas seguras
- Regenere as chaves periodicamente
- Use senhas fortes para usuários
- Configure CORS adequadamente para sua origem
- As senhas são salvas com hash BCrypt

### CORS Configurado

A API aceita requisições de:
- `http://localhost:5173`
- `http://localhost:8081`
- `http://127.0.0.1:5173`

Modifique em `SecurityConfig.java` para adicionar outras origens.

---

## 🤝 Contribuindo

1. Faça um fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/MinhaFeature`)
3. Commit suas mudanças (`git commit -m 'Adiciona MinhaFeature'`)
4. Push para a branch (`git push origin feature/MinhaFeature`)
5. Abra um Pull Request

---

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo LICENSE para mais detalhes.

---

## ✉️ Suporte

Para dúvidas ou problemas, abra uma issue no repositório ou entre em contato com o desenvolvedor.

---

**Desenvolvido por:** Ryan Ribeiro  
**Última atualização:** Fevereiro de 2026  
**Versão:** 1.0.0
