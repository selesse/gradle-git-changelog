package com.selesse.gradle.git.changelog.tasks

import com.google.common.base.MoreObjects
import com.selesse.gradle.git.GitCommandExecutor
import com.selesse.gradle.git.changelog.GitChangelogExtension
import com.selesse.gradle.git.changelog.generator.ChangelogWriter
import com.selesse.gradle.git.changelog.generator.HtmlChangelogWriter
import com.selesse.gradle.git.changelog.generator.MarkdownChangelogWriter
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction

class GenerateChangelogTask extends DefaultTask {
    GenerateChangelogTask() {
        this.description = 'Generates a changelog'
        this.group = 'build'

        inputs.property 'git-head-sha', 'git rev-parse HEAD'.execute().text.trim().toString()
    }

    @OutputFiles
    def getOutputFilePaths() {
        project.changelog.formats.collect {
            String format = it as String

            return getOutputFile(format)
        }
    }

    File getOutputFile(String format) {
        // i.e. CHANGELOG.md -> CHANGELOG.html
        String baseFileName = project.changelog.fileName
        def fileName = baseFileName.substring(0, baseFileName.lastIndexOf('.')) + ".${format}"
        return new File(getOutputDirectory(), fileName)
    }

    File getOutputDirectory() {
        return project.changelog.outputDirectory
    }

    @TaskAction
    def generateChangelog() {
        project.changelog.formats.each {
            String format = it as String

            ChangelogWriter changelogWriter
            if (format == "markdown") {
                format = "md"
                changelogWriter = createMarkdownChangelogWriter()
            } else {
                changelogWriter = createHtmlChangelogWriter()
            }

            getOutputDirectory().mkdirs()
            def changelogFile = getOutputFile(format)


            def fileOutputStream = new FileOutputStream(changelogFile)
            changelogWriter.writeChangelog(new PrintStream(fileOutputStream))
            fileOutputStream.close()
        }
    }

    def createMarkdownChangelogWriter() {
        def extension = project.changelog as GitChangelogExtension
        String commitFormat = MoreObjects.firstNonNull(
                extension.markdownConvention.commitFormat, extension.commitFormat
        )
        def gitExecutor = new GitCommandExecutor(commitFormat)
        return new MarkdownChangelogWriter(extension, gitExecutor)
    }

    def createHtmlChangelogWriter() {
        def extension = project.changelog as GitChangelogExtension
        String commitFormat = MoreObjects.firstNonNull(
                extension.htmlConvention.commitFormat, extension.commitFormat
        )
        def gitExecutor = new GitCommandExecutor(commitFormat)
        return new HtmlChangelogWriter(extension, gitExecutor)
    }
}
