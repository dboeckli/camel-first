package ch.dboeckli.camel.routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ActiveMqSenderRouter extends RouteBuilder {

    @Value("${application.activemq.queue}")
    private String activeMqQueue;

    @Value("${application.camel.active-mq-route.enabled}")
    private boolean enabled;

    public static final String ACTIVE_MQ_ROUTER_ID = "active-mq-timer-route";

    private static final String ACTIVE_MQ_ROUTER_NAME = ActiveMqSenderRouter.class.getSimpleName();

    private static final String BAGGAGE_PREFIX = "CamelBaggage_";

    @Override
    public void configure() {
        from("timer:" + ACTIVE_MQ_ROUTER_NAME + "?period=10000&delay=2000")

            .autoStartup(enabled)
            .routeId(ACTIVE_MQ_ROUTER_ID)

            // Camel's OTel tracer reads CamelBaggage_* exchange properties and propagates
            // them as W3C baggage to the JMS producer (camel-opentelemetry2
            // traceProcessors).
            .process(exchange -> {
                exchange.setProperty(BAGGAGE_PREFIX + "rootflow.id", "abcd");
                exchange.setProperty(BAGGAGE_PREFIX + "tenant.id", "guguseli");
                exchange.setProperty(BAGGAGE_PREFIX + "flow.id", "12345");
                exchange.setProperty(BAGGAGE_PREFIX + "message.id", UUID.randomUUID().toString());
            })
            .id("add-baggage")

            .transform()
            .constant("message-for-activemq")
            .id("transform-activemq-message")

            .log(LoggingLevel.INFO, "Sending activemq message: ${body}")
            .id("log-activemq-message")

            .to("jms:" + activeMqQueue)
            .id("send-to-activemq");
    }

}
