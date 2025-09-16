package ch.dboeckli.camel.routes.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

@Slf4j
public class SimpleLogProcessor implements Processor {
    @Override
    public void process(Exchange exchange) {
        log.info("SimpleLogProcessor: Processing message: {}", exchange.getMessage().getBody());
    }
}
