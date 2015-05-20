package com.selesse.gradle.git.changelog

import groovy.transform.ToString

@ToString
class GitChangelogExtension {
    String title
    File outputDirectory
    String fileName

    String since

    String commitFormat
    Set<String> formats
    File velocityTemplate

    Closure includeLines
    Closure processLines
}
