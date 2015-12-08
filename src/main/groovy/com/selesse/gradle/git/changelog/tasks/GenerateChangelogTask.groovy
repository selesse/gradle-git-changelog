package com.selesse.gradle.git.changelog.tasks

import com.google.common.base.MoreObjects
import com.selesse.gradle.git.GitCommandExecutor
import com.selesse.gradle.git.changelog.GitChangelogExtension
import com.selesse.gradle.git.changelog.generator.ChangelogWriter
import com.selesse.gradle.git.changelog.generator.HtmlChangelogWriter
import com.selesse.gradle.git.changelog.generator.MarkdownChangelogWriter
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class GenerateChangelogTask extends DefaultTask {
    GitChangelogExtension extension

    public GenerateChangelogTask() {
        this.description = 'Generates a changelog'
        this.group = 'build'
    }

    @TaskAction
    def generateChangelog() {
        def outputDirectoryFile = extension.outputDirectory
        outputDirectoryFile.mkdirs()

        extension.formats.each {
            String format = it as String

            ChangelogWriter changelogWriter
            if (format == "markdown") {
                format = "md"
                changelogWriter = createMarkdownChangelogWriter(extension)
            } else {
                changelogWriter = createHtmlChangelogWriter(extension)
            }

            String fileName = extension.fileName
            // i.e. CHANGELOG.md -> CHANGELOG.html
            fileName = fileName.substring(0, fileName.lastIndexOf('.')) + ".${format}"

            File changelogFile = new File(outputDirectoryFile, fileName)

            changelogWriter.writeChangelog(new PrintStream(new FileOutputStream(changelogFile)))
        }
    }

    static def createMarkdownChangelogWriter(GitChangelogExtension extension) {
        String commitFormat = MoreObjects.firstNonNull(
                extension.markdownConvention.commitFormat, extension.commitFormat
        )
        def gitExecutor = new GitCommandExecutor(commitFormat)
        return new MarkdownChangelogWriter(extension, gitExecutor)
    }

    static def createHtmlChangelogWriter(GitChangelogExtension extension) {
        String commitFormat = MoreObjects.firstNonNull(
                extension.htmlConvention.commitFormat, extension.commitFormat
        )
        def gitExecutor = new GitCommandExecutor(commitFormat)
        return new HtmlChangelogWriter(extension, gitExecutor)
    }
}
