package com.chimaera.wagubook.config;
import com.chimaera.wagubook.controller.SignalingHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new SignalingHandler(), "/signal")
                .addInterceptors(new HttpSessionHandshakeInterceptor()) //웹 소켓 세션에서 httpSession정보를 가져오기 위해 HttpSession에 있던 정보들을 copy
                .setAllowedOrigins("*");
    }
}
