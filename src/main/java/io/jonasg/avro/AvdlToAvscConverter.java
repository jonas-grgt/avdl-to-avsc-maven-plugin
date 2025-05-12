package io.jonasg.avro;

import static java.nio.file.Files.walk;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.avro.tool.IdlToSchemataTool;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.logging.Log;

public class AvdlToAvscConverter {

    private final IdlToSchemataTool idlToSchemataTool  = new IdlToSchemataTool();;

    private final Log log;

    public AvdlToAvscConverter(Log log) {
        this.log = log;
    }

    public void convert(String avdlDirectory, String avscDirectory) {
        Path avdlPath = new File(avdlDirectory).toPath();
        if (!avdlPath.toFile().exists()) {
            log.error("Avdl directory does not exist: " + avdlPath);
            return;
        }

        try(Stream<Path> stream = walk(avdlPath)) {
            stream
                    .filter(this::onAvdlExtension)
                    .forEach(avdlFile -> avdlToAvsc(avdlFile, new File(avscDirectory)));

        } catch (IOException e) {
            log.error("Error walking avdl directory", e);
        }
    }

    boolean onAvdlExtension(Path path) {
        var file = path.toFile();
        var fileName = file.getName();
        return Objects.equals("avdl", FilenameUtils.getExtension(fileName));
    }

    void avdlToAvsc(Path avdlFilePath, File outputDirectory) {
        List<String> toolsArgs = new ArrayList<>();
        toolsArgs.add(avdlFilePath.toFile().getAbsolutePath());
        toolsArgs.add(outputDirectory.getAbsolutePath());

        try {
            idlToSchemataTool.run(System.in, System.out, System.err, toolsArgs);
            log.info("Converted " + avdlFilePath);
        } catch (Exception ex) {
            log.error("Error converting " + avdlFilePath + " to " + outputDirectory, ex);
            throw new RuntimeException(ex);
        }
    }

}
