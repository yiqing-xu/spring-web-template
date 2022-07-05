package com.xyq.tweb.bean;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * <p>
 *
 * </p>
 *
 * @author xuyiqing
 * @since 2022/6/22
 */
@Configuration
@Component
public class BeanRegister {

    //@Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

}
