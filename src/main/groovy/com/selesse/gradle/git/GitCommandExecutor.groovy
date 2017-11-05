package com.selesse.gradle.git

import com.selesse.gradle.git.changelog.generator.ComplexChangelogGenerator
import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.Tag
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

class GitCommandExecutor {
    Logger logger = Logging.getLogger(ComplexChangelogGenerator)
    File executionContext
    private String changelogFormat

    GitCommandExecutor(String changelogFormat) {
        this.changelogFormat = changelogFormat
    }

    GitCommandExecutor(String changelogFormat, File context) {
        this.changelogFormat = changelogFormat
        this.executionContext = context
    }

    List<Tag> getTags() {
        def grgit = Grgit.open(currentDir: executionContext)
        def tags = grgit.tag.list()
        grgit.close()
        return tags
    }

    Tag getLastTagOrNull() {
        def tags = getTags()
        if (tags.size() == 0) {
            return null
        }
        return tags.last()
    }

    List<Tag> getTagsSince(String ref) {
        def grgit = Grgit.open(currentDir: executionContext)
        def tags = grgit.tag.list()
        def tagsSince = tags.findAll {
            ref != it.name && grgit.isAncestorOf(ref, it)
        }
        grgit.close()
        return tagsSince
    }

    private String executeCommand(String... args) {
        if (executionContext != null) {
            args.execute(null, executionContext).text.trim()
        } else {
            args.execute().text.trim()
        }
    }

    public String getCommitDate(String commit) {
        executeCommand('git', 'log', '-1', '--format=%ai', commit)
    }

    private String[] getBaseGitCommand() {
        ['git', 'log', "--pretty=format:${changelogFormat}"]
    }

    String getGitChangelog() {
        executeCommand('git', 'log', "--pretty=format:${changelogFormat}")
    }

    String getGitChangelog(String reference) {
        logger.info("Getting Git changelog for {}", reference)
        executeCommand((getBaseGitCommand() + reference) as String[])
    }

    String getGitChangelog(String firstReference, String secondReference) {
        logger.info("Getting Git changelog for {}...{}", firstReference, secondReference)
        executeCommand((getBaseGitCommand() + "${firstReference}...${secondReference}") as String[])
    }

    String getTagName(String commit) {
        executeCommand('git', 'describe', '--tags', commit)
    }

    String getTagDate(String tag) {
        executeCommand('git', 'log', '-1', '--format=%ai', tag)
    }

    String getLatestCommit() {
        executeCommand('git', 'log', '-1', '--pretty=format:%H')
    }
}
