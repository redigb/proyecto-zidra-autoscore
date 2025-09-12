package com.services_main.main_bk_service.dispositivoGps.infrastructure.adapter.mqtt.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "mqtt")
@Getter
@Setter
public class PropertiesMqtt {
    private String broker;
    private String clientId;
    private String username;
    private String password;
}
