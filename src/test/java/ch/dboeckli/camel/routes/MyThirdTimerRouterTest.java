package ch.dboeckli.camel.routes;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Route;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.MockEndpoints;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static ch.dboeckli.camel.routes.MyThirdTimerRouter.MY_THIRD_ROUTE_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@CamelSpringBootTest
@SpringBootTest
@ActiveProfiles("local")
@DirtiesContext
@MockEndpoints("log:*")
class MyThirdTimerRouterTest {

    @Autowired
    private CamelContext camelContext;

    // log:info wird durch mock:log:info ersetzt
    @EndpointInject("mock:log:info")
    private MockEndpoint logMock;

    @BeforeEach
    void startOnlyDesiredRoute() throws Exception {
        // stop all routes
        for (var route : camelContext.getRoutes()) {
            camelContext.getRouteController().stopRoute(route.getId());
        }
        // start only the desired route
        camelContext.getRouteController().startRoute(MY_THIRD_ROUTE_ID);
    }


    @Test
    void timerRoute_emitsGreeting() throws Exception {
        logMock.expectedMinimumMessageCount(1);
        logMock.assertIsSatisfied(5000);

        List<Route> startedRoutes = camelContext.getRoutes().stream()
            .filter(r -> camelContext.getRouteController().getRouteStatus(r.getId()).isStarted())
            .toList();
        String body = logMock.getExchanges().getFirst().getIn().getBody(String.class);

        assertAll(
            () -> assertEquals(1, startedRoutes.size()),
            () -> assertEquals(MY_THIRD_ROUTE_ID, startedRoutes.getFirst().getId()),
            () -> assertThat(body).startsWith("Hello Camel! Time is: ")
        );

    }

}