package com.selesse.gradle.git.changelog

import com.selesse.gradle.git.changelog.convention.HtmlConvention
import com.selesse.gradle.git.changelog.convention.MarkdownConvention
import groovy.transform.ToString
import org.gradle.api.Project

@ToString
class GitChangelogExtension {
    String title
    File outputDirectory
    String fileName = 'CHANGELOG.md'

    String since = 'beginning'

    String commitFormat = '%ad%x09%s (%an)'
    MarkdownConvention markdownConvention = new MarkdownConvention()
    HtmlConvention htmlConvention = new HtmlConvention()

    Set<String> formats = ['markdown']

    Closure includeLines
    Closure processLines

    public GitChangelogExtension(Project project) {
        this.title = project.name
        this.outputDirectory = project.buildDir
    }

    def changelog(Closure closure) {
        closure.delegate = this
        closure()
    }

    def markdown(Closure closure) {
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = markdownConvention
        closure()
    }

    def html(Closure closure) {
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = htmlConvention
        closure()
    }

}
