package ch.dboeckli.camel.routes;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.context.Scope;
import org.apache.camel.Exchange;
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
            .routeId(ACTIVE_MQ_ROUTER_ID)

            .process(exchange -> initBaggage(exchange, String.format("rootflow.id=%s", "abcd")))
            .id("init-baggage")

            .onCompletion()
            .id("activeMqSenderOnCompletion")

            .process(this::closeBaggage)
            .id("close-baggage-on-completion")
            // .end()

            .transform()
            .constant("message-for-activemq")
            .id("transform-activemq-message")

            .log(LoggingLevel.INFO, "Sending activemq message: ${body}")
            .id("log-activemq-message")

            .process(exchange -> addBaggage(exchange,
                    String.format("tenant.id=%s,flow.id=%s,message.id=%s", "guguseli", "12345", UUID.randomUUID())))
            .id("add-baggage")

            .to("jms:" + activeMqQueue)
            .id("send-to-activemq");
    }

    private void initBaggage(Exchange exchange, String additionalBaggage) {
        exchange.getMessage().setHeader("baggage", additionalBaggage);
        Scope scope = Baggage.current().makeCurrent();
        exchange.setProperty("BaggageScope", scope);
        log.info("baggage initialized: {}", additionalBaggage);
    }

    private void closeBaggage(Exchange exchange) {
        Scope baggageScope = exchange.getProperty("BaggageScope", Scope.class);
        if (baggageScope != null) {
            baggageScope.close();
            exchange.removeProperty("BaggageScope");
            log.info("baggage scope closed");
        }
    }

    private void addBaggage(Exchange exchange, String additionalBaggage) {
        String existingHeader = exchange.getMessage().getHeader("baggage", String.class);
        log.info("existingHeader: {}", existingHeader);

        String mergedHeader = (existingHeader == null || existingHeader.isBlank()) ? additionalBaggage
                : existingHeader + "," + additionalBaggage;

        log.info("mergedHeader: {}", mergedHeader);

        exchange.getMessage().setHeader("baggage", mergedHeader);
        log.info("baggage has been set");

        Baggage current = Baggage.current();
        current.forEach((k, e) -> log.info("### Baggage → {} = '{}'", k, e.getValue()));
    }

}
