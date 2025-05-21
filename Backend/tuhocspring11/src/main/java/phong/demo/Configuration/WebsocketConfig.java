package phong.demo.Configuration;

import jakarta.websocket.server.ServerContainer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;
@Profile("!test")
@Configuration
public class WebsocketConfig {


    @Bean
    // add here for testing purpose
    @ConditionalOnClass(ServerContainer.class)
    public ServerEndpointExporter serverEndpointExporter(){
        return new ServerEndpointExporter();
    }




}
