#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include "DHT.h"

#define DHTTYPE DHT11
#define DHTPIN 5
DHT dht(DHTPIN, DHTTYPE);

const char* ssid = "";
const char* password = "";

const char* accessToken = "";

// Seu nome de domínio com caminho de URL ou endereço IP com caminho
const char* serverName = "http://192.168.1.8:8080/eventos/salvar";

unsigned long timerDelay = 5000; // Timer set to 5 seconds (5000)
unsigned long lastTime = 0;

String tipoSensor = "DHT11";
String local = "Quarto 1";
String arduino = "Atmega2560";

void setup() {
  Serial.begin(115200);

  dht.begin();

  WiFi.begin(ssid, password);
  Serial.println("Conectando");

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  
  Serial.println("");
  Serial.print("Conectado à rede WiFi com endereço IP: ");
  Serial.println(WiFi.localIP());

  Serial.println("Timer configurado para 5 segundos (variável timerDelay), levará 5 segundos antes de publicar a primeira leitura.");
}

void loop() {
  float umidade = 32.70;
  float temperatura = 25.70;
  if (WiFi.status() == WL_CONNECTED) {
    umidade = dht.readHumidity();
    temperatura = dht.readTemperature();
  } else {
    Serial.println("WiFi desconectado");
    setup();
  }

  // Testa se os dados são inválidos
  if(isnan(umidade) || isnan(temperatura)) {
    Serial.println("Falha ao ler dados pelo dht11");
  } else {
    Serial.print("Umidade: ");
    Serial.print(umidade);
    Serial.println(" %");
    Serial.print("Temperatura: ");
    Serial.print(temperatura);
    Serial.println(" °C");
    if ((millis() - lastTime) > timerDelay) {
        String dado1 = String(umidade, 2);
        String dado2 = String(temperatura, 2);

        int httpResponseCode = RequisicaoHttpPOST(dado1, serverName);  
        Serial.print("Código de resposta HTTP: ");
        Serial.println(httpResponseCode);

        httpResponseCode = RequisicaoHttpPOST(dado2, serverName);
        Serial.print("Código de resposta HTTP: ");
        Serial.println(httpResponseCode);
        lastTime = millis();
    }
  }
}

int RequisicaoHttpPOST(String dados, const char* serverName) {
  WiFiClient client;
  HTTPClient http;

  http.begin(client, serverName);

  // Se você precisar de autenticação Node-RED/servidor, insira usuário e senha abaixo
  // http.setAuthorization("SEU_USUÁRIO", "SUA_SENHA");3

  // If you are using Bearer Token, then you will need also:
  String authHeader = "Bearer " + String(accessToken);
  http.addHeader("Authorization", authHeader);

  http.addHeader("Content-Type", "application/json");
  String httpRequestData = "{\"tipoSensor\":\"" + tipoSensor + "\",\"local\":\"" + local + 
  "\",\"arduino\":\"" + arduino + "\",\"dados\":\"" + dados + "\"}";

  Serial.println(httpRequestData);

  int httpResponseCode = http.POST(httpRequestData);

  // Se você precisar de uma solicitação HTTP com um tipo de conteúdo: text/plain
  // http.addHeader("Content-Type", "text/plain");

  http.end();
  return httpResponseCode;
}
