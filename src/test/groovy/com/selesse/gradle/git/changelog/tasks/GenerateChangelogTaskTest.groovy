package com.selesse.gradle.git.changelog.tasks

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class GenerateChangelogTaskTest extends Specification {

    def "title is included in changelog"() {
        Project project = ProjectBuilder.builder().build()
        project.pluginManager.apply 'com.selesse.git.changelog'

        GenerateChangelogTask task = project.tasks.generateChangelog as GenerateChangelogTask

        when:
        def changelog = task.generateChangelog()

        then:
        changelog.contains(project.name)

        when:
        project.extensions.changelog.title = 'Non-default plugin title'
        changelog = task.generateChangelog()

        then:
        changelog.contains('Non-default plugin title')
    }

}
