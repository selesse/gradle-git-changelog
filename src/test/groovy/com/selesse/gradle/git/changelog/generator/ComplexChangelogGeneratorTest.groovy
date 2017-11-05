package com.selesse.gradle.git.changelog.generator

import com.selesse.dates.FlexibleDateParser
import com.selesse.gitwrapper.fixtures.GitRepositoryBuilder
import com.selesse.gradle.git.GitCommandExecutor
import com.selesse.gradle.git.changelog.ChangelogParser
import groovy.time.TimeCategory
import org.ajoberstar.grgit.Tag
import org.junit.After
import org.junit.Test

import static org.assertj.core.api.Assertions.assertThat

class ComplexChangelogGeneratorTest {
    GitRepositoryBuilder repository

    @After public void tearDown() {
        if (repository != null) {
            repository.cleanUp()
        }
    }

    @Test public void testComplexChangelog_whenOneTagIsThere() {
        repository =
                GitRepositoryBuilder.create()
                    .runCommand('git init')
                    .runCommand('git', 'config', 'user.name', 'Test Account')
                    .runCommand('git', 'config', 'user.email', 'test@example.com')
                    .createFile('README.md', 'Hello world!')
                    .runCommand('git add README.md')
                    .runCommand('git', 'commit', '-m', 'Initial commit')
                    .runCommand('git tag v0.1.0')
                    .build()

        def executor = new GitCommandExecutor('%s (%an)', repository.getDirectory())
        List<Tag> tags = executor.getTags()

        ChangelogGenerator changelogGenerator = new ComplexChangelogGenerator(executor, tags, false)
        String generatedChangelog = changelogGenerator.generateChangelog()

        def changelogParser = new ChangelogParser(generatedChangelog)
        assertThat(changelogParser.headings).hasSize(1)

        def changelogTag = changelogParser.headings.get(0)
        def (tag, date) = ChangelogParser.extractTagAndDate(changelogTag)

        assertThat(tag).isEqualTo('v0.1.0')
        assertThat(dateOccurredInLastDay(date)).isTrue()

        assertThat(changelogParser.headingsAndTheirCommits.get(changelogTag))
                .containsOnly('Initial commit (Test Account)')
    }

    @Test public void testComplexChangelog_withAnAnnotatedTag_usesTheAnnotatedTagDate() {
        repository =
                GitRepositoryBuilder.create()
                        .runCommand('git init')
                        .runCommand('git', 'config', 'user.name', 'Test Account')
                        .runCommand('git', 'config', 'user.email', 'test@example.com')
                        .createFile('README.md', 'Hello world!')
                        .runCommand('git add README.md')
                        .runCommand('git', 'commit', '-m', 'Initial commit from the past')
                        .runCommand('git', 'commit', '--amend', '--date=2015-05-01 00:00:00 -0400', '-C', 'HEAD')
                        .runCommand('git', 'tag', '-a', 'v0.1.0-a', '-m', 'This is an annotated tag')
                        .build()

        def executor = new GitCommandExecutor('%s (%an)', repository.getDirectory())
        List<Tag> tags = executor.getTags()

        assertThat(tags).hasSize(1)

        ChangelogGenerator changelogGenerator = new ComplexChangelogGenerator(executor, tags, false)
        String generatedChangelog = changelogGenerator.generateChangelog()

        def changelogParser = new ChangelogParser(generatedChangelog)
        assertThat(changelogParser.headings).hasSize(1)

        def changelogTag = changelogParser.headings.get(0)
        def (tag, date) = ChangelogParser.extractTagAndDate(changelogTag)

        assertThat(tag).isEqualTo('v0.1.0-a')
        // This assertion is important: an annotated tag date should take precedence over a commit date
        assertThat(dateOccurredInLastDay(date)).isTrue()

        assertThat(changelogParser.headingsAndTheirCommits.get(changelogTag))
                .containsOnly('Initial commit from the past (Test Account)')
    }

    @Test public void testComplexChangelog_withUnreleasedAndAnnotatedAndUnannotatedTags() {
        repository =
                GitRepositoryBuilder.create()
                        .runCommand('git init')
                        .runCommand('git', 'config', 'user.name', 'Test Account')
                        .runCommand('git', 'config', 'user.email', 'test@example.com')
                        .createFile('README.md', 'Hello world!')
                        .runCommand('git add README.md')
                        .runCommand('git', 'commit', '-m', 'Initial commit from the past')
                        .runCommand('git', 'commit', '--amend', '--date=2015-05-01 00:00:00 -0400', '-C', 'HEAD')
                        .runCommand('git', 'tag', '-a', 'v0.1.0-a', '-m', 'This is an annotated tag')
                        .createFile('CONTRIBUTORS.md', 'This is a list of contributors to this project')
                        .runCommand('git add CONTRIBUTORS.md')
                        .runCommand('git', 'commit', '-m', 'Add contributors')
                        .runCommand('git tag v0.2.0-l')
                        .runCommand('git rm CONTRIBUTORS.md')
                        .runCommand('git', 'commit', '-m', 'Changed my mind - no contributors')
                        .build()

        def executor = new GitCommandExecutor('%s (%an)', repository.getDirectory())
        List<Tag> tags = executor.getTags()

        ChangelogGenerator changelogGenerator = new ComplexChangelogGenerator(executor, tags, false)
        String generatedChangelog = changelogGenerator.generateChangelog()

        def changelogParser = new ChangelogParser(generatedChangelog)
        def headings = changelogParser.headings
        assertThat(headings).hasSize(3)

        def unreleasedHeading = 'Unreleased'
        assertThat(headings.get(0)).isEqualTo(unreleasedHeading)
        assertThat(headings.get(1)).startsWith('v0.2.0-l')
        assertThat(headings.get(2)).startsWith('v0.1.0-a')
        def lightWeightTag = headings.get(1)
        def annotatedTag = headings.get(2)

        assertThat(changelogParser.headingsAndTheirCommits.get(unreleasedHeading))
                .containsOnly('Changed my mind - no contributors (Test Account)')
        assertThat(changelogParser.headingsAndTheirCommits.get(lightWeightTag))
                .containsOnly('Add contributors (Test Account)')
        assertThat(changelogParser.headingsAndTheirCommits.get(annotatedTag))
                .containsOnly('Initial commit from the past (Test Account)')

        def (tag, date) = ChangelogParser.extractTagAndDate(lightWeightTag)
        assertThat(tag).isEqualTo('v0.2.0-l')
        assertThat(dateOccurredInLastDay(date)).isTrue()

        (tag, date) = ChangelogParser.extractTagAndDate(annotatedTag)
        assertThat(tag).isEqualTo('v0.1.0-a')
        assertThat(dateOccurredInLastDay(date)).isTrue()
    }

    static boolean dateOccurredInLastDay(String dateFormattedString) {
        Date commitDate = new FlexibleDateParser().parseDate(dateFormattedString)
        // This assertion is important: an annotated tag date should take precedence over a commit date
        TimeCategory.minus(new Date(), commitDate).days <= 1
    }
}
