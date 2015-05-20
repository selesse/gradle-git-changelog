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
    String htmlTemplate

    Closure includeLines
    Closure processLines
}
