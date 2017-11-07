package com.selesse.gradle.git.changelog.generator

import com.google.common.base.Joiner
import com.google.common.base.Splitter
import com.selesse.gradle.git.GitCommandExecutor
import com.selesse.gradle.git.changelog.ChangelogParser
import com.selesse.gradle.git.changelog.GitChangelogExtension
import com.selesse.gradle.git.changelog.model.Changelog
import org.ajoberstar.grgit.Tag
import org.gradle.api.GradleException
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

abstract class BaseChangelogWriter implements ChangelogWriter {
    Logger logger = Logging.getLogger(BaseChangelogWriter)
    GitChangelogExtension extension
    GitCommandExecutor gitExecutor

    BaseChangelogWriter(GitChangelogExtension extension, GitCommandExecutor gitExecutor) {
        this.extension = extension
        this.gitExecutor = gitExecutor
    }

    String filterOrProcessLines(String changelog) {
        Iterable<String> changelogLines = Splitter.on('\n').split(changelog)
        if (extension.includeLines) {
            changelogLines = changelogLines.findAll extension.includeLines
        }
        if (extension.processLines) {
            changelogLines = changelogLines.collect extension.processLines
        }
        Joiner.on('\n').join(changelogLines)
    }

    List<Tag> getTagList() {
        def tags
        if (extension.since == 'last_tag') {
            def lastTag = gitExecutor.getLastTagOrNull()
            if (lastTag == null) {
                throw new GradleException('"last_tag" option specified, but no tags were found')
            }
            tags = [lastTag]
        } else if (extension.since == 'beginning') {
            tags = gitExecutor.getTags()
        } else {
            tags = gitExecutor.getTagsSince(extension.since)
            if (tags.isEmpty()) {
                throw new GradleException("No tags found since '${extension.since}'")
            }
        }
        tags
    }

    Changelog generateChangelog() {
        List<Tag> tags = getTagList()
        if (tags.size() == 0) {
            logger.info("No tags found, generating basic changelog")
        } else {
            logger.info("{} tags were found, generating complex changelog", tags.size())
        }
        ChangelogParser.generateChangelog(gitExecutor.executionContext, tags)
    }
}
