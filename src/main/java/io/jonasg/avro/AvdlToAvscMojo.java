package io.jonasg.avro;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "avdl-to-avsc", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
@SuppressWarnings("unused")
public class AvdlToAvscMojo extends AbstractMojo {


    /**
     * Directory containing the Aro Schemas in AVDL format.
     *
     * @parameter property="avdlInputDirectory"
     */
    @Parameter(property = "avdlDirectory", required = true)
    private String avdlDirectory;

    /**
     * Directory where to output the Avro schemas (.avsc)
     *
     * @parameter property="avscOutputDirectory"
     *            default-value="${project.build.directory}/generated-resources/avro"
     */
    @Parameter(property = "avscDirectory", required = true)
    private String avscDirectory;

    @Override
    public void execute() {
        new AvdlToAvscConverter(getLog()).convert(avdlDirectory, avscDirectory);
    }
}
