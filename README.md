# Gradle Git Changelog

[![Build status](https://travis-ci.org/selesse/gradle-git-changelog.png)](https://travis-ci.org/selesse/gradle-git-changelog)

Gradle plugin for generating a changelog based on a Git commit history.

**Note**: This plugin is not yet stable and is being actively developed.

## Usage

```
buildscript {
    repositories {
        maven {
            url "https://oss.sonatype.org/content/repositories/snapshots"
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

All of these configurations are optional.

```groovy
changelog {
    // The title appears at the top of the changelog.
    // Default value: the name of the project.
    title = "${project.name} - Changelog"

    // The output directory where the report is generated.
    // Default value: main resource directory, or the "build" directory
    outputDirectory = file("$projectDir")

    // The name of the report to generate.
    // Default value: CHANGELOG.md
    fileName = "changelog.txt"

    // The Git "pretty" changelog commit format.
    // Default value: %ad%x09%s (%an), which produces:
    // Thu May 7 20:10:33 2015 -0400	Initial commit (Alex Selesse)
    commitFormat = '%s (%an)'
}
```

## Planned Features

* Choice of HTML or plain-text changelog, with overridable HTML templates
* Format customization
* Sections based on tags
* Ability to filter out certain lines (i.e. exclude merge commits), or process
  certain lines (i.e. `RELEASE-NOTES: hi` => `hi`)
