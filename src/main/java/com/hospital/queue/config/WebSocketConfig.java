package com.hospital.queue.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Log the configured frontend URL for debugging
        System.out.println("WebSocket configured with frontend URL: " + frontendUrl);
        
        // Allow connections from both the configured frontend URL and the Render.com frontend
        registry.addEndpoint("/ws")
                .setAllowedOrigins(
                    frontendUrl,
                    "https://hospital-queue-frontend.onrender.com"
                )
                .withSockJS();
    }
    
    // Configure WebSocket buffer sizes and timeouts for better stability
    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(8192);
        container.setMaxBinaryMessageBufferSize(8192);
        container.setMaxSessionIdleTimeout(60 * 1000L); // 60 seconds
        return container;
    }
}
