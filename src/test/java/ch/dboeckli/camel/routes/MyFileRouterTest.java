package ch.dboeckli.camel.routes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.MockEndpoints;
import org.apache.camel.test.spring.junit5.UseAdviceWith;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.dboeckli.camel.routes.MyFileRouter.MY_FILE_ROUTE_ID;
import static org.assertj.core.api.Assertions.assertThat;

@CamelSpringBootTest
@SpringBootTest
@ActiveProfiles("local")
@DirtiesContext
@UseAdviceWith
@MockEndpoints("file:files/output")
@Slf4j
class MyFileRouterTest {

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private ProducerTemplate producerTemplate;

    @EndpointInject("mock:file:files/output")
    private MockEndpoint outputMock;

    private static final Path FILES_DIR = Paths.get("files");
    private static final Path INPUT_DIR = FILES_DIR.resolve("input");
    private static final Path OUTPUT_DIR = FILES_DIR.resolve("output");


    @BeforeEach
    void adviceAndStartRoute() throws Exception {
        camelContext.start();

        log.info("### Stopping all routes");
        for (var route : camelContext.getRoutes()) {
            camelContext.getRouteController().stopRoute(route.getId());
        }
        prepareFileFolders();
        assertThat(countRegularFiles(INPUT_DIR)).isEqualTo(countRegularFiles(FILES_DIR));

        log.info("### Starting route: {}", MY_FILE_ROUTE_ID);
        camelContext.getRouteController().startRoute(MY_FILE_ROUTE_ID);
        assertThat(camelContext.getRouteController().getRouteStatus(MY_FILE_ROUTE_ID).isStarted()).isTrue();
    }

    @Test
    void fileRoute_copiesBody_and_preservesFileNameHeader() throws Exception {
        var expectedNames = listRegularFileNames(INPUT_DIR);
        outputMock.expectedMessageCount(expectedNames.size());
        outputMock.expectedHeaderValuesReceivedInAnyOrder(Exchange.FILE_NAME, expectedNames);

        outputMock.allMessages().body().isNotNull();

        outputMock.assertIsSatisfied();

        Awaitility.await()
            .atMost(5, TimeUnit.SECONDS)
            .pollInterval(Duration.ofMillis(100))
            .untilAsserted(() -> assertThat(countRegularFiles(OUTPUT_DIR)).isEqualTo(countRegularFiles(FILES_DIR)));
    }

    private void prepareFileFolders() throws Exception {
        Files.createDirectories(INPUT_DIR);
        Files.createDirectories(OUTPUT_DIR);

        deleteRegularFiles(INPUT_DIR);
        deleteRegularFiles(OUTPUT_DIR);

        copyBaseFilesToInput();
        waitUntilInputContainsAllBaseFiles();
    }

    private void deleteRegularFiles(Path dir) throws Exception {
        if (Files.exists(dir)) {
            try (var stream = Files.walk(dir, 1)) {
                stream.filter(p -> !p.equals(dir))
                    .filter(Files::isRegularFile)
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (Exception e) {
                            throw new RuntimeException("Konnte Datei nicht löschen: " + p, e);
                        }
                    });
            }
        }
    }

    private void copyBaseFilesToInput() throws Exception {
        if (Files.exists(FILES_DIR)) {
            try (var stream = Files.list(FILES_DIR)) {
                stream.filter(Files::isRegularFile)
                    .forEach(src -> {
                        var target = INPUT_DIR.resolve(src.getFileName());
                        try {
                            log.info("Copying file: {} -> {}", src, target);
                            Files.copy(src, target, StandardCopyOption.REPLACE_EXISTING);
                        } catch (Exception e) {
                            throw new RuntimeException("Konnte Datei nicht kopieren: " + src + " -> " + target, e);
                        }
                    });
            }
        }
        Files.list(INPUT_DIR).forEach(p -> log.info("Input file: {}", p));
    }

    private void waitUntilInputContainsAllBaseFiles() throws Exception {
        var expected = listRegularFileNames(FILES_DIR);
        Awaitility.await()
            .atMost(Duration.ofSeconds(3))
            .pollInterval(Duration.ofMillis(100))
            .untilAsserted(() -> {
                var actual = listRegularFileNames(INPUT_DIR);
                // Enthält alle erwarteten Dateien (Reihenfolge egal)
                org.assertj.core.api.Assertions.assertThat(actual).containsAll(expected);
            });
    }

    private java.util.List<String> listRegularFileNames(Path dir) throws Exception {
        if (!Files.exists(dir)) return java.util.List.of();
        try (Stream<Path> s = Files.list(dir)) {
            return s.filter(Files::isRegularFile)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
        }
    }

    private int countRegularFiles(Path dir) throws Exception {
        if (!Files.exists(dir)) return 0;
        try (var s = Files.list(dir)) {
            return (int) s.filter(Files::isRegularFile).count();
        }
    }

}