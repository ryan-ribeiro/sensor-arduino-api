#include <Arduino.h>
#include <WiFi.h>
#include <HTTPClient.h>
#include <Arduino_JSON.h>
#include <LiquidCrystal.h>

/*
    Credits to https://github.com/carter-glynn/Morse-Code-Reader/tree/main
    Adaptated from STM32 to Esp32
*/

const int rs = 13, en = 12, d4 = 14, d5 = 27, d6 = 26, d7 = 25;
LiquidCrystal lcd(rs, en, d4, d5, d6, d7);

// ========== Pinos ==========
#define MORSE_CODE_BUTTON 19
#define BOTAO_LER_BIPE 22
#define ANTERIOR 23
#define PROXIMO   32
#define ENVIAR     18
#define CANCELAR   21
#define LED_PIN    2
#define LED_OUT_PIN 4

// ========== Tempos ==========
#define ARRLENGTH    5
#define SIG_DOT      200
#define SIG_DASH     600
#define LETTER_SPACE 1000

// ========== Variáveis de Rede ==========
const char* ssid          = "dev_5G";
const char* password      = "qwer@123";
String username           = "ryan255";
String loginPassword      = "123456";
String accessToken        = "";

const String tipoSensor = "morse";
const String local      = "sala";
const String arduino    = "esp32-01";

// CORRIGIDO: String ao invés de const char* para permitir concatenação
const char* salvarBipeEndpoint      = "http://192.168.1.6:8080/bipes/salvar";
const String ultimoBipeEndpoint      = "http://192.168.1.6:8080/bipes/ultimo-bipe?local="    + local + "&arduino=" + arduino;
const String idUltimoBipeEndpoint    = "http://192.168.1.6:8080/bipes/id-ultimo-bipe?local=" + local + "&arduino=" + arduino;
const String bipesBeforeEndpoint     = "http://192.168.1.6:8080/bipes/before";
const String bipesAfterEndpoint      = "http://192.168.1.6:8080/bipes/after";
const String loginEndpoint           = "http://192.168.1.6:8080/login";

// ========== Mensagem acumulada ==========
String mensagem = "";

// ========== Enumeração dos sinais ==========
typedef enum {
    DOT,
    DASH,
    SPACE,
    FINISH,
    EMPTY
} Code;

// ========== Tabela Morse ==========
typedef struct {
    char morse[ARRLENGTH + 1];
    char character;
} Morse;

// Tabela de conversão Código Morse -> Letras e números
static const Morse morseTable[] = {
    // Letras
    { ".-", 'A' }, { "-...", 'B' }, { "-.-.", 'C' }, { "-..", 'D' }, { ".", 'E' },
    { "..-.", 'F' }, { "--.", 'G' }, { "....", 'H' }, { "..", 'I' }, { ".---", 'J' },
    { "-.-", 'K' }, { ".-..", 'L' }, { "--", 'M' }, { "-.", 'N' }, { "---", 'O' },
    { ".--.", 'P' }, { "--.-", 'Q' }, { ".-.", 'R' }, { "...", 'S' }, { "-", 'T' },
    { "..-", 'U' }, { "...-", 'V' }, { ".--", 'W' }, { "-..-", 'X' }, { "-.--", 'Y' },
    { "--..", 'Z' },
    // Números
    { ".----", '1' }, { "..---", '2' }, { "...--", '3' }, { "....-", '4' }, { ".....", '5' },
    { "-....", '6' }, { "--...", '7' }, { "---..", '8' }, { "----.", '9' }, { "-----", '0' }
};

// ========== Structs ==========
struct LCDMessage {
    char character;
    uint8_t position;
};

struct Bipe {
    String mensagem;
    String local;
    String arduino;
};

// ========== Handles FreeRTOS ==========
QueueHandle_t queue1;
QueueHandle_t queue2_1;
QueueHandle_t queue2_2;
QueueHandle_t queueLCD;
QueueHandle_t queueBipe;
SemaphoreHandle_t BinSemHandle;
SemaphoreHandle_t mutexLCD;
SemaphoreHandle_t mutexMensagem; 

// ========== Funções HTTP ==========

int RequisicaoHttpPOST(String mensagem, const char* serverName) {
    WiFiClient client;
    HTTPClient http;

    http.begin(client, serverName);

    String authHeader = "Bearer " + accessToken;
    http.addHeader("Authorization", authHeader);
    http.addHeader("Content-Type", "application/json");

    String httpRequestData = "{\"tipoSensor\":\"" + tipoSensor + 
                             "\",\"local\":\""    + local      +
                             "\",\"arduino\":\""  + arduino    +
                             "\",\"mensagem\":\""    + mensagem      + 
                             "\"}";
    Serial.println(httpRequestData);

    int httpResponseCode = http.POST(httpRequestData);
    http.end();
    return httpResponseCode;
}

String httpGETRequest(String serverName) {
  WiFiClient client;
  HTTPClient http;
    
  // Your Domain name with URL path or IP address with path
  http.begin(client, serverName);
  
  String authHeader = "Bearer " + String(accessToken);
  http.addHeader("Authorization", authHeader);
  
  // Send HTTP GET request
  int httpResponseCode = http.GET();
  
  String payload = "{}"; 
  
  if (httpResponseCode>0) {
    Serial.print("HTTP Response code: ");
    Serial.println(httpResponseCode);
    payload = http.getString();
  }
  else {
    Serial.print("Error code: ");
    Serial.println(httpResponseCode);
  }
  // Free resources
  http.end();

  // Se retornou um 401, o token exipirou. Então, apenas pega outro.
  // Assumimos então que a API não terá alterações que impeçam esse lógica de funcionar
  if (httpResponseCode == 401) {
    rotinaAccessToken();
  }

  return payload;
}

String login(const String loginEndpoint, String username, String password) {
    WiFiClient client;
    HTTPClient http;

    http.begin(client, loginEndpoint);
    http.addHeader("Content-Type", "application/json");

    String httpRequestData = "{\"username\":\"" + username +
                             "\",\"password\":\"" + password + "\"}";
    Serial.println(httpRequestData);

    int httpResponseCode = http.POST(httpRequestData);
    String payload = "{}";

    if (httpResponseCode == 200) {
        Serial.print("HTTP Response code: ");
        Serial.println(httpResponseCode);
        payload = http.getString();
    } else {
        Serial.print("Error code: ");
        Serial.println(httpResponseCode);
        http.end();
        return "";
    }

    http.end();
    return payload;
}

String getAccessToken(String returnedPayload) {
    Serial.println(returnedPayload);
    JSONVar myObject = JSON.parse(returnedPayload);

    if (JSON.typeof(myObject) == "undefined") {
        Serial.println("Parsing input failed!");
        return "";
    }

    if (myObject.hasOwnProperty("accessToken")) {
        return String((const char*) myObject["accessToken"]);
    }
    return "";
}

String getMensagemBipe(String returnedPayload) {
    Serial.println(returnedPayload);
    JSONVar myObject = JSON.parse(returnedPayload);

    if (JSON.typeof(myObject) == "undefined") {
        Serial.println("Parsing input failed!");
        return "";
    }

    if (myObject.hasOwnProperty("mensagem")) {
        return String((const char*) myObject["mensagem"]);
    }
    return "";
}

// ========== Tasks ==========

/* 
 * Task 1: Lê o botão, realiza debounce, calcula a duração do clique e 
 * identifica se foi Ponto (DOT), Traço (DASH) ou Espaço (SPACE).
 * Envia o resultado para queue1.
 */
void StartTask1(void *pvParameters) {
    Code signal;
    uint32_t pressTime = 0, releaseTime = 0;
    bool lastState = HIGH;

    while (1) {
        bool currentState = digitalRead((uint8_t)MORSE_CODE_BUTTON);

        if (currentState == LOW && lastState == HIGH) {
            pressTime = millis();
        }
        else if (currentState == HIGH && lastState == LOW) {
            releaseTime = millis();
            uint32_t duration = releaseTime - pressTime;

            if (duration > 50) {
                signal = (duration < SIG_DASH) ? DOT : DASH;
                xQueueSend(queue1, &signal, portMAX_DELAY);
            }
        }
        else if (currentState == HIGH && lastState == HIGH) {
            if (pressTime > 0 && (millis() - releaseTime > LETTER_SPACE)) {
                signal = SPACE;
                xQueueSend(queue1, &signal, portMAX_DELAY);
                pressTime = 0;
            }
        }

        lastState = currentState;
        vTaskDelay(pdMS_TO_TICKS(10));
    }
}

/* 
 * Task 2: Recebe da queue1, acende o LED interno simultaneamente 
 * para feedback visual e envia o sinal duplicado para queue2_1 e queue2_2.
 */
void StartTask2(void *pvParameters) {
    Code signal;
    while (1) {
        if (xQueueReceive(queue1, &signal, portMAX_DELAY) == pdPASS) {
            if (signal == DOT || signal == DASH) {
                digitalWrite(LED_PIN, HIGH);
                vTaskDelay(pdMS_TO_TICKS(signal == DOT ? SIG_DOT : SIG_DASH));
                digitalWrite(LED_PIN, LOW);
            }
            xQueueSend(queue2_1, &signal, portMAX_DELAY);
            xQueueSend(queue2_2, &signal, portMAX_DELAY);
        }
    }
}

/* 
 * Task 3: Recebe da queue2_1, acumula pontos e traços num buffer. 
 * Quando recebe um SPACE, decodifica a sequência e imprime a letra via Serial.
 */
void StartTask3(void *pvParameters) {
    Code signal;
    char buffer[ARRLENGTH + 1] = {0};
    uint8_t index    = 0;
    uint8_t qtdChar  = 0;

    bool lastEnviar   = HIGH;
    bool lastCancelar = HIGH;

    while (1) {
        bool enviar   = digitalRead((uint8_t)ENVIAR);
        bool cancelar = digitalRead((uint8_t)CANCELAR);

        if (xQueueReceive(queue2_1, &signal, pdMS_TO_TICKS(10)) == pdPASS) {
            if (signal == DOT && index < ARRLENGTH) {
                buffer[index++] = '.';
            }
            else if (signal == DASH && index < ARRLENGTH) {
                buffer[index++] = '-';
            }
            else if (signal == SPACE) {
                buffer[index] = '\0';

                if (xSemaphoreTake(mutexMensagem, portMAX_DELAY) == pdTRUE) {
                    for (int i = 0; i < 36; i++) {
                        if (strcmp(buffer, morseTable[i].morse) == 0) {
                            char decoded = morseTable[i].character;
                            Serial.print(decoded);

                            LCDMessage msg = { decoded, qtdChar };
                            xQueueSend(queueLCD, &msg, portMAX_DELAY);

                            mensagem += decoded;
                            qtdChar++;
                            break;
                        }
                    }
                    xSemaphoreGive(mutexMensagem);
                }

                if (index > 0) Serial.println();

                index = 0;
                memset(buffer, 0, sizeof(buffer));
            }
        }

        // Cancelar mensagem
        if (xSemaphoreTake(mutexMensagem, portMAX_DELAY) == pdTRUE) {
            if (cancelar == LOW && lastCancelar == HIGH) {
                lcd.clear();
                mensagem = "";
                qtdChar  = 0;
                Serial.println("Mensagem cancelada.");
            }
            xSemaphoreGive(mutexMensagem);
        }
        lastCancelar = cancelar;

        if (xSemaphoreTake(mutexMensagem, portMAX_DELAY) == pdTRUE) {
            if (mensagem.length() > 0 && enviar == LOW && lastEnviar == HIGH) {
                Bipe bipe = { mensagem, local, arduino };
                xQueueSend(queueBipe, &bipe, 0);
                mensagem = "";  // Limpa após enviar
                qtdChar  = 0;
                lcd.clear();
                Serial.println("Mensagem enviada à fila.");
            }
            xSemaphoreGive(mutexMensagem);
        }
        lastEnviar = enviar;
    }
}

/* 
 * Task 4: Recebe da queue2_2 e reproduz a sequência completa no LED externo.
 */
void StartTask4(void *pvParameters) {
    Code signal;
    while (1) {
        if (xQueueReceive(queue2_2, &signal, portMAX_DELAY) == pdPASS) {
            if (signal == DOT || signal == DASH) {
                digitalWrite(LED_OUT_PIN, HIGH);
                vTaskDelay(pdMS_TO_TICKS(signal == DOT ? SIG_DOT : SIG_DASH));
                digitalWrite(LED_OUT_PIN, LOW);
                vTaskDelay(pdMS_TO_TICKS(SIG_DOT));
            }
            else if (signal == SPACE) {
                vTaskDelay(pdMS_TO_TICKS(LETTER_SPACE));
            }
        }
    }
}

// Task que escreve no LCD os caracteres na Escrita
void StartTaskLCDWriting(void *pvParameters) {
    LCDMessage msg;
    while (1) {
        if (xQueueReceive(queueLCD, &msg, portMAX_DELAY) == pdPASS) {
            // Aguarda o mutex antes de acessar o LCD
            if (xSemaphoreTake(mutexLCD, portMAX_DELAY) == pdTRUE) {
                int row = msg.position / 16;
                lcd.setCursor(msg.position % 16, row);
                lcd.print(msg.character);
                xSemaphoreGive(mutexLCD);
            }
        }
    }
}

void StartTaskSendBipe(void *pvParameters) {
    // TODO: como adormecer essa task até o botão  ENVIAR for pressionado?
    Bipe bipe;
    while (1) {
        if (xQueueReceive(queueBipe, &bipe, portMAX_DELAY) == pdPASS) {
            if (WiFi.status() == WL_CONNECTED) {
                int code = RequisicaoHttpPOST(bipe.mensagem, salvarBipeEndpoint);
                if (code == 401) {
                    rotinaAccessToken();
                    // Tenta reenviar após renovar o token
                    code = RequisicaoHttpPOST(bipe.mensagem, salvarBipeEndpoint);
                }
                Serial.print("Bipe enviado, HTTP code: ");
                Serial.println(code);
            } else {
                Serial.println("WiFi desconectado, bipe não enviado.");
            }
        }
    }
}

void StartTaskLerBipe(void *pvParameters) {
    // Lista local simulando bipes recebidos — substitua por busca HTTP real
    // quando implementar o endpoint de leitura
    const char* bipesRecebidos[] = {
        "OLA MUNDO",
        "TESTE MORSE",
        "ESP32 OK"
    };
    const int totalBipes = 3;
    int indiceBipe = 0;

    // Pegar ultimo bipe e mostrar /bipes/ultimo-bipe?local=sala&arduino=esp32-01
    // Pegar o id desse último bipe bipes/id-ultimo-bipe?local=sala&arduino=esp32-01
    // Navegar pelos endpoints /bipes/before?id={} e /bipes/after?id={}

    bool lastLer      = HIGH;
    bool lastAnterior = HIGH;
    bool lastProximo  = HIGH;
    bool modoLeitura  = false; // Controla se estamos no modo de leitura
    int idAtualBipe = 0;

    while (1) {
        bool lerBipe  = digitalRead((uint8_t)BOTAO_LER_BIPE);
        bool anterior = digitalRead((uint8_t)ANTERIOR);
        bool proximo  = digitalRead((uint8_t)PROXIMO);
        String ultimoBipeRecebido;

        // Botão LER: entra/sai do modo de leitura
        if (lerBipe == LOW && lastLer == HIGH) {
            modoLeitura = !modoLeitura;
            
            if (modoLeitura) {
                ultimoBipeRecebido = httpGETRequest(ultimoBipeEndpoint);
                idAtualBipe = httpGETRequest(idUltimoBipeEndpoint).toInt(); // Pega o ID do último bipe recebido
            }

            if (xSemaphoreTake(mutexLCD, portMAX_DELAY) == pdTRUE) {
                lcd.clear();
                if (modoLeitura) {
                    lcd.setCursor(0, 0);
                    lcd.print("Ultimo bipe:");
                    lcd.setCursor(0, 1);
                    lcd.print(ultimoBipeRecebido);
                } else {
                    lcd.setCursor(0, 0);
                    lcd.print("Modo escrita");
                }
                xSemaphoreGive(mutexLCD);
            }
        }
        lastLer = lerBipe;

        // Navegação só funciona no modo leitura
        if (modoLeitura) {

            // Botão PROXIMO: avança para o próximo bipe
            if (proximo == LOW && lastProximo == HIGH) {
                // Pega o próximo bipe após o ID atual
                ultimoBipeRecebido = httpGETRequest(bipesAfterEndpoint+"?id=" + String(idAtualBipe)); 
                idAtualBipe++;

                if (xSemaphoreTake(mutexLCD, portMAX_DELAY) == pdTRUE) {
                    lcd.clear();
                    lcd.setCursor(0, 0);
                    lcd.print("Bipe recebido:");
                    lcd.setCursor(0, 1);
                    lcd.print(ultimoBipeRecebido);
                    xSemaphoreGive(mutexLCD);
                }
                Serial.print("Proximo bipe: ");
                Serial.println(bipesRecebidos[indiceBipe]);
            }
            lastProximo = proximo;

            // Botão ANTERIOR: volta para o bipe anterior
            if (anterior == LOW && lastAnterior == HIGH) {
                // Pega o próximo bipe após o ID atual
                ultimoBipeRecebido = httpGETRequest(bipesBeforeEndpoint+"?id=" + String(idAtualBipe)); 
                idAtualBipe--;

                if (xSemaphoreTake(mutexLCD, portMAX_DELAY) == pdTRUE) {
                    lcd.clear();
                    lcd.setCursor(0, 0);
                    lcd.print("Bipe recebido:");
                    lcd.setCursor(0, 1);
                    lcd.print(ultimoBipeRecebido);
                    xSemaphoreGive(mutexLCD);
                }
                Serial.print("Bipe anterior: ");
                Serial.println(ultimoBipeRecebido);
            }
            lastAnterior = anterior;
        }

        vTaskDelay(pdMS_TO_TICKS(10)); // Debounce e libera CPU
    }
}

// ========== Setup ==========

void setup() {
    Serial.begin(115200);

    pinMode(MORSE_CODE_BUTTON, INPUT_PULLUP);
    pinMode(LED_PIN,    OUTPUT);
    pinMode(LED_OUT_PIN,OUTPUT);
    pinMode(ENVIAR,   INPUT_PULLUP);
    pinMode(CANCELAR, INPUT_PULLUP);
    pinMode(BOTAO_LER_BIPE, INPUT_PULLUP);
    pinMode(ANTERIOR, INPUT_PULLUP);
    pinMode(PROXIMO, INPUT_PULLUP);

    WiFi.begin(ssid, password);
    Serial.print("Conectando ao WiFi");
    while (WiFi.status() != WL_CONNECTED) {
        vTaskDelay(pdMS_TO_TICKS(500));
        Serial.print(".");
    }
    Serial.println("\nWiFi conectado!");

    rotinaAccessToken();

    BinSemHandle = xSemaphoreCreateBinary();
    xSemaphoreGive(BinSemHandle);

    mutexLCD = xSemaphoreCreateMutex();
    mutexMensagem = xSemaphoreCreateMutex();

    queue1    = xQueueCreate(16, sizeof(Code));
    queue2_1  = xQueueCreate(16, sizeof(Code));
    queue2_2  = xQueueCreate(16, sizeof(Code));
    queueLCD  = xQueueCreate(16, sizeof(LCDMessage));
    queueBipe = xQueueCreate(4,  sizeof(Bipe));

    lcd.begin(16, 2);

    // Core 0: tarefas de rede (WiFi é nativo do core 0)
    xTaskCreatePinnedToCore(StartTaskSendBipe, "Task HTTP", 6144, NULL, 1, NULL, 0);
    xTaskCreatePinnedToCore(StartTaskLerBipe,    "Task Ler", 4096, NULL, 1, NULL, 0);

    // Core 1: tarefas de interface (botões, LCD, LEDs)
    xTaskCreatePinnedToCore(StartTask1,          "Task1",    2048, NULL, 1, NULL, 1);
    xTaskCreatePinnedToCore(StartTask2,          "Task2",    2048, NULL, 1, NULL, 1);
    xTaskCreatePinnedToCore(StartTask3,          "Task3",    2048, NULL, 1, NULL, 1);
    xTaskCreatePinnedToCore(StartTask4,          "Task4",    2048, NULL, 1, NULL, 1);
    xTaskCreatePinnedToCore(StartTaskLCDWriting, "Task LCD", 6144, NULL, 1, NULL, 1);

    xTaskCreatePinnedToCore([](void*){
        while(1) {
            Serial.print("Free heap: ");
            Serial.println(ESP.getFreeHeap());
            vTaskDelay(pdMS_TO_TICKS(5000)); // A cada 5 segundos
        }
    }, "Heap Monitor", 2048, NULL, 1, NULL, 1);
}

void loop() {
    vTaskDelete(NULL);
}

void rotinaAccessToken() {
    String payload = login(loginEndpoint, username, loginPassword);
    if (payload != "") {
        accessToken = getAccessToken(payload);
        Serial.print("Access Token: ");
        Serial.println(accessToken);
    }
}