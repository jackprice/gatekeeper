package io.gatekeeper;

import org.junit.AfterClass;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GatekeeperTest {

    private static List<Path> temporaryDirectories = new ArrayList<>();

    protected static Path createTemporaryDirectory() throws IOException {
        Path directory = Files.createTempDirectory("gatekeeper-test");

        temporaryDirectories.add(directory);

        return directory;
    }

    @AfterClass
    public static void tearDown() throws IOException {
        for (Path path : temporaryDirectories) {
            File file = new File(path.toString());

            for (File nested : file.listFiles()) {
                nested.delete();
            }

            Files.delete(path);
        }
    }
}
