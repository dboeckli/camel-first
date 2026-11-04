package ch.dboeckli.camel.routes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;

import static org.assertj.core.api.Assertions.assertThat;

class MyFileRouterListingTest {

    private final MyFileRouter router = new MyFileRouter();

    @Test
    void listsRegularFilesWithSizeSortedByName(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("b.txt"), "bb");
        Files.writeString(dir.resolve("a.txt"), "aaa");
        Files.createDirectory(dir.resolve("subdir"));

        assertThat(router.listFilesInDir(dir.toString())).isEqualTo("- a.txt (3 bytes)\n- b.txt (2 bytes)");
    }

    @Test
    void returnsEmptyListingForEmptyDirectory(@TempDir Path dir) {
        assertThat(router.listFilesInDir(dir.toString())).isEmpty();
        assertThat(router.buildListing(dir.toString())).isEqualTo("Keine Dateien gefunden.");
    }

    @Test
    void reportsMissingDirectory(@TempDir Path dir) {
        Path missing = dir.resolve("missing");

        assertThat(router.listFilesInDir(missing.toString())).isEqualTo("Verzeichnis nicht gefunden: " + missing);
    }

    @Test
    void reportsDirectoryThatCannotBeListed(@TempDir Path dir) throws IOException {
        Path unreadable = Files.createDirectory(dir.resolve("unreadable"));
        try {
            Files.setPosixFilePermissions(unreadable, PosixFilePermissions.fromString("---------"));

            assertThat(router.listFilesInDir(unreadable.toString()))
                .startsWith("Fehler beim Auflisten der Dateien aus " + unreadable);
        }
        finally {
            Files.setPosixFilePermissions(unreadable, PosixFilePermissions.fromString("rwx------"));
        }
    }

    @Test
    void buildListingReturnsListingForDirectoryWithFiles(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("a.txt"), "aaa");

        assertThat(router.buildListing(dir.toString())).isEqualTo("- a.txt (3 bytes)");
    }

    @Test
    void returnsMinusOneWhenFileSizeCannotBeRead(@TempDir Path dir) {
        assertThat(router.safeSize(dir.resolve("not-there"))).isEqualTo(-1L);
    }

}
