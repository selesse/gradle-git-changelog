package com.selesse.gradle.git.changelog.convention

import groovy.transform.ToString

@ToString
class HtmlConvention {
    String commitFormat
    String template = this.class.classLoader.getResource('html-template.tpl').text
}
