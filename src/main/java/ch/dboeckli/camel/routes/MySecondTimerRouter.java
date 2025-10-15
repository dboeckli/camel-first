package ch.dboeckli.camel.routes;

import ch.dboeckli.camel.routes.util.CurrentTime;
import lombok.RequiredArgsConstructor;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MySecondTimerRouter extends RouteBuilder {

    private final CurrentTime currentTime;

    public static final String MY_SECOND_ROUTE_ID = "my-second-timer-route";
    private static final String MY_SECOND_ROUTE_NAME = MySecondTimerRouter.class.getSimpleName();

    @Override
    public void configure() {
        from("timer:" + MY_SECOND_ROUTE_NAME + "?period=5000&delay=2000")
            .routeId(MY_SECOND_ROUTE_ID)

            .log(LoggingLevel.INFO, MY_SECOND_ROUTE_NAME, "# body before transform is: ${body}")
            .transform().method(currentTime, "getCurrentTime") // transform message null to value from bean method getCurrentTime()
            .log(LoggingLevel.INFO, MY_SECOND_ROUTE_NAME, "# body after transform is: ${body}")

            .to("log:info"); // log endpoint
    }
}