package ch.dboeckli.camel;
// TODOS: RENAME PACKAGE

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class FirstCamelApplication {

    public static void main(String[] args) {
        log.info("Starting FirstCamelApplication...");
        SpringApplication.run(FirstCamelApplication.class, args);
    }

}
