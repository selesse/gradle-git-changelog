package com.selesse.gradle.git.changelog.functional

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class GitLogBaseFunctionalTest extends Specification {
    @Rule final TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
    }

    def "when using base plugin, and output directory is unspecified, defaults to 'build'"() {
        given:
        buildFile << """
        plugins {
            id 'com.selesse.git.changelog'
        }
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('generateChangelog')
                .withPluginClasspath()
                .build()

        then:
        new File(testProjectDir.root, "build/CHANGELOG.md").isFile()
        result.task(":generateChangelog").outcome == SUCCESS
    }

    def "when using base plugin, and output directory is specified, it can be customized"() {
        given:
        buildFile << """
        plugins {
            id 'com.selesse.git.changelog'
        }

        changelog {
            outputDirectory = new File(project.rootDir, 'some/nested/structure')
        }
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('generateChangelog')
                .withPluginClasspath()
                .build()

        then:
        new File(testProjectDir.root, "some/nested/structure/CHANGELOG.md").isFile()
        result.task(":generateChangelog").outcome == SUCCESS
    }
}

