package com.selesse.gradle.git.changelog

import groovy.transform.ToString

@ToString
class GitChangelogExtension {
    String title
    File outputDirectory
    String fileName
    String commitFormat
    Closure includeLines
    Closure processLines
}
