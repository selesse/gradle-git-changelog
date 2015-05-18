package com.selesse.gradle.git.changelog.tasks

import com.google.common.base.Joiner
import com.google.common.base.Splitter
import com.selesse.gradle.git.GitCommandExecutor
import com.selesse.gradle.git.changelog.GitChangelogExtension
import com.selesse.gradle.git.changelog.generator.ChangelogGenerator
import com.selesse.gradle.git.changelog.generator.ComplexChangelogGenerator
import com.selesse.gradle.git.changelog.generator.SimpleChangelogGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction

class GenerateChangelogTask extends DefaultTask {
    Logger logger = Logging.getLogger(GenerateChangelogTask)

    public GenerateChangelogTask() {
        this.description = 'Generates a changelog'
        this.group = 'build'
    }

    @TaskAction
    def generateChangelog() {
        def outputDirectoryFile = project.changelog.outputDirectory as File
        def fileName = project.changelog.fileName as String
        File changelogFile = new File(outputDirectoryFile, fileName)
        outputDirectoryFile.mkdirs()

        generateChangelog(new PrintStream(new FileOutputStream(changelogFile)))
    }

    def generateChangelog(PrintStream printStream) {
        String content = generateChangelogContent()

        printStream.print(content)
        printStream.flush()

        return content
    }

    String generateChangelogContent() {
        GitChangelogExtension extension = project.changelog

        def title = extension.title
        def heading = "$title\n${'='.multiply(title.length())}\n\n"

        def changelogFormat = extension.commitFormat
        def gitCommandExecutor = new GitCommandExecutor(changelogFormat)

        return heading + generateChangelogContent(extension, gitCommandExecutor)
    }

    String generateChangelogContent(GitChangelogExtension extension, GitCommandExecutor gitExecutor) {
        def tags = gitExecutor.getTags()

        ChangelogGenerator changelogGenerator

        if (tags.size() == 0) {
            logger.info("No tags found, generating basic changelog")
            changelogGenerator = new SimpleChangelogGenerator(gitExecutor)
        } else {
            logger.info("{} tags were found, generating complex changelog", tags.size())
            changelogGenerator = new ComplexChangelogGenerator(gitExecutor, tags)
        }

        def changelog = changelogGenerator.generateChangelog()
        if (extension.includeLines || extension.processLines) {
            Iterable<String> changelogLines = Splitter.on('\n').split(changelog)
            if (extension.includeLines) {
                changelogLines = changelogLines.findAll extension.includeLines
            }
            if (extension.processLines) {
                changelogLines = changelogLines.collect extension.processLines
            }
            changelog = Joiner.on('\n').join(changelogLines)
        }
        return changelog
    }
}
