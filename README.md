# Avdl to Avsc Maven Plugin

The Avro IDL language is a much more ergonomic way to define Avro schemas than the JSON
format.
Yet the confluent schema registry requires the JSON format.

This plugin allows you to define your schemas in Avro IDL, use them to generate POJO's
with the `avro-maven-plugin` and convert them to JSON format
so they can be uploaded to the schema registry using the
`kafka-schema-registry-maven-plugin`.

## Usage

`avdlDirectory` will be traversed recursively for all `.avdl` files and converted to
`.avsc` files in `avscDirectory`. Every avro record will result as a single `.avsc`
file.

When one `.avdl` file contains multiple _types_ it will result in multiple `avsc` files, one for each type.

For example:

```avdl
namespace io.jonasg;

record Person {
	string name;
	int age;
	string? email;
	Sex sex;
}

enum Sex {
	MALE,
	FEMALE
}
```

Will result in two `.avsc` files; `Person.avsc` and `Sex.avsc`. 
The `Person.avsc` file will be **self-contained** and not depend on `Sex.avsc`.

If you only want one single file you can declare a main schema as such:

```avdl
namespace io.jonasg;

schema Person;

record Person {
	string name;
	int age;
	string? email;
	Sex sex;
}

enum Sex {
	MALE,
	FEMALE
}
```
This will only output self-contained `Person.avsc` file.

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
