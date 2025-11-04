package io.jonasg.avro;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AvdlToAvscConverterTest {

    private Log log;

    private AvdlToAvscConverter converter;

    @BeforeEach
    void setUp() {
        log = mock(Log.class);
        converter = new AvdlToAvscConverter(log);
    }

    @Test
    void convertWithNonExistentDirectory() {
        String nonExistentDirectory = "non-existent-dir";
        String outputDirectory = "output-dir";

        converter.convert(nonExistentDirectory, outputDirectory);

        verify(log).error("Avdl directory does not exist: " + Path.of(nonExistentDirectory));
    }

    @Test
    void convertWithValidAvdlFiles() throws IOException {
        Path tempOutputDir = Files.createTempDirectory("avdl-output");

        converter.convert("src/test/resources/avdl-input", tempOutputDir.toString());

        verify(log).info(contains("Converted src/test/resources/avdl-input/test.avdl"));
        assertThat(tempOutputDir.toFile().listFiles())
                .hasSize(1)
                .extracting(File::getName)
                .contains("Person.avsc");
        tempOutputDir.toFile().delete();
    }


    @Test
    void mainSchemaOnly() throws IOException {
        Path tempOutputDir = Files.createTempDirectory("avdl-output");

        converter.convert("src/test/resources/avdl-main-only", tempOutputDir.toString());

        assertThat(tempOutputDir.toFile().listFiles())
                .hasSize(1)
                .extracting(File::getName)
                .contains("Person.avsc");
        tempOutputDir.toFile().delete();
    }

    @Test
    void testConvertWithNoAvdlFiles() throws IOException {
        converter.convert("src/test/resources/no-avdl-files", "src/test/resources/avdl-output");

        verify(log, never()).info(contains("Converted"));
    }
}