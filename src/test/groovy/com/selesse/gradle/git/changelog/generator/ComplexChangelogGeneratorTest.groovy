package com.selesse.gradle.git.changelog.generator

import com.google.common.base.Splitter
import com.selesse.gitwrapper.fixtures.GitRepositoryBuilder
import com.selesse.gradle.git.GitCommandExecutor
import org.junit.Test

import static org.assertj.core.api.Assertions.assertThat

class ComplexChangelogGeneratorTest {
    @Test public void testComplexChangelog_whenOneTagIsThere() {
        GitRepositoryBuilder repository =
                GitRepositoryBuilder.create()
                    .runCommand('git init')
                    .createFile('README.md', 'Hello world!')
                    .runCommand('git add README.md')
                    .runCommand('git', 'commit', '-m', 'Initial commit')
                    .runCommand('git tag v0.1.0')
                    .build()

        File executionDirectory = repository.getDirectory()

        def executor = new GitCommandExecutor('%s (%an)', executionDirectory)
        List<String> tags = executor.getTags()

        assertThat(tags).hasSize(1)

        ChangelogGenerator changelogGenerator = new ComplexChangelogGenerator(executor, tags)
        String generatedChangelog = changelogGenerator.generateChangelog()

        assertThat(generatedChangelog).endsWith(
                "----------------------------------\n" +
                        "Initial commit (Alex Selesse)\n")
        List<String> lines = Splitter.on('\n').splitToList(generatedChangelog)
        def (tag, date) = extractTagAndDate(lines.get(0))

        assertThat(tag).isEqualTo("v0.1.0")
        Date commitDate = Date.parse('yyyy-MM-dd HH:mm:ss Z', date)
        assertThat(commitDate).isBetween(new Date().minus(1), new Date())
    }

    def extractTagAndDate(String string) {
        def matches = string =~ ~/([^\s]+) \(([^)]+)\)/

        return [matches[0][1], matches[0][2]]
    }
}
