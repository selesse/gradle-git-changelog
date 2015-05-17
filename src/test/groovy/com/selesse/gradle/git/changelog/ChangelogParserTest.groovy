package com.selesse.gradle.git.changelog

import org.junit.Test

import static org.assertj.core.api.Assertions.assertThat

class ChangelogParserTest {
    @Test public void testCanParseChangelogFromChangelogString() {
        def changelogString = """Unreleased
----------
Wed May 13 22:48:19 2015 -0400\tRemove junk (Alex Selesse)

lw-0.2.0 (2015-05-13 20:13:16 -0400)
------------------------------------
Wed May 13 20:13:16 2015 -0400\tAnother commit (Alex Selesse)
Wed May 13 20:06:46 2015 -0400\tCommit #2 (Alex Selesse)

v0.1.0 (Wed May 13 20:00:37 2015 -0400)
---------------------------------------
Wed May 13 20:00:15 2015 -0400\tInitial commit (Alex Selesse)"""

        def unreleasedTag = 'Unreleased'
        def lightweightTag = 'lw-0.2.0 (2015-05-13 20:13:16 -0400)'
        def annotatedTag = 'v0.1.0 (Wed May 13 20:00:37 2015 -0400)'
        def changelogParser = new ChangelogParser(changelogString)
        assertThat(changelogParser.headings).containsOnly(
                unreleasedTag, lightweightTag, annotatedTag
        )
        assertThat(changelogParser.headingsAndTheirCommits.asMap()).containsOnlyKeys(
                unreleasedTag, lightweightTag, annotatedTag
        )

        def unreleasedCommits = changelogParser.headingsAndTheirCommits.get(unreleasedTag)
        assertThat(unreleasedCommits).containsOnly('Wed May 13 22:48:19 2015 -0400\tRemove junk (Alex Selesse)')

        def lightweightCommits = changelogParser.headingsAndTheirCommits.get(lightweightTag)
        assertThat(lightweightCommits).containsOnly(
                'Wed May 13 20:13:16 2015 -0400\tAnother commit (Alex Selesse)',
                'Wed May 13 20:06:46 2015 -0400\tCommit #2 (Alex Selesse)'
        )

        def annotatedCommits = changelogParser.headingsAndTheirCommits.get(annotatedTag)
        assertThat(annotatedCommits).containsOnly(
                'Wed May 13 20:00:15 2015 -0400\tInitial commit (Alex Selesse)',
        )
    }

}
