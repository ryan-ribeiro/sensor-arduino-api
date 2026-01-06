#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include <Arduino_JSON.h>

const char* ssid = "";
const char* password = "";

const char* accessToken = "eyJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJzZW5zb3ItYXBpIiwic3ViIjoiYzMyZDY1ODAtNWY0ZS00ZGNkLWI0YmEtY2U4Zjk5YzMzM2RjIiwiZXhwIjoxNzY3NzE0MzMyLCJpYXQiOjE3Njc2NjAzMzIsInNjb3BlIjoiVVNFUiJ9.GDww2EGlTc82hdC7_409GYA6Uq_J9NvVKkTpZb66ColhWd_sbEtwnljjejiOHHObPfA5Vft4GSRvWY3Xp0fxr7bfKjZwNXuqUiXB1Vx4ADr3Rm7s8xGRkvHoDVgjfDJGMDSzWVi-e-zZnuMTY0-KdVcgS4zt-GWSjm7W6vBHX-3drk0Xzu0HnZm8CPy_USmUGq1B-PfGq3LeO2_RmOZEucSkY2do7lHvGWPI_ml3iMd-yajTCmyY4iToV1QJKAXWGUqJUQw6ecxSJA1l4E9Yq9EfUOdFYokxM719xIXRCQq_uZ8mzI-QjNi1D6JICanNdobYTBSFbVPjMEBeBvPK_A";
const char* dataUltimoEventoEndpoint = "http://192.168.1.8:8080/eventos/data-ultimo-evento?arduino=esp32&tipoSensor=dht11&local=home";

// the following variables are unsigned longs because the time, measured in
// milliseconds, will quickly become a bigger number than can be stored in an int.
unsigned long lastTime = 0;
unsigned long timerDelay = 5000;

String eventoRetornado;
float eventoRetornadoArr[3];

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