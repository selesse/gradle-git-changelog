package com.selesse.gradle.git.changelog.generator

import com.google.common.base.Splitter
import com.selesse.dates.FlexibleDateParser
import com.selesse.gitwrapper.fixtures.GitRepositoryBuilder
import com.selesse.gradle.git.GitCommandExecutor
import org.junit.After
import org.junit.Test

import static org.assertj.core.api.Assertions.assertThat

class ComplexChangelogGeneratorTest {
    File temporaryGitDirectory

    @After public void tearDown() {
        if (temporaryGitDirectory != null && temporaryGitDirectory.isDirectory()) {
            temporaryGitDirectory.deleteDir()
        }
    }

    @Test public void testComplexChangelog_whenOneTagIsThere() {
        GitRepositoryBuilder repository =
                GitRepositoryBuilder.create()
                    .runCommand('git init')
                    .runCommand('git', 'config', 'user.name', 'Test Account')
                    .createFile('README.md', 'Hello world!')
                    .runCommand('git add README.md')
                    .runCommand('git', 'commit', '-m', 'Initial commit')
                    .runCommand('git tag v0.1.0')
                    .build()

        temporaryGitDirectory = repository.getDirectory()

        def executor = new GitCommandExecutor('%s (%an)', temporaryGitDirectory)
        List<String> tags = executor.getTags()

        assertThat(tags).hasSize(1)

        ChangelogGenerator changelogGenerator = new ComplexChangelogGenerator(executor, tags)
        String generatedChangelog = changelogGenerator.generateChangelog()

        assertThat(generatedChangelog).endsWith(
                "----------------------------------\n" +
                        "Initial commit (Test Account)\n")
        List<String> lines = Splitter.on('\n').splitToList(generatedChangelog)
        def (tag, date) = extractTagAndDate(lines.get(0))

        assertThat(tag).isEqualTo("v0.1.0")
        Date commitDate = new FlexibleDateParser().parseDate(date)
        assertThat(commitDate).isBetween(new Date().minus(1), new Date())
    }

    @Test public void testComplexChangelog_withAnAnnotatedTag() {
        GitRepositoryBuilder repository =
                GitRepositoryBuilder.create()
                        .runCommand('git init')
                        .runCommand('git', 'config', 'user.name', 'Test Account')
                        .createFile('README.md', 'Hello world!')
                        .runCommand('git add README.md')
                        .runCommand('git', 'commit', '-m', 'Initial commit from the past')
                        .runCommand('git', 'commit', '--amend', '--date=2015-05-01 00:00:00 -0400', '-C', 'HEAD')
                        .runCommand('git', 'tag', '-a', 'v0.1.0-a', '-m', 'This is an annotated tag')
                        .build()

        temporaryGitDirectory = repository.getDirectory()

        def executor = new GitCommandExecutor('%s (%an)', temporaryGitDirectory)
        List<String> tags = executor.getTags()

        assertThat(tags).hasSize(1)

        ChangelogGenerator changelogGenerator = new ComplexChangelogGenerator(executor, tags)
        String generatedChangelog = changelogGenerator.generateChangelog()

        assertThat(generatedChangelog)
                .startsWith('v0.1.0-a (')
                .endsWith("----------------------------------\n" +
                          "Initial commit from the past (Test Account)\n")
        List<String> lines = Splitter.on('\n').splitToList(generatedChangelog)
        def (tag, date) = extractTagAndDate(lines.get(0))

        assertThat(tag).isEqualTo("v0.1.0-a")
        Date tagDate = new FlexibleDateParser().parseDate(date)
        assertThat(tagDate).isBetween(new Date().minus(1), new Date())
    }

    def extractTagAndDate(String string) {
        def matches = string =~ ~/([^\s]+) \(([^)]+)\)/

        return [matches[0][1], matches[0][2]]
    }
}
