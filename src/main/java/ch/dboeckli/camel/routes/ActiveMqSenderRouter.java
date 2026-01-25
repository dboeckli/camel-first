package ch.dboeckli.camel.routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ActiveMqSenderRouter extends RouteBuilder {

    @Value("${application.activemq.queue}")
    private String activeMqQueue;

    public static final String ACTIVE_MQ_ROUTER_ID = "active-mq-timer-route";
    private static final String ACTIVE_MQ_ROUTER_NAME = ActiveMqSenderRouter.class.getSimpleName();

    @Override
    public void configure() {
        from("timer:" + ACTIVE_MQ_ROUTER_NAME + "?period=10000&delay=2000")  // timer endpoint
            .routeId(ACTIVE_MQ_ROUTER_ID)
            .transform().constant("message-for-activemq")
            .log(LoggingLevel.INFO, "Sending activemq message: ${body}")
            .to("jms:" + activeMqQueue);
    }
}
