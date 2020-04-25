gradle-kawa-plugin
==================
Add [Kawa](https://www.gnu.org/software/kawa/) language support to Gradle projects.

[![Gradle Plugin](https://img.shields.io/badge/gradle%20plugin-0.1.0-blue?style=flat-square)](https://plugins.gradle.org/plugin/in.praj.kawa)
![License](https://img.shields.io/github/license/praj-foss/gradle-kawa-plugin?style=flat-square)

## Quick start
Make sure you have JDK 8 or higher installed. The plugin downloads
and builds the Kawa toolchain before compiling your project sources.

### Applying
Add this to your plugins block to get started:

```gradle
plugins {
    id "in.praj.kawa" version "0.1.0"
}
```

### Configuring
The example below shows how the `kawa` extension can be used to
configure this plugin. All the values specified are defaults except
`version`, which must be provided by the user.

```gradle
kawa {
    version  = "3.1.1"                  // REQUIRED
    cacheDir = "$buildDir/kawaCache"    // Caches Kawa toolchain
    
    kawac {
        srcDir   = "$projectDir/src"
        destDir  = "$buildDir/kawaClasses"
        language = "scheme"
        args     = "--warn-as-error"
    }
}
```

#### Note
Since the `buildDir` gets deleted while running the `clean` task,
all the downloaded kawa tools will get deleted along with it. To
prevent this, you can specify a directory under your project root 
as the `cacheDir` for Kawa. Don't forget to mention that directory
in your `.gitignore` too.

### Usage
Run the `compileKawa` task to compile all Kawa sources inside your
specified `srcDir`. The compiler outputs to `destDir` if there
are no errors. You can run the following command to start the task:

```shell script
./gradlew compileKawa
```

It automatically downloads the required toolchain during first run.

## Features
The project is currently in pre-alpha, so not all the necessary
features are present for production use. However, it can be used
to manage small Kawa projects quite reasonably. This plugin 
provides the following features:
 - Automatic building of Kawa toolchain
 - Task to compile Kawa source files
 - Simple DSL for configuration 
 
The plugin currently supports Kawa versions 3.0 or higher. Older
versions that come with an Ant script might work too, but it has
not been tested.

## Roadmap
A lot more features are planned to provide better functionality
and support for building larger Kawa projects. These include
better integration with the java plugin and allowing the user
to add specific dependencies from the toolchain (e.g.: JavaFX
and XML modules) in an idiomatic way.

## License
This project is licensed under the MIT License. 
Please see the [LICENSE](LICENSE) file for more information.
