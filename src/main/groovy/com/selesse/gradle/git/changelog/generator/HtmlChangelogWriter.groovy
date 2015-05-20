package com.selesse.gradle.git.changelog.generator

import com.selesse.gradle.git.GitCommandExecutor
import com.selesse.gradle.git.changelog.GitChangelogExtension
import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

class HtmlChangelogWriter extends BaseChangelogWriter {
    String htmlTemplate

    HtmlChangelogWriter(GitChangelogExtension extension, GitCommandExecutor gitExecutor) {
        super(extension, gitExecutor)
        this.htmlTemplate = extension.htmlTemplate
    }

    @Override
    void writeChangelog(PrintStream printStream) {
        def templateConfig = new TemplateConfiguration()
        MarkupTemplateEngine engine = new MarkupTemplateEngine(this.class.classLoader, templateConfig)
        def template = engine.createTemplate htmlTemplate
        def model = [:]
        template.make(model).writeTo(new PrintWriter(printStream))
    }
}
