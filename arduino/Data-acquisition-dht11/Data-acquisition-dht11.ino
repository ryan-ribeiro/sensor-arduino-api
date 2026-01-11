#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include <Arduino_JSON.h>
#include "DHT.h"

#define DHTTYPE DHT11
#define DHTPIN 5
DHT dht(DHTPIN, DHTTYPE);

// Configurações da rede Wi-Fi conectada
const char* ssid = "dev";
const char* password = "qwer@123";


// Configurações de login do usuário cadastrado na API
String username = "ryan";
String loginPassword = "123";

// Token de acesso para a API
String accessToken = "";
const char* loginEndpoint = "http://192.168.1.8:8080/login";

// Endereço para o endpoint /salvar evento
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

  String returnedPayload = "";
  // Chamar login
  while(returnedPayload == "") {
    returnedPayload = login(loginEndpoint, username, loginPassword);
  }
  // Extrair accessToken
  accessToken = getAcessToken(returnedPayload);

  if (accessToken == "") {
    Serial.println("Não foi possível pegar o access token. Saindo...");
    Serial.print("accessToken: ");
    Serial.println(accessToken);
    abort();
  }

  Serial.println("Timer configurado para 5 segundos (variável timerDelay), levará 5 segundos antes de publicar a primeira leitura.");
}

void loop() {
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


String login(const char* loginEndpoint, String username, String password) {
  WiFiClient client;
  HTTPClient http;

  http.begin(client, loginEndpoint);

  http.addHeader("Content-Type", "application/json");
  String httpRequestData = "{\"username\":\"" + username + "\",\"password\":\""+ password + "\"}";

  Serial.println(httpRequestData);

  int httpResponseCode = http.POST(httpRequestData);

  String payload = "{}"; 
  
  if (httpResponseCode == 200) {
    Serial.print("HTTP Response code: ");
    Serial.println(httpResponseCode);
    payload = http.getString();
  }
  else {
    Serial.print("Error code: ");
    Serial.println(httpResponseCode);
    http.end();
    return "";
  }

  http.end();
  return payload;
}

String getAcessToken(String returnedPayload) {
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