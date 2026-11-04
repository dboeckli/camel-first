package ch.dboeckli.camel.routes;

import io.opentelemetry.context.Scope;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class ActiveMqSenderRouterBaggageTest {

    private static final String BAGGAGE_HEADER = "baggage";

    private static final String BAGGAGE_SCOPE_PROPERTY = "BaggageScope";

    private final ActiveMqSenderRouter router = new ActiveMqSenderRouter();

    private final DefaultCamelContext context = new DefaultCamelContext();

    private final Exchange exchange = new DefaultExchange(context);

    @Test
    void addsBaggageWhenHeaderIsMissing() {
        router.addBaggage(exchange, "flow.id=1");

        assertThat(exchange.getMessage().getHeader(BAGGAGE_HEADER, String.class)).isEqualTo("flow.id=1");
    }

    @Test
    void replacesBlankBaggageHeader() {
        exchange.getMessage().setHeader(BAGGAGE_HEADER, "   ");

        router.addBaggage(exchange, "flow.id=1");

        assertThat(exchange.getMessage().getHeader(BAGGAGE_HEADER, String.class)).isEqualTo("flow.id=1");
    }

    @Test
    void appendsToExistingBaggageHeader() {
        exchange.getMessage().setHeader(BAGGAGE_HEADER, "rootflow.id=abcd");

        router.addBaggage(exchange, "flow.id=1");

        assertThat(exchange.getMessage().getHeader(BAGGAGE_HEADER, String.class))
            .isEqualTo("rootflow.id=abcd,flow.id=1");
    }

    @Test
    void closesBaggageScopeAndRemovesProperty() {
        router.initBaggage(exchange, "rootflow.id=abcd");
        assertThat(exchange.getProperty(BAGGAGE_SCOPE_PROPERTY, Scope.class)).isNotNull();

        router.closeBaggage(exchange);

        assertThat(exchange.getProperty(BAGGAGE_SCOPE_PROPERTY)).isNull();
    }

    @Test
    void closingWithoutInitializedBaggageIsIgnored() {
        assertThatCode(() -> router.closeBaggage(exchange)).doesNotThrowAnyException();
    }

}
