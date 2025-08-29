#include <WiFi.h>
#include <ArduinoJson.h>
#include <HTTPClient.h>

// Credenciales WiFi para Wokwi
const char* ssid = "Wokwi-GUEST";
const char* password = "";

// Endpoint API (servidor Axum en entorno local)
const char* serverName = "http://192.168.0.104:3005/gps";

// Simular datos GPS
float latitude = 19.4326; // Latitud inicial (ej., Ciudad de México)
float longitude = -99.1332; // Longitud inicial
float speed = 0.0; // Velocidad en km/h (0 para electrodoméstico fijo)
int fuelLevel = 75; // Nivel simulado de "combustible" (nivel de energía %)

// Simulación de batería y energía
int batteryLevel = 85; // Nivel inicial de batería (0-100%)
const int powerPin = 33; // Pin GPIO para interruptor de energía externa
bool externalPower = false; // Estado actual de la fuente de energía
String powerSource = "Batería"; // "Externa" o "Batería"

// Tasas de carga/descarga (por ciclo de loop, cada 5s)
const int chargeRate = 5; // +5% por 5s al cargar
const int dischargeRate = 2; // -2% por 5s en batería

// Fuerza de señal simulada
int signalStrength = 92; // 0-100%

// Historial de ubicaciones (hasta 3 entradas)
String locationHistory[3]; // Almacena cadenas JSON para ubicaciones
int locationIndex = 0; // Índice actual en el historial
int locationCount = 0; // Número de ubicaciones almacenadas

// Simular timestamps
unsigned long lastUpdateTime = 0;
String statuses[] = {"Pago validado", "Ubicación confirmada", "Conexión establecida"};
int statusIndex = 0;

void setup() {
  Serial.begin(115200);
  Serial.println("Iniciando Rastreador GPS ESP32 con Simulación de Batería...");

  // Configurar pin de energía (botón) con pull-up interno
  pinMode(powerPin, INPUT_PULLUP);

  // Conectar a WiFi
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.println("Conectando a WiFi...");
  }
  Serial.println("Conectado a WiFi");
  Serial.print("Dirección IP: ");
  Serial.println(WiFi.localIP());
}

void loop() {
  // Verificar estado de energía externa (botón presionado = LOW = externa conectada)
  externalPower = (digitalRead(powerPin) == LOW);

  // Simular gestión de energía
  if (externalPower) {
    powerSource = "Externa";
    if (batteryLevel < 100) {
      batteryLevel += chargeRate;
      if (batteryLevel > 100) batteryLevel = 100;
      Serial.println("Cargando batería...");
    } else {
      Serial.println("Batería llena, usando solo energía externa.");
    }
  } else {
    powerSource = "Batería";
    if (batteryLevel > 0) {
      batteryLevel -= dischargeRate;
      if (batteryLevel < 0) batteryLevel = 0;
      Serial.println("Usando batería...");
    } else {
      Serial.println("Batería vacía! Dispositivo apagándose.");
    }
  }

  // Proceder solo si hay energía
  if (externalPower || batteryLevel > 0) {
    // Simular movimiento GPS (mínimo para electrodoméstico fijo)
    latitude += random(-1, 1) / 100000.0; // Cambio muy pequeño (errores de GPS)
    longitude += random(-1, 1) / 100000.0;
    speed = 0.0; // Siempre 0 para dispositivo fijo
    fuelLevel = random(70, 80); // Nivel entre 70-80% (ej., nivel de energía)
    signalStrength = random(80, 100); // Señal entre 80-100%

    // Generar timestamp (ej., "10:30 AM")
    unsigned long currentTime = millis();
    int secondsSinceLast = (currentTime - lastUpdateTime) / 1000;
    String lastUpdate = String(secondsSinceLast) + " segundos atrás";
    if (secondsSinceLast < 1) lastUpdate = "ahora";
    lastUpdateTime = currentTime;

    // Crear entrada de ubicación
    StaticJsonDocument<200> locationDoc;
    locationDoc["lat"] = latitude;
    locationDoc["lng"] = longitude;
    locationDoc["timestamp"] = "10:" + String(30 - (locationIndex * 5)) + " AM"; // Tiempo mock
    locationDoc["status"] = statuses[statusIndex];
    statusIndex = (statusIndex + 1) % 3; // Ciclar por estados

    String locationJson;
    serializeJson(locationDoc, locationJson);
    locationHistory[locationIndex] = locationJson;
    locationIndex = (locationIndex + 1) % 3; // Ciclar de 0-2
    if (locationCount < 3) locationCount++;

    // Crear payload JSON principal
    StaticJsonDocument<512> doc;
    doc["id"] = "ART-001";
    doc["name"] = "Rastreador GPS Electrodoméstico";
    doc["type"] = "Appliance Tracker";
    doc["status"] = WiFi.status() == WL_CONNECTED ? "online" : "offline";
    JsonArray locations = doc.createNestedArray("locations");
    for (int i = 0; i < locationCount; i++) {
      int idx = (locationIndex - 1 - i + 3) % 3; // Obtener ubicaciones en orden inverso
      if (locationHistory[idx] != "") {
        StaticJsonDocument<200> loc;
        deserializeJson(loc, locationHistory[idx]);
        locations.add(loc);
      }
    }
    doc["battery"] = batteryLevel;
    doc["signal"] = signalStrength;
    doc["lastUpdate"] = lastUpdate;
    JsonObject data = doc.createNestedObject("data");
    data["velocidad"] = String(speed) + " km/h";
    data["combustible"] = String(fuelLevel) + "%";

    String requestBody;
    serializeJson(doc, requestBody);

    // Imprimir en Serial Monitor para pruebas
    Serial.println("Datos GPS Simulados:");
    Serial.println(requestBody);

    // Enviar al backend
    if (WiFi.status() == WL_CONNECTED) {
      HTTPClient http;
      http.begin(serverName);
      http.addHeader("Content-Type", "application/json");
      int httpResponseCode = http.POST(requestBody);
      if (httpResponseCode > 0) {
        String response = http.getString();
     //   Serial.println("Código de Respuesta HTTP: " + Sting(httpResponseCode));
        Serial.println("Respuesta: " + response);
      } else {
        Serial.println("Error en la solicitud HTTP: " + String(httpResponseCode));
      }
      http.end();
    }
  } else {
    Serial.println("No hay energía disponible.");
  }

  Serial.print("Nivel de Batería: ");
  Serial.print(batteryLevel);
  Serial.print("% | Fuente de Energía: ");
  Serial.println(powerSource);

  delay(5000); // Actualizar cada 5 segundos
}