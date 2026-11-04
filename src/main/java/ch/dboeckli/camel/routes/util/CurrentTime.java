package ch.dboeckli.camel.routes.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
@Slf4j
public class CurrentTime {

    public String getCurrentTime() {
        return "Hello Camel from Bean! Time is: " + LocalDateTime.now(ZoneId.systemDefault());
    }

    public void logCurrentTime() {
        log.info("logCurrentTime: Current time is: {}", LocalDateTime.now(ZoneId.systemDefault()));
    }

}
