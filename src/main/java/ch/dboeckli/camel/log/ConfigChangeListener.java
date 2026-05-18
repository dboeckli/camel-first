package ch.dboeckli.camel.log;

import io.micrometer.observation.Observation;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.StreamSupport;

@Component
@Slf4j
public class ConfigChangeListener {

    private final Tracer tracer;

    private static final List<String> PASSWORD_KEY_LIST = Arrays.asList("jwt.key-value", "password", "credentials",
            "secret");

    public ConfigChangeListener(OpenTelemetry openTelemetry) {
        this.tracer = openTelemetry.getTracer("config-change-listener");
    }

    @EventListener
    public void handleContextRefresh(ContextRefreshedEvent event) {
        Span span = tracer.spanBuilder("config.change.listener").startSpan();
        try (Scope _ = span.makeCurrent()) {
            doHandleContextRefresh(event);
        }
        catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            throw e;
        }
        finally {
            span.end();
        }
    }

    public void doHandleContextRefresh(ContextRefreshedEvent event) {
        final Environment env = event.getApplicationContext().getEnvironment();
        log.debug(LogMessage.RECEIVED_CONTEXT_REFRESH_EVENT.getMessage());
        log.info("Active profiles: {}", Arrays.toString(env.getActiveProfiles()));
        final MutablePropertySources sources = ((AbstractEnvironment) env).getPropertySources();
        StreamSupport.stream(sources.spliterator(), false)
            .filter(EnumerablePropertySource.class::isInstance)
            .map(ps -> ((EnumerablePropertySource<?>) ps).getPropertyNames())
            .flatMap(Arrays::stream)
            .distinct()
            .forEach(prop -> {
                String propertyValue = env.getProperty(prop);
                if (propertyValue != null) {

                    if (PASSWORD_KEY_LIST.stream().anyMatch(prop.toLowerCase()::contains)
                            || PASSWORD_KEY_LIST.stream().anyMatch(propertyValue.toLowerCase()::contains)) {

                        log.info("{}: {}", prop, "**************************"); // hide
                                                                                // password
                    }
                    else {
                        log.info("{}: {}", prop, propertyValue);
                    }

                }
                else {
                    log.warn("null propertyValue encountered in {}: {}", prop, propertyValue);
                }
            });
    }

}
