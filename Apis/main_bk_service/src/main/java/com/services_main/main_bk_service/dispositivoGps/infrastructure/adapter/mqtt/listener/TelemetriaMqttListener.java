package com.services_main.main_bk_service.dispositivoGps.infrastructure.adapter.mqtt.listener;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.services_main.main_bk_service.dispositivoGps.application.port.in.GpsTelemetriaServicePort;
import com.services_main.main_bk_service.dispositivoGps.infrastructure.adapter.in.model.gpsTelemetria.GpsTelemetriaRequest;
import com.services_main.main_bk_service.dispositivoGps.infrastructure.adapter.mqtt.client.MqttAdapter;
import com.services_main.main_bk_service.dispositivoGps.infrastructure.adapter.mqtt.publisher.TelemetriaEventPublisher;
import com.services_main.main_bk_service.dispositivoGps.infrastructure.mappers.gpsTelemetria.GpsTelemetriaRestMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TelemetriaMqttListener {

    private final MqttAdapter mqttAdapter;
    private final GpsTelemetriaServicePort gpsTelemetriaService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final GpsTelemetriaRestMapper mapper;

    private final TelemetriaEventPublisher publisherSocket;

    @PostConstruct
    public void init() throws MqttException {
        mqttAdapter.subscribe("gps/+/telemetria", (topic, message) -> {
            try {
                String payload = new String(message.getPayload());
                System.out.println("📥 MQTT recibido en topic=" + topic);
                System.out.println(payload);

                JsonNode json = objectMapper.readTree(payload);

                String fechaRaw = json.get("fechaHora").asText();
                LocalDateTime fecha;
                try {
                    fecha = LocalDateTime.parse(fechaRaw); // si viene en ISO-8601
                } catch (Exception ex) {
                    // fallback si ESP32 sigue mandando 1970
                    fecha = LocalDateTime.now();
                }

                GpsTelemetriaRequest dto = new GpsTelemetriaRequest(
                        json.get("gpsDeviceId").asLong(),
                        json.get("latitud").asDouble(),
                        json.get("longitud").asDouble(),
                        fecha,
                        json.get("speed").asDouble(),
                        json.get("estadoEncendido").asBoolean(),
                        json.get("extraData")
                );

                gpsTelemetriaService.registrarTelemetria(mapper.toDomain(dto)).subscribe();
                publisherSocket.publish(dto.getGpsDeviceId(), dto); // enviar por WeSocket
                System.out.println("✅ Telemetría procesada para deviceId=" + dto.getGpsDeviceId());

            } catch (Exception e) {
                System.err.println("❌ Error procesando MQTT: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
