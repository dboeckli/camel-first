package ch.dboeckli.camel.routes;

import ch.dboeckli.camel.routes.processor.SimpleLogProcessor;
import ch.dboeckli.camel.routes.util.CurrentTime;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MyFirstTimerRouter extends RouteBuilder {

    @Value("${application.activemq.queue}")
    private String activeMqQueue;

    @Value("${application.camel.my-first-camel-route.enabled}")
    private boolean enabled;

    private final CurrentTime currentTime;

    public static final String MY_FIRST_ROUTE_ID = "my-first-timer-route";

    private static final String MY_FIRST_ROUTE_NAME = MySecondTimerRouter.class.getSimpleName();

    @Override
    public void configure() {
        from("timer:" + MY_FIRST_ROUTE_NAME + "?period=5000&delay=2000") // timer endpoint
            .autoStartup(enabled)
            .routeId(MY_FIRST_ROUTE_ID)
            .bean(currentTime, "getCurrentTime") // transform via bean because bean method
                                                 // getCurrentTime returns a String
            .bean(currentTime, "logCurrentTime") // process via bean because bean method
                                                 // logCurrentTime returns void
            .process(new SimpleLogProcessor())
            .to("log:info"); // log endpoint
    }

}