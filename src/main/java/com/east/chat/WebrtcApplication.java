package com.east.chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@EnableScheduling
@SpringBootApplication
public class WebrtcApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebrtcApplication.class, args);
    }

}
