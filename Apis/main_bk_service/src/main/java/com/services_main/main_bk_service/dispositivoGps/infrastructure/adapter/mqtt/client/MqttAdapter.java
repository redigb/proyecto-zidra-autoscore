package com.services_main.main_bk_service.dispositivoGps.infrastructure.adapter.mqtt.client;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MqttAdapter {

    private final PropertiesMqtt propertiesMqtt;
    private MqttClient client;

    // guardamos listeners para re-suscribir tras reconexión
    private final List<Pair<String, IMqttMessageListener>> listeners = new ArrayList<>();

    @PostConstruct
    public void init() {
        Flux.interval(Duration.ofSeconds(10))
                .subscribe(tick -> connect());
    }

    private void connect() {
        try {
            if (client == null || !client.isConnected()) {
                client = new MqttClient(propertiesMqtt.getBroker(), propertiesMqtt.getClientId());
                MqttConnectOptions options = new MqttConnectOptions();
                options.setCleanSession(true);
                options.setAutomaticReconnect(true);
                options.setUserName(propertiesMqtt.getUsername());
                options.setPassword(propertiesMqtt.getPassword().toCharArray());

                client.connect(options);
                System.out.println("✅ Conectado a MQTT en " + propertiesMqtt.getBroker());

                // re-suscribir todos los listeners registrados
                for (var l : listeners) {
                    client.subscribe(l.getFirst(), l.getSecond());
                    System.out.println("📡 Re-suscrito a " + l.getFirst());
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error al conectar a MQTT: " + e.getMessage());
        }
    }

    public void subscribe(String topic, IMqttMessageListener listener) throws MqttException {
        listeners.add(Pair.of(topic, listener)); // guardar
        if (client != null && client.isConnected()) {
            client.subscribe(topic, listener);
            System.out.println("📡 Suscrito a " + topic);
        } else {
            System.err.println("⚠️ Cliente no conectado aún, guardado para más tarde: " + topic);
        }
    }

    public void publish(String topic, String message) throws MqttException {
        if (client != null && client.isConnected()) {
            client.publish(topic, new MqttMessage(message.getBytes()));
        } else {
            System.err.println("⚠️ No se puede publicar, cliente MQTT no conectado.");
        }
    }
}