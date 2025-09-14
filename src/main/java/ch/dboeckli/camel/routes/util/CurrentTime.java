package ch.dboeckli.camel.routes.util;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class CurrentTime {

    public String getCurrentTime() {
        return "Hello Camel from Bean! Time is: " + LocalDateTime.now();
    }

    public void logCurrentTime() {
        log.info("logCurrentTime: Current time is: {}", LocalDateTime.now());
    }

}
