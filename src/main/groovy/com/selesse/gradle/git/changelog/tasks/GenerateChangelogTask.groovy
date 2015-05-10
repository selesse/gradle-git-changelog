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
        def title = project.name
        def heading = "$title\n${'='.multiply(title.length())}\n\n"

        def changelog = ['git', 'log', '--pretty=format:%ad%x09%s (%an)'].execute().text.trim()
        println heading + changelog
        return heading + changelog
    }
}
