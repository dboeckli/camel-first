package ch.dboeckli.camel.trace;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Scope;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class TraceDebugFilterTest {

    private final TraceDebugFilter filter = new TraceDebugFilter();

    @Test
    void continuesFilterChainWithoutCurrentSpan() throws Exception {
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(new MockHttpServletRequest("GET", "/actuator/health"), new MockHttpServletResponse(), chain);

        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void continuesFilterChainWithValidCurrentSpan() throws Exception {
        SpanContext spanContext = SpanContext.create("0af7651916cd43dd8448eb211c80319c", "b7ad6b7169203331",
                TraceFlags.getSampled(), TraceState.getDefault());

        try (Scope ignored = Span.wrap(spanContext).makeCurrent()) {
            MockFilterChain chain = new MockFilterChain();

            filter.doFilter(new MockHttpServletRequest("GET", "/actuator/health"), new MockHttpServletResponse(),
                    chain);

            assertThat(chain.getRequest()).isNotNull();
        }
    }

}
