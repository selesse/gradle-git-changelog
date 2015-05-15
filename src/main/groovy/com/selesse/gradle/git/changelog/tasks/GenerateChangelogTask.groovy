package com.selesse.gradle.git.changelog.tasks

import com.selesse.gradle.git.GitCommandExecutor
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

    private String generateChangelogContent() {
        def title = project.changelog.title
        def heading = "$title\n${'='.multiply(title.length())}\n\n"
        // TODO(AS): Externalize this into plugin property
        def changelogFormat = '%ad%x09%s (%an)'

        def gitCommandExecutor = new GitCommandExecutor(changelogFormat)
        def tags = gitCommandExecutor.getTags()

        ChangelogGenerator changelogGenerator

        if (tags.size() == 0) {
            logger.info("No tags found, generating basic changelog")
            changelogGenerator = new SimpleChangelogGenerator(gitCommandExecutor)
        } else {
            logger.info("{} tags were found, generating complex changelog", tags.size())
            changelogGenerator = new ComplexChangelogGenerator(gitCommandExecutor, tags)
        }

        def changelog = changelogGenerator.generateChangelog()
        print heading + changelog
        return heading + changelog
    }
}
