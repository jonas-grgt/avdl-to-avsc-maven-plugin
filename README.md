# Avdl to Avsc Maven Plugin

The Avro IDL language is a much more ergonomic way to define Avro schemas than the JSON
format.
Yet the confluent schema registry requires the JSON format.

This plugins allows you to define your schemas in Avro IDL, use them to generate POJO's
with the `avro-maven-plugin` and convert them to JSON format
so they can be uploaded to the schema registry using the
`kafka-schema-registry-maven-plugin`.

## Usage

`avdlDirectory` will be traversed recursively for all `.avdl` files and converted to
`.avsc` files in `avscDirectory`. Every avro record will result as a single `.avsc`
file.

Add the plugin to your `pom.xml`

```xml

<plugin>
  <groupId>io.jonasg</groupId>
  <artifactId>avdl-to-avsc-maven-plugin</artifactId>
  <version>${avdl-to-avsc-maven-plugin.version}</version>
  <executions>
    <execution>
      <goals>
        <goal>avdl-to-avsc</goal>
      </goals>
      <configuration>
        <avdlDirectory>${avdl.dir}</avdlDirectory>
        <avscDirectory>${project.build.directory}/generated-sources/avsc</avscDirectory>
      </configuration>
    </execution>
  </executions>
</plugin>
```

## Upload the AVSC files to the schema registry

```xml

<plugins>
  <plugin>
    <groupId>io.confluent</groupId>
    <artifactId>kafka-schema-registry-maven-plugin</artifactId>
    <version>${confluent.version}</version>
    <configuration>
      <subjects>
        <my-subject>target/generated-sources/avsc/Event.avsc</my-subject>
      </subjects>
    </configuration>
  </plugin>
</plugins>
```
