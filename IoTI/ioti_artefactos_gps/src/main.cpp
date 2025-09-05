#include <WiFi.h>
#include <ArduinoJson.h>
#include <HTTPClient.h>
#include <SPI.h>
#include <SD.h>
#include <Wire.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>

// ---- Configuración OLED ----
#define SCREEN_WIDTH 128
#define SCREEN_HEIGHT 64
Adafruit_SSD1306 display(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, -1);

// WiFi Wokwi - local
const char *ssid = "Wokwi-GUEST";
const char *password = "";

// Simulación de servidor
const char *serverName = "http://test-server.local/simulated";

// Pines
const int ledOnline = 25;
const int ledOffline = 26;
const int ledSD = 27;
const int chipSelect = 5;
const int btnPin = 4;

// Variables GPS + sistema
float latitude = 19.4326;
float longitude = -99.1332;
int batteryLevel = 100;
bool externalPower = false;
bool fakeWiFiConnected = true;
unsigned long lastWiFiToggle = 0;
unsigned long wifiCycleTime = 30000;

const int chargeRate = 5;    // +5% cada ciclo
const int dischargeRate = 2; // -2% cada ciclo
int signalStrength = 95;
unsigned long lastUpdateTime = 0;
String statuses[] = {"Pago validado", "Ubicación confirmada", "Conexión establecida"};
int statusIndex = 0;

void setup()
{
  Serial.begin(115200);
  Serial.println("🚀 Iniciando GPS híbrido automático...");

  pinMode(ledOnline, OUTPUT);
  pinMode(ledOffline, OUTPUT);
  pinMode(ledSD, OUTPUT);
  pinMode(btnPin, INPUT_PULLUP);

  // ---- Inicializar pantalla ----
  if (!display.begin(SSD1306_SWITCHCAPVCC, 0x3C))
  {
    Serial.println("⚠️ Error OLED");
    for (;;)
      ;
  }
  display.clearDisplay();
  display.setTextSize(1);
  display.setTextColor(SSD1306_WHITE);

  // SD
  if (!SD.begin(chipSelect))
  {
    Serial.println("⚠️ Error al iniciar la tarjeta SD");
  }
  else
  {
    Serial.println("✅ Tarjeta SD lista");
  }

  // WiFi inicial
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED)
  {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\n✅ WiFi conectado a: " + WiFi.localIP().toString());

  lastWiFiToggle = millis();
}

void mostrarPantalla()
{
  display.clearDisplay();
  display.setCursor(0, 0);
  display.print("GPS Hibrido");
  display.setCursor(0, 12);
  display.print("Lat: ");
  display.print(latitude, 4);
  display.setCursor(0, 22);
  display.print("Lng: ");
  display.print(longitude, 4);
  display.setCursor(0, 32);
  display.print("Bateria: ");
  display.print(batteryLevel);
  display.print("%");
  display.setCursor(0, 42);
  display.print("Fuente: ");
  display.print(externalPower ? "Corriente" : "Bateria");
  display.display();
}

void loop()
{
  unsigned long now = millis();

  // 🔘 Botón para alternar fuente manualmente (override)
  if (digitalRead(btnPin) == LOW)
  {
    externalPower = !externalPower;
    Serial.println("⚡ Cambio manual de fuente por botón");
    delay(300); // anti-rebote
  }

  // 🔁 Ciclo automático de energía
  if (batteryLevel <= 0)
  {
    externalPower = true; // fuerza a corriente
  }
  else if (batteryLevel >= 100)
  {
    externalPower = false; // fuerza a batería
  }

  // 🔋 Gestión de energía
  if (externalPower)
  {
    batteryLevel += chargeRate;
    if (batteryLevel > 100)
      batteryLevel = 100;
  }
  else
  {
    batteryLevel -= dischargeRate;
    if (batteryLevel < 0)
      batteryLevel = 0;
  }

  // 🔁 Ciclo WiFi simulado
  if (now - lastWiFiToggle > wifiCycleTime)
  {
    fakeWiFiConnected = !fakeWiFiConnected;
    lastWiFiToggle = now;
    Serial.println(fakeWiFiConnected ? "🌐 WiFi RESTABLECIDO" : "🚫 WiFi DESCONECTADO");
  }

  // 📡 Simular GPS
  latitude += random(-5, 5) / 10000.0;
  longitude += random(-5, 5) / 10000.0;
  signalStrength = random(80, 100);
  lastUpdateTime = now;

  // 📦 Crear JSON
  StaticJsonDocument<400> doc;
  doc["id"] = "ART-HIBRIDO-001";
  doc["tipo"] = "GPS híbrido";
  doc["estado"] = fakeWiFiConnected ? "online" : "offline";
  doc["lat"] = latitude;
  doc["lng"] = longitude;
  doc["bateria"] = batteryLevel;
  doc["señal"] = signalStrength;
  doc["fuente"] = externalPower ? "Corriente" : "Batería";
  doc["status"] = statuses[statusIndex];
  statusIndex = (statusIndex + 1) % 3;

  String payload;
  serializeJson(doc, payload);
  Serial.println("📤 Payload:");
  Serial.println(payload);

  // 🚦LEDs
  digitalWrite(ledOnline, fakeWiFiConnected ? HIGH : LOW);
  digitalWrite(ledOffline, !fakeWiFiConnected ? HIGH : LOW);

  // Enviar o guardar
  if (fakeWiFiConnected)
  {
    digitalWrite(ledSD, LOW);
    Serial.println("🛰️ Enviando al servidor...");
    Serial.println("✅ Simulación de envío completada (no real).");
  }
  else
  {
    digitalWrite(ledSD, HIGH);
    File file = SD.open("/gps_log.txt", FILE_APPEND);
    if (file)
    {
      file.println(payload);
      file.close();
      Serial.println("💾 Guardado en SD (offline)");
    }
    else
    {
      Serial.println("⚠️ Error al guardar en SD");
    }
  }

  // 🔹 Mostrar en OLED
  mostrarPantalla();

  Serial.printf("🔋 Batería: %d%% | Fuente: %s\n", batteryLevel, externalPower ? "Corriente" : "Batería");
  delay(5000);
}
