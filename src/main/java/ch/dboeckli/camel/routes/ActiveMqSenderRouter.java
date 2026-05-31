package ch.dboeckli.camel.routes;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.baggage.BaggageEntry;
import io.opentelemetry.context.Scope;
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

    @Override
    public void configure() {
        // timer endpoint
        from("timer:" + ACTIVE_MQ_ROUTER_NAME + "?period=10000&delay=2000")

            .autoStartup(enabled)

            .process(exchange -> {
                Scope scope = Baggage.current().toBuilder().put("rootflow.id", "1234").build().makeCurrent();
                exchange.setProperty("BaggageScope", scope);
            })
            .onCompletion()
            .process(exchange -> {
                Scope baggageScope = exchange.getProperty("BaggageScope", Scope.class);
                if (baggageScope != null) {
                    baggageScope.close();
                    exchange.removeProperty("BaggageScope");
                }
            })

            .process(exchange -> {
                BaggageEntry baggageEntry = Baggage.current().getEntry("rootflow.id");
                log.info("### baggageEntry: {}", baggageEntry);
                String baggage = String.format("tenant.id=%s,flow.id=%s,message.id=%s", "guguseli", "12345",
                        UUID.randomUUID());
                exchange.getMessage().setHeader("baggage", baggage);
                log.info("baggege has been set: {}", baggage);
            })

            .routeId(ACTIVE_MQ_ROUTER_ID)
            .transform()
            .constant("message-for-activemq")
            .log(LoggingLevel.INFO, "Sending activemq message: ${body}")
            .to("jms:" + activeMqQueue);
    }

}
