package com.ruoyi.transmit.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 配置消息代理（前端订阅用）
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app"); // 客户端发送消息前缀
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                // ✅ 允许跨域来源（本地前端）
//                .setAllowedOriginPatterns("http://localhost:81", "http://127.0.0.1:81")
                .setAllowedOriginPatterns(
                        "http://localhost:8081",
                        "http://127.0.0.1:8081",
                        "http://63.56.3.194:8081"
                )
                // ✅ 若前端启用了 withCredentials，必须允许带 cookie
                .setAllowedOrigins() // 不要写 "*"，用上面的 patterns
                .setAllowedOriginPatterns("http://localhost:8081", "http://127.0.0.1:8081")
                // ✅ 启用 SockJS 兼容模式
                .withSockJS()
                // ✅ 避免 credentials 与 cookie 冲突
                .setSessionCookieNeeded(false);
    }


//@Override
//public void registerStompEndpoints(StompEndpointRegistry registry) {
//    // ✅ 禁用Origin检查，因为代理已经处理了CORS
//    registry.addEndpoint("/ws")
//            .setAllowedOriginPatterns(
//                        "http://localhost:8081",
//                        "http://127.0.0.1:8081",
//                        "http://10.146.11.249:8081"
//                )
//            .withSockJS()
//            .setSupressCors(true); // ✅ 抑制CORS处理
//}
//
//    @Override
//    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
//        registration.setSendTimeLimit(15 * 1000).setSendBufferSizeLimit(512 * 1024);
//    }

}

//@Configuration
//@EnableWebSocketMessageBroker
//public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
//
//    @Override
//    public void configureMessageBroker(MessageBrokerRegistry config) {
//        config.enableSimpleBroker("/topic", "/queue");
//        config.setApplicationDestinationPrefixes("/app");
//    }
//
//    @Override
//    public void registerStompEndpoints(StompEndpointRegistry registry) {
//        // ✅ 简化配置，使用允许的源列表
//        registry.addEndpoint("/ws")
//                .setAllowedOriginPatterns(
//                        "http://localhost:8081",
//                        "http://127.0.0.1:8081",
//                        "http://localhost:8787",
//                        "http://127.0.0.1:8787"
//                )
//                .withSockJS();
//    }
//}
