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

schema Person; // declares Person as the main schema

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

## Porting AVDL directory structure to the AVSC output

the plugin `maintainDirectoryStructure` configuration option allows you to maintain the directory structure of your AVDL
files in the output AVSC directory. When set to `true`, the plugin will create subdirectories in the output AVSC
directory
that mirror the structure of the input AVDL directory.

given the following plugin configuration :

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
                <avdlDirectory>src/main/resources/avdl</avdlDirectory>
                <avscDirectory>${project.build.directory}/generated-sources/avsc</avscDirectory>
                <maintainDirectoryStructure>true</maintainDirectoryStructure>
            </configuration>
        </execution>
    </executions>
</plugin>   
```

And the AVDL source directory structure :

```src/main/resources/avdl
├── user
│   ├── User.avdl
│   └── Address.avdl
└── order
    └── Order.avdl
``` 

The plugin will generate the below AVSC directory structure:

```target/generated-sources/avsc
├── user
│   ├── User.avsc
│   └── Address.avsc
└── order
    └── Order.avsc
```

## Importing AVDL files from a dependency

If you want to share and reuse types across multiple projects, you can package `.avdl`
files inside a jar and import them from a dependent project's `.avdl` files using
Avro IDL's `import idl` statement.

Say `Address.avdl` from the earlier example is instead packaged inside a separate
dependency jar, on the classpath at:

```
avro/avdl/Address.avdl
```

```avdl
namespace io.jonasg;

schema Address;

record Address {
    string street;
    string city;
}
```

You can import it from `Order.avdl` by referencing its path on the classpath:

```avdl
namespace io.jonasg;

schema Order;

import idl "avro/avdl/Address.avdl";

record Order {
    string orderId;
    Address shippingAddress;
}
```

The plugin resolves the import against the project's runtime classpath, so make sure
the jar containing `Address.avdl` is declared as a dependency of the project running
the plugin:

```xml

<dependencies>
    <dependency>
        <groupId>io.jonasg</groupId>
        <artifactId>avro-common-types</artifactId>
        <version>${avro-common-types.version}</version>
    </dependency>
</dependencies>
```

No additional plugin configuration is needed — as long as the dependency jar is on the
project's classpath, `import idl "..."` resolves the same way it would for a local file.