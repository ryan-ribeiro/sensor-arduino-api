#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include <Arduino_JSON.h>

const char* ssid = "";
const char* password = "";

const char* accessToken = "";

//Your Domain name with URL path or IP address with path
const char* serverName = "http://192.168.1.8:8080/eventos/1302";

// the following variables are unsigned longs because the time, measured in
// milliseconds, will quickly become a bigger number than can be stored in an int.
unsigned long lastTime = 0;
// Set timer to 5 seconds (5000)
unsigned long timerDelay = 5000;

String sensorReadings;
float floatRetorno;
String stringRetorno;
bool booleanRetorno;

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
    if(WiFi.status() == WL_CONNECTED){
      sensorReadings = httpGETRequest(serverName);
      JSONVar myObject = JSON.parse(sensorReadings);
  
      if (JSON.typeof(myObject) == "undefined") {
        Serial.println("Parsing input failed!");
        return;
      }
    
      JSONVar keys = myObject.keys();
    
      for (int i = 0; i < keys.length(); i++) {
        JSONVar value = myObject[keys[i]];
        String keyName = (const char*)keys[i];

        Serial.print("Chave: ");
        Serial.print(keyName);
        Serial.print(" | ");

        if (JSON.typeof(value) == "undefined") {
          Serial.println("Parsing input failed!");
          return;
        }

        // E se tiver mais de um JSON dentro? 
        // Se o tipo for "object", este loop NÃO entra nele.
        if (JSON.typeof(value) == "object") {
          Serial.println("-> Contém um objeto aninhado (precisa de novo loop)");
        }

        // 1. Tratando NÚMEROS
        if (JSON.typeof(value) == "number") {
          floatRetorno = (double)value; 
          Serial.print("float: ");
          Serial.println(floatRetorno);
        }

        // 2. Tratando STRINGS
        if (JSON.typeof(value) == "string") {
          // O cast correto é (const char*)
          stringRetorno = (const char*)value; 
          Serial.print("String: ");
          Serial.println(stringRetorno);
        }

        // 3. Tratando BOOLEANOS
        if (JSON.typeof(value) == "boolean") {
          booleanRetorno = (bool)value;
          Serial.print("bool: ");
          Serial.println(booleanRetorno ? "true" : "false");
        }

        if (JSON.typeof(value) == "null") {
          Serial.println("null");
        }
        
      }
      Serial.println("Fim da request.\n\n");
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
  
  // If you need Node-RED/server authentication, insert user and password below
  //http.setAuthorization("REPLACE_WITH_SERVER_USERNAME", "REPLACE_WITH_SERVER_PASSWORD");

  // If you are using Bearer Token, then you will need also:
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
