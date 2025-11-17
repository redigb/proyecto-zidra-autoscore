# 📝 Funcionamiento del Broker MQTT

Este sistema utiliza **Mosquitto MQTT** como broker encargado de recibir
y distribuir la telemetría enviada por los dispositivos IoT (ESP32).\
Toda la comunicación se realiza mediante **MQTT TCP**, sin HTTPS ni
WebSockets.

------------------------------------------------------------------------

## ✔ Método de conexión

### 🔹 Desde los dispositivos IoT (ESP32)

Los dispositivos se conectan directamente por IP al broker usando el
puerto **1883**:

    mqtt://IP_DEL_SERVIDOR:1883

-   No se utiliza dominio.
-   No se usa Traefik.
-   No se expone MQTT por HTTP/HTTPS.

### 🔹 Desde el backend (Spring Boot)

El backend se comunica internamente con el contenedor:

    mqtt://mosquitto:1883

Esto es posible porque Docker/Dokploy resolvió el hostname del servicio
(`mosquitto`) dentro de la red interna.

------------------------------------------------------------------------

## ✔ Arquitectura

1.  El **ESP32** publica telemetría por MQTT hacia la **IP pública del
    servidor**.
2.  **Dokploy** redirige el puerto 1883 al contenedor de Mosquitto.
3.  **Mosquitto** recibe los mensajes y los reparte a todos los
    suscriptores.
4.  El **backend Spring Boot** se suscribe a los tópicos y procesa la
    data recibida.

------------------------------------------------------------------------

## ✔ Docker Compose utilizado en Dokploy

El servicio Mosquitto se despliega exponiendo únicamente el puerto MQTT:

``` yaml
services:
  mosquitto:
    image: eclipse-mosquitto:latest
    container_name: mosquitto
    ports:
      - "1883:1883"
    volumes:
      - ./mosquitto.conf:/mosquitto/config/mosquitto.conf
      - ./data:/mosquitto/data
      - ./log:/mosquitto/log
```

Esto permite:

-   Publicar desde cualquier red → **IP_SERVIDOR:1883**
-   Suscripción interna de servicios Docker → **mosquitto:1883**
