# 🚀 Zidra Risk & IoT Tracking System

Bienvenido al repositorio principal de **Zidra Risk & IoT Tracking System**, una solución integral End-to-End que fusiona lo mejor de las tecnologías **Fintech** e **IoT**.

## 🎯 ¿De qué trata y cuál es nuestra intención?

El propósito principal de este sistema es **evaluar el riesgo crediticio mediante Inteligencia Artificial** y, al mismo tiempo, **gestionar y rastrear en tiempo real los activos financiados (como vehículos) a través de dispositivos GPS**.

Esta doble funcionalidad permite a la organización no solo tomar decisiones financieras más seguras e informadas a la hora de otorgar créditos, sino también proteger y monitorear la garantía física durante todo el ciclo de vida del préstamo.

## 🏗️ Arquitectura y Estructura del Proyecto

Este repositorio funciona como un **monorepositorio**, organizando sus diferentes dominios en directorios especializados. A continuación, se detalla la estructura principal:

### 📂 1. Apis
Contiene los servicios backend que exponen la lógica de negocio y las integraciones principales:

*   **`main_bk_service`**: Desarrollado en **Spring Boot WebFlux** con **R2DBC** (PostgreSQL). Es el corazón del sistema, responsable de la evaluación crediticia (clientes, solicitudes, integración N8N) y la administración de dispositivos IoT (telemetría GPS y vinculación con artefactos).
*   **`risk_score`**: Una API construida en **FastAPI (Python)**. Se encarga del ciclo completo de Machine Learning (entrenamiento, versionado y predicción en tiempo real) para el scoring de riesgo de clientes nuevos y existentes.
*   **`receptor_ioti_bk`**: Microservicio hiper-rápido creado en **Rust (Axum + Tokio + SQLx)**. Actúa como el receptor directo de la telemetría GPS, guardando los datos en PostgreSQL y retransmitiéndolos en tiempo real a través de WebSockets.
*   **`n8n_service`**: Contiene configuraciones y flujos exportados de N8N, utilizado como orquestador para conectar las evaluaciones crediticias con los modelos de IA y otros sistemas externos.

### 📂 2. IoTI (Internet de las Cosas)
Todo lo relacionado con la conectividad y hardware de rastreo:

*   **`broker_mosquito`**: Configuración de **Eclipse Mosquitto MQTT**. Funciona como el canal de comunicación TCP para recibir mensajes desde los dispositivos hardware en campo.
*   **`ioti_artefactos_gps`**: Código C++ (Arduino/PlatformIO) para los dispositivos físicos (ej. ESP32). Se encargan de leer coordenadas GPS, y enviarlas vía MQTT hacia nuestro broker.

### 📂 3. Aplicaciones
Las interfaces de usuario:

*   **`calculadora_crediticia_movil`**: Aplicación móvil multiplataforma desarrollada en **Flutter**, diseñada para facilitar el cálculo y simulación de créditos desde cualquier dispositivo.
*   **`front-admin-service`**: Aplicación web frontend (Dashboard) para la administración del sistema.

### 📂 4. Asistente
*   **Modelos 3D**: Contiene archivos como `Lucy_asistente.fbx` y `mi_avatar_asistente.glb`, orientados a una futura interfaz o experiencia inmersiva con un asistente virtual.

## 🛠️ Stack Tecnológico Destacado

*   **Backend & APIs:** Spring Boot 3 (WebFlux), Rust (Axum), Python (FastAPI).
*   **Base de Datos:** PostgreSQL (con acceso reactivo vía R2DBC y asíncrono vía SQLx).
*   **IoT & Mensajería:** Eclipse Mosquitto (MQTT), WebSockets.
*   **Machine Learning:** Scikit-Learn, LightGBM, Pandas.
*   **Hardware:** C++ (Framework Arduino para ESP32).
*   **Frontend & Móvil:** Flutter.
*   **Orquestación & CI/CD:** Docker, Docker Compose, Dokploy, N8N.

## 🚀 Despliegue

El despliegue está orquestado mediante Docker Compose (ver `docker-compose.yml` y `autoscore-stack.yml`).

1.  Asegúrate de configurar las variables de entorno necesarias (ej. `POSTGRES_USER`, `POSTGRES_PASSWORD`, credenciales de API).
2.  Despliega la infraestructura base usando los archivos Compose. Esto levantará la base de datos PostgreSQL, la API de Riesgo, el orquestador N8N y el Broker MQTT, enlazándolos a través de redes Docker.

---

*Desarrollado con ❤️ para garantizar seguridad financiera y protección de activos.*
