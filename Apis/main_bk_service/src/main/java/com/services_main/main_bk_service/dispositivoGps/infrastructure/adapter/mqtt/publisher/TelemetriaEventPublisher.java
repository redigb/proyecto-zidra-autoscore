package com.services_main.main_bk_service.dispositivoGps.infrastructure.adapter.mqtt.publisher;

import com.services_main.main_bk_service.dispositivoGps.infrastructure.adapter.in.model.gpsTelemetria.GpsTelemetriaRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TelemetriaEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void publish(Long deviceId, GpsTelemetriaRequest dto) {
        // envia a /topic/gps/6
        messagingTemplate.convertAndSend("/topic/gps/" + deviceId, dto);
    }
}
