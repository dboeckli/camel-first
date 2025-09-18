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

    public static final String MY_FILE_ROUTE_ID = "my-file-route";

    @Override
    public void configure() {
        from("timer:list-files-on-start?repeatCount=1")
            .routeId("list-input-files-on-start")
            .process(exchange -> {
                String listing = listFilesInDir("files/input");
                exchange.getMessage().setBody(listing.isEmpty() ? "Keine Dateien gefunden." : listing);
            })
            .log(LoggingLevel.INFO, "Eingangs-Dateiliste:\n${body}");



        from("file:files/input")
            .routeId(MY_FILE_ROUTE_ID)
            .log(LoggingLevel.INFO, "Pickup: '${header.CamelFileName}' at ${header.CamelFilePath} (${header.CamelFileLength} bytes)")
            .log("Files: ${body}")
            .to("file:files/output")
            .log(LoggingLevel.INFO, "Written: '${header.CamelFileName}' to ${header.CamelFilePath}");
    }

    private String listFilesInDir(String dir) {
        Path path = Path.of(dir);
        if (!Files.isDirectory(path)) {
            return "Verzeichnis nicht gefunden: " + dir;
        }
        try (Stream<Path> s = Files.list(path)) {
            return s
                .filter(Files::isRegularFile)
                .map(p -> String.format("- %s (%d bytes)", p.getFileName(), safeSize(p)))
                .sorted()
                .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return "Fehler beim Auflisten der Dateien aus " + dir + ": " + e.getMessage();
        }
    }

    private long safeSize(Path p) {
        try {
            return Files.size(p);
        } catch (Exception e) {
            return -1L;
        }
    }


}
