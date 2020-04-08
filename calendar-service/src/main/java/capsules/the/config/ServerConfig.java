package capsules.the.config;

import capsules.the.endpoint.EventEndpoint;

import javax.websocket.Endpoint;
import javax.websocket.server.ServerApplicationConfig;
import javax.websocket.server.ServerEndpointConfig;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ServerConfig /** implements ServerApplicationConfig **/ {

//    @Override
    public Set<ServerEndpointConfig> getEndpointConfigs(Set<Class<? extends Endpoint>> endpointClasses) {
        Set<ServerEndpointConfig> configSet = new HashSet<>();
        for (Class endpointClass : endpointClasses) {
            if (endpointClass.equals(EventEndpoint.class)) {
                ServerEndpointConfig serverEndpointConfig = ServerEndpointConfig.Builder.create(endpointClass, "/event").build();
                configSet.add(serverEndpointConfig);
            }
        }
        return configSet;
    }

//    @Override
    public Set<Class<?>> getAnnotatedEndpointClasses(Set<Class<?>> endpointClasses) {
        return Collections.emptySet();
    }
}
