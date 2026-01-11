#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include <Arduino_JSON.h>

// Configurações da rede Wi-Fi conectada
const char* ssid = "dev";
const char* password = "qwer@123";

// Configurações de login do usuário cadastrado na API
String username = "ryan";
String loginPassword = "123";

// Token de acesso para a API
String accessToken = "";
const char* loginEndpoint = "http://192.168.1.8:8080/login";

// Endereço para o endpoint /data-ultimo-evento
const char* dataUltimoEventoEndpoint = "http://192.168.1.8:8080/eventos/data-ultimo-evento?arduino=esp32&tipoSensor=dht11&local=home";

// the following variables are unsigned longs because the time, measured in
// milliseconds, will quickly become a bigger number than can be stored in an int.
unsigned long lastTime = 0;
unsigned long timerDelay = 5000;

void setup() {
  Serial.begin(115200);

  WiFi.begin(ssid, password);
  Serial.println("Connecting");
  while(WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("");
  Serial.print("Connected to WiFi network with IP Address: ");
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
 
  Serial.println("Timer set to 5 seconds (timerDelay variable), it will take 5 seconds before publishing the first reading.");
}

void loop() {
  if ((millis() - lastTime) > timerDelay) {
    //Check WiFi connection status
    if(WiFi.status()== WL_CONNECTED){
      //Send an HTTP GET request every 5 seconds
      String DataHoraUltimoEvento = httpGETRequest(dataUltimoEventoEndpoint);
      Serial.println("Data e hora do ultimo evento:");
      Serial.println(DataHoraUltimoEvento);
    }
    else {
      Serial.println("WiFi Disconnected");
      setup();
    }
    lastTime = millis();
  }
}

String httpGETRequest(const char* serverName) {
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

  return payload;
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