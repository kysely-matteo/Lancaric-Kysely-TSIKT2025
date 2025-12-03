package sk.semestralka.studybase.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");  // Kanály na ktoré klienti odoberajú správy
        config.setApplicationDestinationPrefixes("/app");  // Prefix pre odosielanie správ
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        System.out.println("DEBUG: Konfigurujem WebSocket endpoint");
        registry.addEndpoint("/ws")  // WebSocket endpoint
                .setAllowedOriginPatterns("*");  // Povoľ všetky origins (v produkcii obmedz)
                //.withSockJS();  // Pre staršie prehliadače (alebo JavaFX)
    }
}