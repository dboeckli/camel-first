package ch.dboeckli.camel.routes;

import ch.dboeckli.camel.routes.util.CurrentTime;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class MyThirdTimerRouter extends RouteBuilder {

    public static final String MY_THIRD_ROUTE_ID = "my-third-timer-route";

    @Override
    public void configure() {
        from("timer:third-timer from " + MyThirdTimerRouter.class)  // timer endpoint
            .routeId(MY_THIRD_ROUTE_ID)
            .transform().constant("Hello Camel! Time is: " + LocalDateTime.now()) // transform
            .to("log:info"); // log endpoint
    }
}