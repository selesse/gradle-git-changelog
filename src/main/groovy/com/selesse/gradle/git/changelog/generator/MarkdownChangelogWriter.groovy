package com.selesse.gradle.git.changelog.generator

import com.selesse.gradle.git.GitCommandExecutor
import com.selesse.gradle.git.changelog.GitChangelogExtension
import com.selesse.gradle.git.changelog.model.Changelog
import com.selesse.gradle.git.changelog.model.CommitSet
import com.selesse.gradle.git.changelog.model.NamedCommitSet
import com.selesse.gradle.git.changelog.model.TaggedCommitSet
import org.ajoberstar.grgit.Commit
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

class MarkdownChangelogWriter extends BaseChangelogWriter {
    Logger logger = Logging.getLogger(MarkdownChangelogWriter)

    MarkdownChangelogWriter(GitChangelogExtension extension, GitCommandExecutor gitExecutor) {
        super(extension, gitExecutor)
    }

    @Override
    void writeChangelog(PrintStream printStream) {
        String content = generateChangelogString()

        printStream.print(content)
        printStream.flush()
    }

    String generateChangelogString() {
        def title = extension.title
        def heading = "# $title\n\n"

        return heading + generateChangelogContent()
    }

    String generateChangelogContent() {
        def changelog = generateChangelog()
        def changelogString = generateChangelogString(changelog)
        if (extension.includeLines || extension.processLines) {
            changelogString = filterOrProcessLines(changelogString)
        }
        return changelogString
    }

    static String generateChangelogString(Changelog changelog) {
        StringBuilder changelogOutput = new StringBuilder()

        for (CommitSet commitSet : changelog.changelog) {
            changelogOutput.append(printSectionTitle(commitSet))
            for (Commit commit : commitSet.associatedCommits) {
                changelogOutput.append('* ').append(commit.shortMessage).append('\n')
            }
            changelogOutput.append('\n')
        }

        return changelogOutput.toString()
    }

    private static String printSectionTitle(CommitSet commitSet) {
        StringBuilder title = new StringBuilder()
        if (commitSet instanceof NamedCommitSet) {
            title.append('# ').append(commitSet.title)
        }
        if (commitSet instanceof TaggedCommitSet) {
            // TODO: Grgit Tag objects don't seem to have dates associated to them, so this is wrong
            title.append(' (').append(commitSet.tag.commit.date).append(')')
        }
        if (commitSet instanceof NamedCommitSet) {
            title.append('\n')
        }
        return title.toString()
    }
}
