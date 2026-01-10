package ch.dboeckli.camel.routes;

import lombok.RequiredArgsConstructor;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class MyFileRouter extends RouteBuilder {

    public static final String LIST_FILES_ROUTE_ID = "list-files-route";
    public static final String LIST_FILES_ROUTE_NAME = "list-files-on-start";

    public static final String COPY_FILES_ROUTE_ID = "copy-files-route";

    public static final String INPUT_DIR = "files/input";
    public static final String OUTPUT_DIR = "files/output";

    @Override
    public void configure() {
        from("timer:" + LIST_FILES_ROUTE_NAME + "?repeatCount=1")
            .routeId(LIST_FILES_ROUTE_ID)
            .process(exchange -> {
                String listing = listFilesInDir(INPUT_DIR);
                exchange.getMessage().setBody(listing.isEmpty() ? "Keine Dateien gefunden." : listing);
            })
            .log(LoggingLevel.INFO, "Eingangs-Dateiliste:\n${body}");


        from("file:" + INPUT_DIR)
            .routeId(COPY_FILES_ROUTE_ID)
            .log(LoggingLevel.INFO, "Pickup: '${header.CamelFileName}' at ${header.CamelFilePath} (${header.CamelFileLength} bytes)")
            .log("Files: ${body}")
            .to("file:" + OUTPUT_DIR)
            .log(LoggingLevel.INFO, "Written: '${header.CamelFileName}' to ${header.CamelFilePath}");
    }

    private String listFilesInDir(String dir) {
        Path path = Path.of(dir);
        if (!Files.isDirectory(path)) {
            return "Verzeichnis nicht gefunden: " + dir;
        }
        try (Stream<Path> pathStream = Files.list(path)) {
            return pathStream
                .filter(Files::isRegularFile)
                .map(foundPath -> String.format("- %s (%d bytes)", foundPath.getFileName(), safeSize(foundPath)))
                .sorted()
                .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return "Fehler beim Auflisten der Dateien aus " + dir + ": " + e.getMessage();
        }
    }

    private long safeSize(Path path) {
        try {
            return Files.size(path);
        } catch (Exception e) {
            return -1L;
        }
    }


}
