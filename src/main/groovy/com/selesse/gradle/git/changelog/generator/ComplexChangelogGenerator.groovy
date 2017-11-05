package com.selesse.gradle.git.changelog.generator

import com.google.common.base.Joiner
import com.google.common.base.MoreObjects
import com.selesse.gradle.git.GitCommandExecutor
import org.ajoberstar.grgit.Commit
import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.Tag

class ComplexChangelogGenerator implements ChangelogGenerator {
    final List<Tag> tags
    final Map<String, String> tagAndDateMap
    final GitCommandExecutor executor
    final boolean skipFirstTag

    ComplexChangelogGenerator(GitCommandExecutor executor, List<Tag> tags, boolean skipFirstTag) {
        this.executor = executor
        this.tags = tags
        this.tagAndDateMap = tags.collectEntries {
            [it.name, it.commit.date.toString()]
        } as Map<String, String>
        this.skipFirstTag = skipFirstTag
    }

    String generateChangelog() {
        List<String> changelogs = []

        def grgit = Grgit.open(currentDir: executor.executionContext)

        List<Map<Tag, Commit>> changelog = []
        // TODO: organize data structure like this, THEN print it

        def dateCommitMap = tagAndDateMap.keySet().collectEntries {
            [(executor.getCommitDate(it)): it]
        } as Map<String, String>

        def dates = dateCommitMap.keySet().sort()

        if (!skipFirstTag) {
            appendFirstCommitChangeLog(dates, dateCommitMap, tagAndDateMap, changelogs)
        }

        for (int i = 0; (i + 1) < dates.size(); i++) {
            def firstCommit = dateCommitMap.get(dates.get(i))

            def secondCommitDate = dates.get(i + 1)
            def secondCommit = dateCommitMap.get(secondCommitDate)

            secondCommitDate = MoreObjects.firstNonNull(tagAndDateMap.get(secondCommit), executor.getTagDate(secondCommit))

            def sectionTitle = "${executor.getTagName(secondCommit)} (${secondCommitDate})"
            def sectionChangelog = executor.getGitChangelog(firstCommit, secondCommit)

            changelogs << getChangelogSection(sectionTitle, sectionChangelog)
        }

        appendLastCommitChangelog(dateCommitMap, dates, changelogs)

        def reverseChangelogs = changelogs.reverse()
        grgit.close()
        return Joiner.on("\n").join(reverseChangelogs)
    }

    private List appendFirstCommitChangeLog(List<String> dates, Map<String, String> dateCommitMap, Map<String,
            String> tagAndDateMap, List<String> changelogs) {
        def secondCommitDate = dates.get(0)
        def secondCommit = dateCommitMap.get(secondCommitDate)

        secondCommitDate = MoreObjects.firstNonNull(tagAndDateMap.get(secondCommit), executor.getTagDate(secondCommit))

        def sectionTitle = "${executor.getTagName(secondCommit)} (${secondCommitDate})"
        def sectionChangelog = executor.getGitChangelog(secondCommit)
        changelogs << getChangelogSection(sectionTitle, sectionChangelog)
    }

    private void appendLastCommitChangelog(Map<String, String> dateCommitMap, List<String> dates, List<String> changelogs) {
        def firstCommit = dateCommitMap.get(dates.last())
        def secondCommit = executor.getLatestCommit()
        def changelog = executor.getGitChangelog(firstCommit, secondCommit)
        if (changelog.length() > 0) {
            changelogs << getChangelogSection("Unreleased", changelog)
        }
    }

    String getChangelogSection(String sectionTitle, String sectionChangelog) {
        StringBuilder changelogSection = new StringBuilder()
        changelogSection.append(sectionTitle)
                .append('\n')
                .append('-'.multiply(sectionTitle.length()))
                .append('\n')
                .append(sectionChangelog)
                .append('\n')
        return changelogSection.toString()
    }

}
