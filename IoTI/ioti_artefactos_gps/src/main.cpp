#include <WiFi.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>
#include <TimeLib.h>

// ---- Config WiFi ----
const char *ssid = "Wokwi-GUEST";
const char *password = "";

// ---- Config MQTT ----
const char* mqtt_server = "192.168.0.109";  // -- ip servidor o localHost
const int mqtt_port = 1883;

// ---- DATOS DEL DISPOSITIVO
const int id_dispositivo = 6;
const char* mqtt_user = "gps001";             // usuario creado con mosquitto_passwd
const char* mqtt_pass = "user01";             // contraseña
String topic = "gps/" + String(id_dispositivo) + "/telemetria";
const char* mqtt_topic = topic.c_str();  // tópico para publicar


WiFiClient espClient;
PubSubClient client(espClient);

// ---- Variables de simulación ----
float baseLat = -9.2954;   // Tingo María aprox
float baseLon = -75.9981;
float latitude = baseLat;
float longitude = baseLon;
int batteryLevel = 100;
bool externalPower = true;

unsigned long lastMsg = 0;
char msg[512];

void reconnect() {
  while (!client.connected()) {
    Serial.print("🔄 Intentando conexión MQTT...");
    if (client.connect("gps001-client", mqtt_user, mqtt_pass)) {
      Serial.println("✅ Conectado a MQTT");
    } else {
      Serial.print("❌ fallo, rc=");
      Serial.print(client.state());
      Serial.println(" → reintentando en 5 segundos");
      delay(5000);
    }
  }
}

void setup() {
  Serial.begin(115200);

  WiFi.begin(ssid, password);
  Serial.print("Conectando a WiFi...");
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\n✅ WiFi conectado");

  client.setServer(mqtt_server, mqtt_port);
}

void loop() {
  if (!client.connected()) {
    reconnect();
  }
  client.loop();

  unsigned long now = millis();
  if (now - lastMsg > 5000) {  // cada 5 seg
    lastMsg = now;

    // 📡 Simular movimiento alrededor de Tingo María
    latitude = baseLat + random(-50, 50) / 10000.0;
    longitude = baseLon + random(-50, 50) / 10000.0;

    // 🔋 Simular batería
    if (externalPower) {
      batteryLevel += 2;
      if (batteryLevel > 100) batteryLevel = 100;
    } else {
      batteryLevel -= 1;
      if (batteryLevel < 0) batteryLevel = 0;
    }

    // Generar timestamp ISO
    char timestamp[25];
    sprintf(timestamp, "%04d-%02d-%02dT%02d:%02d:%02d",
            year(), month(), day(), hour(), minute(), second());

    // 📦 JSON
    StaticJsonDocument<512> doc;
    doc["gpsDeviceId"] = id_dispositivo;
    doc["latitud"] = latitude;
    doc["longitud"] = longitude;
    doc["fechaHora"] = timestamp;
    doc["speed"] = random(20, 80);
    doc["estadoEncendido"] = externalPower;

    JsonObject extra = doc.createNestedObject("extraData");
    extra["bateria"] = batteryLevel;
    extra["fuente"] = externalPower ? "Corriente" : "Bateria";
    //extra["temperatura"] = random(25, 35); // temperatura simulada
    //extra["altitud"] = 650;                // altitud base Tingo María

    String payload;
    serializeJson(doc, payload);

    Serial.println("📤 Publicando a MQTT:");
    Serial.println(payload);

    client.publish(mqtt_topic, payload.c_str());
  }
}
