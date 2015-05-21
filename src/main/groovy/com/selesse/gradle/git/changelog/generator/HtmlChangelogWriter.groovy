package com.selesse.gradle.git.changelog.generator

import com.google.common.collect.Maps
import com.selesse.gradle.git.GitCommandExecutor
import com.selesse.gradle.git.changelog.ChangelogParser
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
        def changelogContent = generateChangelogContent()
        ChangelogParser changelogParser = new ChangelogParser(changelogContent)

        def templateConfig = new TemplateConfiguration()
        templateConfig.with {
            autoNewLine = true
            autoIndent = true
            autoEscape = true
        }
        MarkupTemplateEngine engine = new MarkupTemplateEngine(this.class.classLoader, templateConfig)
        def modelTypes = [title: 'String',
                          headings: 'Set<String>',
                          headingsToCommitMap: 'Map<String, Collection<String>>']
        def template = engine.createTypeCheckedModelTemplate(htmlTemplate, modelTypes)
        def model = Maps.newHashMap()
        model.put('title', extension.title)
        model.put('headings', changelogParser.headings)
        model.put('headingsToCommitMap', changelogParser.headingsAndTheirCommits.asMap())
        template.make(model).writeTo(new PrintWriter(printStream))
    }
}
