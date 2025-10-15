package ch.dboeckli.camel.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MyForthTimerRouter extends RouteBuilder {

    public static final String MY_FORTH_ROUTE_ID = "my-forth-timer-route";
    private static final String MY_FORTH_ROUTE_NAME = MyForthTimerRouter.class.getSimpleName();

    @Override
    public void configure() {
        from("timer:" + MY_FORTH_ROUTE_NAME + "?period=5000&delay=2000")
            .routeId(MY_FORTH_ROUTE_ID)
            .process(exchange -> exchange.getIn().setBody("Hello Camel! Time is: " + LocalDateTime.now()))
            .to("log:info"); // log endpoint
    }
}