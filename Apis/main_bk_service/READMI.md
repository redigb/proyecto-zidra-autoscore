## 📘 Main Backend Service

Este backend implementa dos grandes funcionalidades:

-   Evaluación crediticia y scoring de riesgo\
-   Administración y telemetría de dispositivos GPS (IoT)

El proyecto está desarrollado con **Spring Boot WebFlux**, usa **R2DBC**
para acceso reactivo a **PostgreSQL** y se integra con **MQTT** para
procesar datos de dispositivos en tiempo real.

------------------------------------------------------------------------

## 🚀 funcion del sistema

------------------------------------------------------------------------

## 🟦 1. Módulo de Evaluación Crediticia

Este módulo gestiona los datos de clientes, solicitudes de crédito y el
proceso de evaluación de riesgo usando un modelo externo.

### **Flujo de funcionamiento**

1.  Se registra un pre-cliente con sus datos personales.\
2.  Se crea una solicitud de crédito asociada a ese pre-cliente.\
3.  La solicitud es evaluada enviando la información a un servicio
    externo (N8N + modelo IA).\
4.  El sistema recibe:
    -   Nivel de riesgo\
    -   Lista de motivos\
    -   Recomendaciones\
    -   Probabilidad de incumplimiento\
5.  Esa información se guarda en la base:
    -   **Scoring** (resultado técnico del modelo)\
    -   **Interpretación** (resumen entendible para un asesor)

### **¿Qué permite?**

✔ Registrar clientes y solicitudes\
✔ Evaluar riesgo con IA\
✔ Guardar scoring y explicaciones\
✔ Consultar solicitudes, scoring e interpretaciones

------------------------------------------------------------------------

## 🟩 2. Módulo IoT / GPS (Dispositivos y Telemetría)

Este módulo se encarga de manejar los dispositivos GPS usados para
rastreo vehicular y lectura de datos en tiempo real.

### **Flujo de funcionamiento**

1.  Los dispositivos GPS envían mensajes a un broker MQTT.\
2.  Un servicio interno escucha esos mensajes y los convierte en
    telemetría (posición, velocidad, estado del motor, etc).\
3.  Los datos se almacenan en PostgreSQL usando R2DBC.

La API permite consultar:

-   Última ubicación\
-   Historial entre fechas\
-   Telemetría completa por dispositivo

### **Además: Vinculación GPS → Artefacto**

El backend permite vincular un GPS con un artefacto (vehículo u otro
activo).

El sistema gestiona:

-   Vinculaciones activas\
-   Historial de vínculos\
-   Desvinculación segura\
-   Consulta por artefacto o por GPS

------------------------------------------------------------------------

## 🟧 Diseño general del sistema

### ✔ Todo es reactivo

El backend usa **Spring WebFlux + R2DBC**, logrando alta concurrencia
con pocas conexiones.

### ✔ Integración con servicios externos

-   **N8N:** para ejecutar modelos de evaluación de riesgo\
-   **MQTT:** para recibir telemetría GPS\
-   **PostgreSQL:** base de datos reactiva

### ✔ Arquitectura orientada a puertos y adaptadores

Cada módulo tiene separada:

-   Lógica de dominio\
-   Adaptadores REST\
-   Puertos de persistencia\
-   Servicios

------------------------------------------------------------------------

## 🔌 Principales interfaces disponibles

### **Evaluación crediticia**

-   Registrar pre-cliente\
-   Registrar solicitud\
-   Evaluar solicitud\
-   Obtener scoring\
-   Obtener interpretación\
-   Consultar clientes y solicitudes

### **GPS / IoT**

-   Registrar dispositivo GPS\
-   Consultar por IMEI o ID\
-   Obtener telemetría\
-   Obtener última ubicación\
-   Filtrar por fechas\
-   Eliminar telemetría

### **Vinculación GPS con artefactos**

-   Crear vínculo\
-   Obtener vínculo activo\
-   Consultar por artefacto\
-   Desvincular GPS

------------------------------------------------------------------------

## ⚙️ Tecnologías principales

-   Spring Boot 3.5.5\
-   Spring WebFlux\
-   R2DBC PostgreSQL\
-   MQTT (Paho Client)\
-   Project Reactor\
-   Lombok · MapStruct\
-   Docker · Dokploy

------------------------------------------------------------------------

## ▶️ ¿Cómo se usa?

1.  Despliega el backend con Docker o Dokploy.\
2.  Configura las variables necesarias:
    -   `SPRING_PROFILES_ACTIVE`\
    -   Credenciales DB\
    -   Credenciales MQTT\
3.  Asegúrate de que el broker MQTT y PostgreSQL estén accesibles.\
4.  Consume la API según tus necesidades:

```{=html}
<!-- -->
```
    /api/evaluador/...
    /api/gps-telemetria/...
    /api/ioti-gps/...
    /api/artefacto-gps-link/...
