package ch.dboeckli.camel;

import ch.dboeckli.camel.routes.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Route;
import org.apache.camel.ServiceStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
    useMainMethod = SpringBootTest.UseMainMethod.ALWAYS,
    properties = {
        "otel.java.global-autoconfigure.enabled=true",
        "spring.docker.compose.enabled=true",
        "spring.docker.compose.skip.in-tests=false"
    }
)
@Slf4j
@ActiveProfiles("local")
class FirstCamelApplicationIT {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CamelContext camelContext;

    @Test
    void contextLoads() {
        assertNotNull(applicationContext, "Application context should not be null");
        log.info("Testing Spring 6 Application {}", applicationContext.getApplicationName());

        List<String> routeIds = camelContext.getRoutes().stream()
            .map(Route::getRouteId)
            .toList();
        log.info("Found Route IDs: {}", routeIds);

        assertAll("Check all route IDs are present and started",
            () -> assertEquals(7, routeIds.size()),

            // Check existence and status for ActiveMqSenderRouter
            () -> assertTrue(routeIds.contains(ActiveMqSenderRouter.ACTIVE_MQ_ROUTER_ID)),
            () -> assertEquals(ServiceStatus.Started, camelContext.getRouteController().getRouteStatus(ActiveMqSenderRouter.ACTIVE_MQ_ROUTER_ID), "ActiveMq Route should be started"),

            // Check existence and status for MySecondTimerRouter
            () -> assertTrue(routeIds.contains(MySecondTimerRouter.MY_SECOND_ROUTE_ID)),
            () -> assertEquals(ServiceStatus.Started, camelContext.getRouteController().getRouteStatus(MySecondTimerRouter.MY_SECOND_ROUTE_ID), "Second Timer Route should be started"),

            // Check existence and status for MyThirdTimerRouter
            () -> assertTrue(routeIds.contains(MyThirdTimerRouter.MY_THIRD_ROUTE_ID)),
            () -> assertEquals(ServiceStatus.Started, camelContext.getRouteController().getRouteStatus(MyThirdTimerRouter.MY_THIRD_ROUTE_ID), "Third Timer Route should be started"),

            // Check existence and status for MyForthTimerRouter
            () -> assertTrue(routeIds.contains(MyForthTimerRouter.MY_FORTH_ROUTE_ID)),
            () -> assertEquals(ServiceStatus.Started, camelContext.getRouteController().getRouteStatus(MyForthTimerRouter.MY_FORTH_ROUTE_ID), "Forth Timer Route should be started"),

            // Check existence and status for MyFileRouter (List Files)
            () -> assertTrue(routeIds.contains(MyFileRouter.LIST_FILES_ROUTE_ID)),
            () -> assertEquals(ServiceStatus.Started, camelContext.getRouteController().getRouteStatus(MyFileRouter.LIST_FILES_ROUTE_ID), "List Files Route should be started"),

            // Check existence and status for MyFileRouter (Copy Files)
            () -> assertTrue(routeIds.contains(MyFileRouter.COPY_FILES_ROUTE_ID)),
            () -> assertEquals(ServiceStatus.Started, camelContext.getRouteController().getRouteStatus(MyFileRouter.COPY_FILES_ROUTE_ID), "Copy Files Route should be started")
        );
    }


}
