#include <Arduino.h>

/*
    Credits to https://github.com/carter-glynn/Morse-Code-Reader/tree/main
    Adaptated from STM32 to Esp32
*/

// Definições de pinos para ESP32
#define BUTTON_PIN 19      
#define LED_PIN 2         // LED interno da placa
#define LED_OUT_PIN 4     // LED secundário externo (conecte ao GPIO 4)

// Definições de tempos (em milissegundos) para uma entrada humana mais realista
#define ARRLENGTH 5
#define SIG_DOT 200       // Tempo máximo para ser considerado um ponto (DOT)
#define SIG_DASH 600      // Tempo máximo para traço (DASH)
#define LETTER_SPACE 1000 // Tempo de inatividade para concluir uma letra

// Enumeração dos tipos de sinais
typedef enum { 
    DOT, 
    DASH, 
    SPACE, 
    FINISH, 
    EMPTY 
} Code;

// Estrutura para a tabela Morse
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

// Handles para Filas e Semáforos do FreeRTOS
QueueHandle_t queue1;
QueueHandle_t queue2_1;
QueueHandle_t queue2_2;
SemaphoreHandle_t BinSemHandle;

/* 
 * Task 1: Lê o botão, realiza debounce, calcula a duração do clique e 
 * identifica se foi Ponto (DOT), Traço (DASH) ou Espaço (SPACE).
 * Envia o resultado para queue1.
 */
void StartTask1(void *pvParameters) {
    Code signal;
    uint32_t pressTime = 0, releaseTime = 0;
    bool lastState = HIGH;
    
    while(1) {
        bool currentState = digitalRead(BUTTON_PIN);
        
        if (currentState == LOW && lastState == HIGH) { 
            // Botão acabou de ser pressionado
            pressTime = millis();
        } 
        else if (currentState == HIGH && lastState == LOW) { 
            // Botão acabou de ser solto
            releaseTime = millis();
            uint32_t duration = releaseTime - pressTime;
            
            if (duration > 50) { // Tratamento básico de Debounce
                signal = (duration < SIG_DASH) ? DOT : DASH;
                xQueueSend(queue1, &signal, portMAX_DELAY);
            }
        } 
        else if (currentState == HIGH && lastState == HIGH) { 
            // Botão ocioso (não pressionado)
            if (pressTime > 0 && (millis() - releaseTime > LETTER_SPACE)) {
                signal = SPACE;
                xQueueSend(queue1, &signal, portMAX_DELAY);
                pressTime = 0; // Reset para evitar envios contínuos de SPACE
            }
        }
        
        lastState = currentState;
        vTaskDelay(pdMS_TO_TICKS(10)); // Delay para não travar a CPU e auxiliar no debounce
    }
}

/* 
 * Task 2: Recebe da queue1, acende o LED interno simultaneamente 
 * para feedback visual e envia o sinal duplicado para queue2_1 e queue2_2.
 */
void StartTask2(void *pvParameters) {
    Code signal;
    while(1) {
        if (xQueueReceive(queue1, &signal, portMAX_DELAY) == pdPASS) {
            if (signal == DOT || signal == DASH) {
                digitalWrite(LED_PIN, HIGH);
                vTaskDelay(pdMS_TO_TICKS(signal == DOT ? SIG_DOT : SIG_DASH));
                digitalWrite(LED_PIN, LOW);
            }
            // Distribui para as próximas tarefas
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
    uint8_t index = 0;
    
    while(1) {
        if (xQueueReceive(queue2_1, &signal, portMAX_DELAY) == pdPASS) {
            if (signal == DOT && index < ARRLENGTH) {
                buffer[index++] = '.';
            } 
            else if (signal == DASH && index < ARRLENGTH) {
                buffer[index++] = '-';
            } 
            else if (signal == SPACE) {
                buffer[index] = '\0';
                
                // Busca na tabela Morse
                for (int i = 0; i < 26; i++) {
                    if (strcmp(buffer, morseTable[i].morse) == 0) {
                        Serial.print(morseTable[i].character);
                        break;
                    }
                }
                
                if (index > 0) {
                    Serial.println(); // Pula linha após decodificar a letra
                }
                
                // Reseta o buffer para a próxima letra
                index = 0;
                memset(buffer, 0, sizeof(buffer));
            }
        }
    }
}

/* 
 * Task 4: Recebe da queue2_2 e reproduz a sequência completa no LED externo.
 */
void StartTask4(void *pvParameters) {
    Code signal;
    while(1) {
        if (xQueueReceive(queue2_2, &signal, portMAX_DELAY) == pdPASS) {
            if (signal == DOT || signal == DASH) {
                digitalWrite(LED_OUT_PIN, HIGH);
                vTaskDelay(pdMS_TO_TICKS(signal == DOT ? SIG_DOT : SIG_DASH));
                digitalWrite(LED_OUT_PIN, LOW);
                vTaskDelay(pdMS_TO_TICKS(SIG_DOT)); // Espaço pequeno entre as piscadas da mesma letra
            } 
            else if (signal == SPACE) {
                vTaskDelay(pdMS_TO_TICKS(LETTER_SPACE)); // Espaço maior entre letras
            }
        }
    }
}

// Configuração inicial do ESP32
void setup() {
    Serial.begin(115200);
    
    // Configura os pinos
    pinMode(BUTTON_PIN, INPUT_PULLUP);
    pinMode(LED_PIN, OUTPUT);
    pinMode(LED_OUT_PIN, OUTPUT);

    // Cria semáforo (embora não esteja sendo usado ativamente na lógica refatorada, foi mantido do original)
    BinSemHandle = xSemaphoreCreateBinary();
    xSemaphoreGive(BinSemHandle);

    // Cria as filas com capacidade para 16 itens do tipo 'Code'
    queue1 = xQueueCreate(16, sizeof(Code));
    queue2_1 = xQueueCreate(16, sizeof(Code));
    queue2_2 = xQueueCreate(16, sizeof(Code));

    // Inicializa as Tasks do FreeRTOS
    // Parâmetros: Função, Nome, Tamanho da Pilha, Parâmetro, Prioridade, Handle
    xTaskCreate(StartTask1, "Task1", 2048, NULL, 1, NULL);
    xTaskCreate(StartTask2, "Task2", 2048, NULL, 1, NULL);
    xTaskCreate(StartTask3, "Task3", 2048, NULL, 1, NULL);
    xTaskCreate(StartTask4, "Task4", 2048, NULL, 1, NULL);
}

void loop() {
    // No ESP32 as tarefas FreeRTOS rodam de forma independente nos cores.
    // O loop principal pode ser deletado para liberar memória/recursos.
    vTaskDelete(NULL);
}