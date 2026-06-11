package ch.dboeckli.camel.routes.trace;

import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension;
import io.opentelemetry.sdk.trace.data.SpanData;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static io.opentelemetry.api.GlobalOpenTelemetry.resetForTest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertAll;

@CamelSpringBootTest
@SpringBootTest(properties = { "otel.traces.exporter=none", "otel.metrics.exporter=none", "otel.logs.exporter=none",
        "application.camel.my-first-camel-route.enabled=false", "application.camel.my-second-camel-route.enabled=false",
        "application.camel.my-third-camel-route.enabled=false", "application.camel.my-forth-camel-route.enabled=false",
        "application.camel.file-route.enabled=false", "application.camel.camel.active-mq-route.enabled=true",
        "spring.docker.compose.skip.in-tests=false", "spring.docker.compose.file=compose-with-mq.yaml" })
@ActiveProfiles("local")
@AutoConfigureObservability
@DirtiesContext
@Slf4j
public class ActiveMqSenderRouterTraceIT {

    @RegisterExtension
    static final OpenTelemetryExtension otelTesting = OpenTelemetryExtension.create();

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private ConsumerTemplate consumerTemplate;

    @Value("${application.activemq.queue}")
    private String activeMqQueue;

    @BeforeEach
    void setUp() {
        otelTesting.clearSpans();
        camelContext.start();
    }

    @AfterEach
    void tearDown() {
        resetForTest();
    }

    @Test
    void hello_returnsHelloMessage() {
        // Erwartete Span-Namen in logischer Reihenfolge
        List<String> expectedNames = List.of("timer", "activeMqSenderOnCompletion-onCompletion", "init-baggage-process",
                "close-baggage-on-completion-process", "transform-activemq-message-transform",
                "log-activemq-message-log", "add-baggage-process", "send-to-activemq-to", "my-first-activemq-queue");

        await().atMost(20, TimeUnit.SECONDS)
            .pollInterval(200, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> assertThat(otelTesting.getSpans()).hasSizeGreaterThanOrEqualTo(expectedNames.size()));

        // Spans nach Startzeit sortieren -> deterministische logische Reihenfolge
        List<SpanData> spans = otelTesting.getSpans()
            .stream()
            .sorted(Comparator.comparingLong(SpanData::getStartEpochNanos))
            .toList();

        for (SpanData span : spans) {
            log.info("### Span - traceId: {}, spanId: {}, name {}, kind: {}, status: {}", span.getTraceId(),
                    span.getSpanId(), span.getName(), span.getKind().name(), span.getStatus().getStatusCode().name());
        }
        for (SpanData span : spans) {
            log.info("### Span - name: {}, attributes: {}", span.getName(), span.getAttributes());
        }

        String expectedTraceId = spans.getFirst().getTraceId();
        assertAll(() -> assertThat(spans).hasSize(expectedNames.size()),

                // Alle Namen vorhanden (reihenfolgenunabhängig, robust)
                () -> assertThat(spans).extracting(SpanData::getName)
                    .containsExactlyInAnyOrderElementsOf(expectedNames),

                // Alle Spans INTERNAL und UNSET
                () -> assertThat(spans).allSatisfy(span -> {
                    assertThat(span.getKind().name()).isEqualTo("INTERNAL");
                    assertThat(span.getStatus().getStatusCode().name()).isEqualTo("UNSET");
                    assertThat(span.hasEnded()).isTrue();
                }),

                // Alle Spans gehören zum selben Trace
                () -> assertThat(spans).allSatisfy(span -> assertThat(span.getTraceId()).isEqualTo(expectedTraceId)));

        Exchange exchange = await().atMost(20, TimeUnit.SECONDS)
            .until(() -> consumerTemplate.receive("jms:" + activeMqQueue, 1000), Objects::nonNull);

        Message in = exchange.getIn();

        log.info("### Queue Message Body: {}", in.getBody(String.class));
        log.info("### Queue Message Headers:");
        in.getHeaders().forEach((key, value) -> log.info("###   Header - {} = {}", key, value));
    }

}
