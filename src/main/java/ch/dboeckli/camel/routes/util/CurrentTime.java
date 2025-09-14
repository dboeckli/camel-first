package ch.dboeckli.camel.routes.util;


import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CurrentTime {

    public String getCurrentTime() {
        return "Hello Camel from Bean! Time is: " + LocalDateTime.now();
    }

}
