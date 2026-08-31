package io.jonasg.avro;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Mojo(name = "avdl-to-avsc",
        defaultPhase = LifecyclePhase.GENERATE_SOURCES,
        requiresDependencyResolution = ResolutionScope.RUNTIME)
@SuppressWarnings("unused")
public class AvdlToAvscMojo extends AbstractMojo {

    /**
     * The Maven project currently being built. Used to resolve the project's runtime
     * classpath, so that {@code import idl} statements in the source AVDL files can
     * reference types packaged inside dependency jars, not just files on the local
     * filesystem.
     *
     * @parameter default-value="${project}"
     * @readonly
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

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
     * default-value="${project.build.directory}/generated-resources/avro"
     */
    @Parameter(property = "avscDirectory", required = true)
    private String avscDirectory;

    /**
     * Port the directory structure of the avdl files into the corresponding avsc output directory
     *
     * @parameter property="maintainDirectoryStructure"
     * default-value="false"
     */
    @Parameter(property = "maintainDirectoryStructure", required = false, defaultValue = "false")
    private boolean maintainDirectoryStructure;

    @Override
    public void execute() throws MojoExecutionException {
        ConverterConfiguration configuration = new ConverterConfiguration(
                Path.of(avdlDirectory),
                Path.of(avscDirectory),
                maintainDirectoryStructure);

        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(buildProjectClassLoader(originalClassLoader));
            new AvdlToAvscConverter(getLog()).convert(configuration);
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }

    private URLClassLoader buildProjectClassLoader(ClassLoader parent) throws MojoExecutionException {
        try {
            List<URL> urls = new ArrayList<>();
            urls.add(Path.of(avdlDirectory).toUri().toURL());
            for (Object element : project.getRuntimeClasspathElements()) {
                urls.add(Path.of((String) element).toUri().toURL());
            }
            return new URLClassLoader(urls.toArray(new URL[0]), parent);
        } catch (DependencyResolutionRequiredException | MalformedURLException e) {
            throw new MojoExecutionException("Failed to build classpath for avdl-to-avsc", e);
        }
    }

}