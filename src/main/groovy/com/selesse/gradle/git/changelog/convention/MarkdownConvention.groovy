package com.selesse.gradle.git.changelog.convention

import groovy.transform.ToString

@ToString
class MarkdownConvention {
    String commitFormat = '* %s (%an)'
}
