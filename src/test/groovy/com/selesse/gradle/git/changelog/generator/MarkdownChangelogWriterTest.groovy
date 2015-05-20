package com.selesse.gradle.git.changelog.generator
import com.selesse.gitwrapper.fixtures.GitRepositoryBuilder
import com.selesse.gradle.git.changelog.ChangelogParser
import com.selesse.gradle.git.changelog.tasks.GenerateChangelogTask
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

import static com.selesse.gradle.git.changelog.generator.BaseWriterTest.*;

class MarkdownChangelogWriterTest extends Specification {
    Project project
    GenerateChangelogTask task
    File temporaryGitDirectory

    def setup() {
        project = ProjectBuilder.builder().build()
        project.pluginManager.apply 'com.selesse.git.changelog'
        task = project.tasks.generateChangelog as GenerateChangelogTask

        project.extensions.changelog.since = 'beginning'
    }

    def cleanup() {
        if (temporaryGitDirectory != null && temporaryGitDirectory.isDirectory()) {
            temporaryGitDirectory.deleteDir()
        }
    }

    def 'filters out undesired lines'() {
        when:
        GitRepositoryBuilder repository = createGitRepositoryBuilderRunner().build()
        temporaryGitDirectory = repository.getDirectory()

        project.extensions.changelog.includeLines = { !it.contains('[ci skip]') }
        String changelogContent = writeMarkdownChangelog(createMarkdownWriter(project, temporaryGitDirectory))

        then:
        !changelogContent.contains('[ci skip]')
    }

    def 'processes lines according to the extension property'() {
        when:
        GitRepositoryBuilder repository = createGitRepositoryBuilderRunner().build()

        temporaryGitDirectory = repository.getDirectory()
        project.extensions.changelog.processLines = {
            String input = it as String
            if (input.contains('[ci skip] ')) {
                input = 'Hi mom! => ' + input.minus('[ci skip] ')
            }
            input
        }
        String changelogContent = writeMarkdownChangelog(createMarkdownWriter(project, temporaryGitDirectory))

        then:
        changelogContent.contains('Hi mom! => Remove contributors')
    }

    def 'since option includes appropriate changes'() {
        when:
        GitRepositoryBuilder repository = createGitRepositoryBuilderRunner()
                .runCommand('git tag 0.5')
                .sleepOneSecond()
                .createFile('hi-mom.md', 'hello mother')
                .runCommand('git add hi-mom.md')
                .runCommand('git', 'commit', '-m', "I don't much care for Gob")
                .runCommand('git tag 1.0')
                .createFile('test.md', 'test file')
                .runCommand('git add test.md')
                .runCommand('git', 'commit', '-m', 'Add test file')
                .createFile('another-file.md', 'This is another file')
                .runCommand('git add another-file.md')
                .runCommand('git', 'commit', '-m', 'Add another file')
                .build()

        temporaryGitDirectory = repository.getDirectory()
        project.extensions.changelog.since = 'last_tag'
        String changelogContent = writeMarkdownChangelog(createMarkdownWriter(project, temporaryGitDirectory))

        then:
        changelogContent ==
                'Unreleased\n' +
                '----------\n' +
                'Add another file (Test Account)\n' +
                'Add test file (Test Account)\n'

        when:
        project.extensions.changelog.since = '0.5'
        changelogContent = writeMarkdownChangelog(createMarkdownWriter(project, temporaryGitDirectory))

        then:
        ChangelogParser changelogParser = new ChangelogParser(changelogContent)
        changelogParser.headings.size() == 2
        changelogParser.headingsAndTheirCommits.get('Unreleased').containsAll(
                ['Add another file (Test Account)', 'Add test file (Test Account)']
        )
        def tagHeading = changelogParser.headings.get(1)
        tagHeading.startsWith('1.0')
        changelogParser.headingsAndTheirCommits.get(tagHeading).size() == 1
        changelogParser.headingsAndTheirCommits.get(tagHeading).contains("I don't much care for Gob (Test Account)")
    }


}
