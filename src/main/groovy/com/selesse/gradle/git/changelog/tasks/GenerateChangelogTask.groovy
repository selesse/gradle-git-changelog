package com.selesse.gradle.git.changelog.tasks

import com.selesse.gradle.git.GitCommandExecutor
import com.selesse.gradle.git.changelog.GitChangelogExtension
import com.selesse.gradle.git.changelog.generator.ChangelogWriter
import com.selesse.gradle.git.changelog.generator.HtmlChangelogWriter
import com.selesse.gradle.git.changelog.generator.MarkdownChangelogWriter
import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction

class GenerateChangelogTask extends DefaultTask {
    Logger logger = Logging.getLogger(GenerateChangelogTask)
    GitChangelogExtension extension

    public GenerateChangelogTask() {
        this.description = 'Generates a changelog'
        this.group = 'build'
    }

    @TaskAction
    def generateChangelog() {
        extension = project.extensions.changelog

        def outputDirectoryFile = extension.outputDirectory
        outputDirectoryFile.mkdirs()

        extension.formats.each {
            String format = it as String

            ChangelogWriter changelogWriter
            def gitExecutor = new GitCommandExecutor(extension.commitFormat)
            if (format == "markdown") {
                format = "md"
                changelogWriter = new MarkdownChangelogWriter(extension, gitExecutor)
            } else {
                changelogWriter = new HtmlChangelogWriter(extension, gitExecutor)
            }

            String fileName = extension.fileName
            // i.e. CHANGELOG.md -> CHANGELOG.html
            fileName = fileName.substring(0, fileName.lastIndexOf('.')) + ".${format}"

            File changelogFile = new File(outputDirectoryFile, fileName)

            changelogWriter.writeChangelog(new PrintStream(new FileOutputStream(changelogFile)))
        }
    }
}
