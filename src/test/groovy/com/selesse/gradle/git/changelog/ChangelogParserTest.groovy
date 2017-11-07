package com.selesse.gradle.git.changelog

import com.selesse.gradle.git.changelog.model.Changelog
import org.junit.Test

import static org.assertj.core.api.Assertions.assertThat

class ChangelogParserTest {
    @Test void canParseThisRepositoryProperly() {
        Changelog changelog = ChangelogParser.generateChangelog(new File("."))
        def firstCommitSet = changelog.changelog.last()

        assertThat(firstCommitSet.title).isEqualTo("v0.1.0")
        assertThat(firstCommitSet.associatedCommits.size()).isEqualTo(48)

        assertThat(firstCommitSet.associatedCommits.last().id).isEqualTo('e5d3d0f70c6206056c09bca825c02131d4017736')
        assertThat(firstCommitSet.associatedCommits.first().id).isEqualTo('f5d576568c3d7802e56cf243fa8442190ebba746')
    }
}
