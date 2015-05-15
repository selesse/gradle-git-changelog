package com.selesse.gradle.git.changelog.generator

import com.selesse.gitwrapper.fixtures.GitRepositoryBuilder
import com.selesse.gradle.git.GitCommandExecutor
import org.junit.After
import org.junit.Test

import static org.assertj.core.api.Assertions.assertThat

class SimpleChangelogGeneratorTest {
    File temporaryGitDirectory

    @After public void tearDown() {
        if (temporaryGitDirectory != null && temporaryGitDirectory.isDirectory()) {
            temporaryGitDirectory.deleteDir()
        }
    }

    @Test public void testSimpleChangelog() {
        GitRepositoryBuilder repository =
                GitRepositoryBuilder.create()
                        .runCommand('git init')
                        .runCommand('git', 'config', 'user.name', 'Test Account')
                        .createFile('README.md', 'Hello world!')
                        .runCommand('git add README.md')
                        .runCommand('git', 'commit', '-m', 'Initial commit')
                        .createFile('LICENSE.md', 'WTFPL License!')
                        .createFile('CONTRIBUTORS.md', 'Test Account')
                        .runCommand('git add LICENSE.md CONTRIBUTORS.md')
                        .runCommand('git', 'commit', '-m', 'Add license')
                        .runCommand('git rm CONTRIBUTORS.md')
                        .runCommand('git', 'commit', '-m', 'Never mind, nobody will contribute to this...\n\n' +
                            'What happens if I write a really long commit message that spans across multiple lines?')
                        .build()

        temporaryGitDirectory = repository.getDirectory()

        def executor = new GitCommandExecutor('%s (%an)', temporaryGitDirectory)

        ChangelogGenerator changelogGenerator = new SimpleChangelogGenerator(executor)
        String generatedChangelog = changelogGenerator.generateChangelog()

        assertThat(generatedChangelog).isEqualTo(
                "Never mind, nobody will contribute to this... (Test Account)\n" +
                "Add license (Test Account)\n" +
                "Initial commit (Test Account)")
    }
}
