# Gradle Git Changelog

Gradle plugin for generating a changelog based on a Git commit history.

**Note**: This plugin is not yet stable and is being actively developed.

## Usage

```
buildscript {
    repositories {
        maven {
            url uri('path/to/repo')
        }
    }
    dependencies {
        classpath 'com.selesse:gradle-git-changelog:0.1.0-SNAPSHOT'
    }
}

apply plugin: 'com.selesse.git.changelog'
```

This will automatically hook the `generateChangelog` task into the
`processResources` or `assemble` task of the project.

## Configuration

```groovy
changelog {
    // The title appears at the top of the changelog.
    // Default value: the name of the project.
    title: "${project.name} - Changelog"

    // The output directory where the report is generated.
    // Default value: main resource directory, or the "build" directory
    outputDirectory: file("$projectDir")

    // The name of the report to generate.
    // Default value: CHANGELOG.md
    fileName: "changelog.txt"
}
```

## Planned Features

* Choice of HTML or plain-text changelog, with overrideable HTML templates
* Format customization
* Sections based on tags
