package ch.dboeckli.camel.routes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Route;
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
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.dboeckli.camel.routes.MyFileRouter.*;
import static org.assertj.core.api.Assertions.assertThat;

@CamelSpringBootTest
@SpringBootTest
@ActiveProfiles("local")
@DirtiesContext
@UseAdviceWith
@MockEndpoints("file:" + OUTPUT_DIR)
@Slf4j
class MyFileRouterTest {

    @Autowired
    private CamelContext camelContext;

    @EndpointInject("mock:file:" + OUTPUT_DIR)
    private MockEndpoint outputMock;

    private static final Path FILES_PATH = Path.of(INPUT_DIR).getParent();
    private static final Path INPUT_PATH = Path.of(INPUT_DIR);
    private static final Path OUTPUT_PATH = Path.of(OUTPUT_DIR);


    @BeforeEach
    void adviceAndStartRoute() throws Exception {
        camelContext.start();

        log.info("### Stopping all routes");
        for (Route route : camelContext.getRoutes()) {
            camelContext.getRouteController().stopRoute(route.getId());
        }
        prepareFileFolders();
        assertThat(countRegularFiles(INPUT_PATH)).isEqualTo(countRegularFiles(FILES_PATH));

        log.info("### Starting route: {}", COPY_FILES_ROUTE_ID);
        camelContext.getRouteController().startRoute(COPY_FILES_ROUTE_ID);
        assertThat(camelContext.getRouteController().getRouteStatus(COPY_FILES_ROUTE_ID).isStarted()).isTrue();
    }

    @Test
    void fileRoute_should_copy_files_to_output_folder() throws Exception {
        List<String> expectedNames = listRegularFileNames(INPUT_PATH);
        outputMock.expectedMessageCount(expectedNames.size());
        outputMock.expectedHeaderValuesReceivedInAnyOrder(Exchange.FILE_NAME, expectedNames);

        outputMock.allMessages().body().isNotNull();

        outputMock.assertIsSatisfied();

        Awaitility.await()
            .atMost(5, TimeUnit.SECONDS)
            .pollInterval(Duration.ofMillis(100))
            .untilAsserted(() -> assertThat(countRegularFiles(OUTPUT_PATH)).isEqualTo(countRegularFiles(FILES_PATH)));
    }

    private void prepareFileFolders() throws Exception {
        Files.createDirectories(INPUT_PATH);
        Files.createDirectories(OUTPUT_PATH);

        deleteRegularFiles(INPUT_PATH);
        deleteRegularFiles(OUTPUT_PATH);

        copyBaseFilesToInput();
        waitUntilInputContainsAllBaseFiles();
    }

    private void deleteRegularFiles(Path path) throws Exception {
        if (Files.exists(path)) {
            try (Stream<Path> stream = Files.walk(path, 1)) {
                stream.filter(foundPath -> !foundPath.equals(path))
                    .filter(Files::isRegularFile)
                    .forEach(foundPath -> {
                        try {
                            Files.deleteIfExists(foundPath);
                        } catch (Exception e) {
                            throw new RuntimeException("Konnte Datei nicht löschen: " + foundPath, e);
                        }
                    });
            }
        }
    }

    private void copyBaseFilesToInput() throws Exception {
        if (Files.exists(FILES_PATH)) {
            try (Stream<Path> stream = Files.list(FILES_PATH)) {
                stream.filter(Files::isRegularFile)
                    .forEach(src -> {
                        Path target = INPUT_PATH.resolve(src.getFileName());
                        try {
                            log.info("Copying file: {} -> {}", src, target);
                            Files.copy(src, target, StandardCopyOption.REPLACE_EXISTING);
                        } catch (Exception e) {
                            throw new RuntimeException("Konnte Datei nicht kopieren: " + src + " -> " + target, e);
                        }
                    });
            }
        }
        Files.list(INPUT_PATH).forEach(path -> log.info("Input file: {}", path));
    }

    private void waitUntilInputContainsAllBaseFiles() throws Exception {
        List<String> expectedFileNames = listRegularFileNames(FILES_PATH);
        Awaitility.await()
            .atMost(Duration.ofSeconds(3))
            .pollInterval(Duration.ofMillis(100))
            .untilAsserted(() -> {
                List<String> actualFileNames = listRegularFileNames(INPUT_PATH);
                org.assertj.core.api.Assertions.assertThat(actualFileNames).containsAll(expectedFileNames);
            });
    }

    private List<String> listRegularFileNames(Path path) throws Exception {
        if (!Files.exists(path)) return java.util.List.of();
        try (Stream<Path> pathStream = Files.list(path)) {
            return pathStream.filter(Files::isRegularFile)
                .map(foundPath -> foundPath.getFileName().toString())
                .collect(Collectors.toList());
        }
    }

    private int countRegularFiles(Path path) throws Exception {
        if (!Files.exists(path)) return 0;
        try (Stream<Path> pathStream = Files.list(path)) {
            return (int) pathStream.filter(Files::isRegularFile).count();
        }
    }

}