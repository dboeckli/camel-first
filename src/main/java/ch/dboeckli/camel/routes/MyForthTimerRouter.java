package ch.dboeckli.camel.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MyForthTimerRouter extends RouteBuilder {

    public static final String MY_FORTH_ROUTE_ID = "my-forth-timer-route";

    @Override
    public void configure() {
        from("timer:forth-timer from " + MyForthTimerRouter.class)  // timer endpoint
            .routeId(MY_FORTH_ROUTE_ID)
            .process(e -> e.getIn().setBody("Hello Camel! Time is: " + LocalDateTime.now()))
            .to("log:info"); // log endpoint
    }
}