package ch.dboeckli.camel.routes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Route;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.MockEndpoints;
import org.apache.camel.test.spring.junit5.UseAdviceWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static ch.dboeckli.camel.routes.ActiveMqSenderRouter.ACTIVE_MQ_ROUTER_ID;
import static org.junit.jupiter.api.Assertions.*;

@CamelSpringBootTest
@SpringBootTest
@ActiveProfiles("local")
@DirtiesContext
@MockEndpoints("jms:*")
@Slf4j
@UseAdviceWith // disables auto-start of Camel routes
class ActiveMqSenderRouterTest {

    @Autowired
    private CamelContext camelContext;

    @Value("${application.activemq.queue}")
    private String activeMqQueue;

    @BeforeEach
    void startOnlyDesiredRoute() throws Exception {
        camelContext.start(); // @UseAdviceWith disables the auto-start

        log.info("### Stopping all routes");
        for (var route : camelContext.getRoutes()) {
            camelContext.getRouteController().stopRoute(route.getId());
        }
        log.info("### Starting route: {}", ACTIVE_MQ_ROUTER_ID);
        camelContext.getRouteController().startRoute(ACTIVE_MQ_ROUTER_ID);
    }

    @Test
    void timerRoute_sendsMessageToJmsQueue() throws Exception {
        MockEndpoint jmsMock = camelContext.getEndpoint("mock:jms:" + activeMqQueue, MockEndpoint.class);

        jmsMock.expectedMessageCount(1);
        jmsMock.message(0).body().isEqualTo("message-for-activemq");
        jmsMock.assertIsSatisfied(7000);

        List<Route> startedRoutes = camelContext.getRoutes()
            .stream()
            .filter(r -> camelContext.getRouteController().getRouteStatus(r.getId()).isStarted())
            .toList();

        Map<String, Object> headers = jmsMock.getReceivedExchanges().getFirst().getMessage().getHeaders();
        log.info("### Received headers:");
        headers.forEach((key, value) -> log.info("  {} = {}", key, value));

        String baggage = jmsMock.getReceivedExchanges().getFirst().getMessage().getHeader("baggage", String.class);
        log.info("Received baggage header: {}", baggage);

        assertAll(() -> assertNotNull(baggage),
                () -> assertTrue(baggage.contains("rootflow.id=abcd"), "missing rootflow.id, baggage was: " + baggage),
                () -> assertTrue(baggage.contains("tenant.id=guguseli"), "missing tenant.id, baggage was: " + baggage),
                () -> assertTrue(baggage.contains("flow.id=12345"), "missing flow.id, baggage was: " + baggage),
                () -> assertTrue(baggage.contains("message.id="), "missing message.id, baggage was: " + baggage),
                // UUID-Format prüfen
                () -> assertTrue(baggage.matches(".*message\\.id=[0-9a-f-]{36}.*"),
                        "message.id is not a valid UUID, baggage was: " + baggage));

        assertAll(() -> assertNotNull(baggage), () -> assertTrue(baggage.contains("rootflow.id=abcd"),
                "baggage should contain rootflow.id=abcd but was: " + baggage));

        assertAll(() -> assertEquals(1, startedRoutes.size()),
                () -> assertEquals(ACTIVE_MQ_ROUTER_ID, startedRoutes.getFirst().getId()));
    }

}
