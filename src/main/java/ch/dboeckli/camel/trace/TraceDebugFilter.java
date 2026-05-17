package ch.dboeckli.camel.trace;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

@Component
@Slf4j
public class TraceDebugFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Log incoming traceparent header
        String incomingTraceparent = request.getHeader("traceparent");
        log.info("### Incoming traceparent: {}", incomingTraceparent);

        // Log current span context

        SpanContext traceContext = Span.current().getSpanContext();
        if (traceContext.isValid()) {
            log.info("### Current trace context: {}",
                    ReflectionToStringBuilder.toString(traceContext, ToStringStyle.MULTI_LINE_STYLE));
        }
        else {
            log.warn("### No valid span context found: {}",
                    ReflectionToStringBuilder.toString(traceContext, ToStringStyle.MULTI_LINE_STYLE));
        }

        filterChain.doFilter(request, response);
    }

}
