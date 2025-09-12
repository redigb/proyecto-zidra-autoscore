package com.services_main.main_bk_service.dispositivoGps.infrastructure.adapter.mqtt.publisher;


import com.services_main.main_bk_service.dispositivoGps.infrastructure.adapter.mqtt.client.MqttAdapter;
import lombok.RequiredArgsConstructor;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GpsCommandService {

    private final MqttAdapter mqttAdapter;

    public void sendCommand(String gpsUsername, String commandJson) throws MqttException {
        String topic = "gps/" + gpsUsername + "/commands";
        mqttAdapter.publish(topic, commandJson);
    }
}
