package com.selesse.gradle.git.changelog.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class GenerateChangelogTask extends DefaultTask {

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

        def changelog = ['git', 'log', '--pretty=format:%ad%x09%s (%an)'].execute().text.trim() + "\n"

        println heading + changelog
        return heading + changelog
    }
}
