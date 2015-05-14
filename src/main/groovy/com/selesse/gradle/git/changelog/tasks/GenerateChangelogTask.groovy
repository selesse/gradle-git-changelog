package com.selesse.gradle.git.changelog.tasks
import com.google.common.base.Splitter
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

        def tags = Splitter.on("\n").omitEmptyStrings().trimResults().splitToList(
                ['git', 'for-each-ref', '--format=%(objectname) | %(taggerdate)', 'refs/tags'].execute().text.trim()
        )

        ChangelogGenerator changelogGenerator

        if (tags.size() == 0) {
            logger.info("No tags found, generating basic changelog")
            changelogGenerator = new SimpleChangelogGenerator(changelogFormat)
        } else {
            logger.info("{} tags were found, generating complex changelog", tags.size())

            def tagAndDateMap = tags.collectEntries {
                def tagAndDate = Splitter.on("|").omitEmptyStrings().trimResults().splitToList(it) as List<String>
                if (tagAndDate.size() != 2) {
                    tagAndDate = [tagAndDate.get(0), null]
                }
                [(tagAndDate.get(0)):tagAndDate.get(1)]
            } as Map<String, String>

            changelogGenerator = new ComplexChangelogGenerator(changelogFormat, tagAndDateMap)
        }

        def changelog = changelogGenerator.generateChangelog()
        print heading + changelog
        return heading + changelog
    }
}
