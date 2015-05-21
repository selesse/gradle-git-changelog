# Gradle Git Changelog

[![Build status](https://travis-ci.org/selesse/gradle-git-changelog.png)](https://travis-ci.org/selesse/gradle-git-changelog)

Gradle plugin for generating a changelog based on a Git commit history.

[See recent changes.](CHANGELOG.md)

**Note**: This plugin should not be considered stable until 1.0.

## Usage

```
buildscript {
    repositories {
        maven {
            mavenCentral()
        }
    }
    dependencies {
        classpath 'com.selesse:gradle-git-changelog:0.1.0'
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

    // The range of commits the changelog should be composed of.
    // Default value: 'beginning' (i.e. full changelog)
    // Possible values: 'beginning', 'last_tag', 'xxx'
    //
    // 'last_tag' will use all the commits since the last tag,
    // 'beginning' will use all commits since the initial commit (default)
    // 'xxx' will use all the tags since the 'xxx' Git reference (i.e. `since = 1.2.0` will display the changelog
    //       since the 1.2.0 tag, excluding 1.2.0)
    since = 'last_tag'

    // The output formats that should be generated.
    // Default value: ['markdown']
    // Possible values: 'html', 'markdown'.
    formats = ['html', 'markdown']

    // The Groovy HTML template used to generate the HTML changelog.
    // See http://docs.groovy-lang.org/latest/html/documentation/template-engines.html
    htmlTemplate = file("$projectDir/htmlTemplate").text

    // The Git "pretty" changelog commit format.
    // Default value: %ad%x09%s (%an), which produces:
    // Thu May 7 20:10:33 2015 -0400	Initial commit (Alex Selesse)
    commitFormat = '%s (%an)'

    // A closure that returns 'true' if the line should be included in the changelog.
    // Default value: accept everything, { true }
    includeLines = {
        !it.contains("Merge")
    }

    // A closure that transforms a changelog String.
    // Default value: the identity closure, { it }
    //
    // For example, to remove '[ci skip]' from the changelog messages:
    processLines = {
        String input = it as String
        if (input.contains('[ci skip] ')) {
            input = input.minus('[ci skip] ')
        }
        input
    }
}
```
