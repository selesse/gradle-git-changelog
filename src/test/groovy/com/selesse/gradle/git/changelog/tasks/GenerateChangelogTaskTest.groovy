package com.selesse.gradle.git.changelog.tasks
import com.selesse.gitwrapper.fixtures.GitRepositoryBuilder
import com.selesse.gitwrapper.fixtures.GitRepositoryBuilderRunner
import com.selesse.gradle.git.GitCommandExecutor
import com.selesse.gradle.git.changelog.ChangelogParser
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class GenerateChangelogTaskTest extends Specification {
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

    def 'title is included in changelog'() {
        when:
        def changelog = task.generateChangelog()

        then:
        changelog.contains(project.name)

        when:
        project.extensions.changelog.title = 'Non-default plugin title'
        def byteArrayOutputStream = new ByteArrayOutputStream()
        def stream = new PrintStream(byteArrayOutputStream)
        changelog = task.generateChangelog(stream)

        then:
        changelog.contains('Non-default plugin title')
        byteArrayOutputStream.toString('UTF-8') == changelog
    }

    def 'filters out undesired lines'() {
        when:
        GitRepositoryBuilder repository = createGitRepositoryBuilderRunner().build()

        temporaryGitDirectory = repository.getDirectory()
        GitCommandExecutor gitCommandExecutor = new GitCommandExecutor('%s (%an)', temporaryGitDirectory)

        project.extensions.changelog.includeLines = { !it.contains('[ci skip]') }
        task.extension = project.extensions.changelog

        String changelogContent = task.generateChangelogContent(gitCommandExecutor)

        then:
        !changelogContent.contains('[ci skip]')
    }

    def 'processes lines according to the extension property'() {
        when:
        GitRepositoryBuilder repository = createGitRepositoryBuilderRunner().build()

        temporaryGitDirectory = repository.getDirectory()
        GitCommandExecutor gitCommandExecutor = new GitCommandExecutor('%s (%an)', temporaryGitDirectory)

        project.extensions.changelog.processLines = {
            String input = it as String
            if (input.startsWith('[ci skip]')) {
                input = "Hi mom! => ${input.minus('[ci skip] ')}"
            }
            input
        }
        task.extension = project.extensions.changelog

        String changelogContent = task.generateChangelogContent(gitCommandExecutor)

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

        GitCommandExecutor gitCommandExecutor = new GitCommandExecutor('%s (%an)', temporaryGitDirectory)
        project.extensions.changelog.since = 'last_tag'
        task.extension = project.extensions.changelog
        String changelogContent = task.generateChangelogContent(gitCommandExecutor)

        then:
        changelogContent ==
                'Unreleased\n' +
                '----------\n' +
                'Add another file (Test Account)\n' +
                'Add test file (Test Account)\n'

        when:
        project.extensions.changelog.since = '0.5'
        task.extension = project.extensions.changelog
        changelogContent = task.generateChangelogContent(gitCommandExecutor)

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

    private static GitRepositoryBuilderRunner createGitRepositoryBuilderRunner() {
        GitRepositoryBuilder.create()
                .runCommand('git init')
                .runCommand('git', 'config', 'user.name', 'Test Account')
                .createFile('README.md', 'Hello world!')
                .runCommand('git add README.md')
                .runCommand('git', 'commit', '-m', 'Initial commit from the past')
                .runCommand('git', 'commit', '--amend', '--date=2015-05-01 00:00:00 -0400', '-C', 'HEAD')
                .createFile('CONTRIBUTORS.md', 'This is a list of contributors to this project')
                .runCommand('git add CONTRIBUTORS.md')
                .runCommand('git', 'commit', '-m', 'Add contributors')
                .runCommand('git rm CONTRIBUTORS.md')
                .runCommand('git', 'commit', '-m', '[ci skip] Remove contributors')
    }

}
