package ch.dboeckli.camel.routes;

import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class MyThirdTimerRouter extends RouteBuilder {

    public static final String MY_THIRD_ROUTE_ID = "my-third-timer-route";
    private static final String MY_THIRD_ROUTE_NAME = MyThirdTimerRouter.class.getSimpleName();

    @Override
    public void configure() {
        from("timer:" + MY_THIRD_ROUTE_NAME + "?period=5000&delay=2000")
            .routeId(MY_THIRD_ROUTE_ID)
            .transform().constant("Hello Camel! Time is: " + LocalDateTime.now()) // transform message null to constant
            .to("log:info"); // log endpoint
    }
}