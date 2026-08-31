package io.jonasg.avro;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.file.DeletingPathVisitor;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.apache.commons.io.file.Counters.noopPathCounters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AvdlToAvscMojoTest {

    private Log log;
    private MavenProject project;
    private Path tempOutputDir;
    private Path dependencyJar;

    @BeforeEach
    void setUp() throws Exception {
        log = mock(Log.class);
        project = mock(MavenProject.class);
        tempOutputDir = Files.createTempDirectory("avdl-mojo-output");
        dependencyJar = buildDependencyJar();
        when(project.getRuntimeClasspathElements()).thenReturn(List.of(dependencyJar.toString()));
    }

    @AfterEach
    void cleanUp() throws IOException {
        Files.walkFileTree(tempOutputDir, new DeletingPathVisitor(noopPathCounters()));
        Files.deleteIfExists(dependencyJar);
    }

    @Test
    void resolvesImportFromDependencyJarOnClasspath() throws Exception {
        AvdlToAvscMojo mojo = new AvdlToAvscMojo();
        setField(mojo, "project", project);
        setField(mojo, "avdlDirectory", "src/test/resources/avdl-classpath-import");
        setField(mojo, "avscDirectory", tempOutputDir.toString());
        setField(mojo, "maintainDirectoryStructure", false);

        mojo.setLog(log);
        mojo.execute();

        verify(log, never()).error(anyString());

        File personAvsc = tempOutputDir.resolve("Person.avsc").toFile();
        assertThat(personAvsc).exists();
        String schemaJson = Files.readString(personAvsc.toPath());
        assertThat(schemaJson).contains("\"name\" : \"Address\"");
    }

    private Path buildDependencyJar() throws IOException {
        Path jar = Files.createTempFile("avro-common", ".jar");
        String addressAvdl = """
                namespace io.jonasg;

                schema Address;

                record Address {
                    string street;
                    string city;
                }
                """;

        try (JarOutputStream jos = new JarOutputStream(Files.newOutputStream(jar, StandardOpenOption.TRUNCATE_EXISTING))) {
            jos.putNextEntry(new JarEntry("avro/avdl/address.avdl"));
            jos.write(addressAvdl.getBytes());
            jos.closeEntry();
        }
        return jar;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}