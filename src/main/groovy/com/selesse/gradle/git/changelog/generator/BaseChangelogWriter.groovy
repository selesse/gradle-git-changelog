package com.selesse.gradle.git.changelog.generator

import com.google.common.base.Joiner
import com.google.common.base.Splitter
import com.selesse.gradle.git.GitCommandExecutor
import com.selesse.gradle.git.changelog.GitChangelogExtension
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

    String generateChangelogContent() {
        List<Tag> tags = getTagList()

        ChangelogGenerator changelogGenerator

        if (tags.size() == 0) {
            logger.info("No tags found, generating basic changelog")
            changelogGenerator = new SimpleChangelogGenerator(gitExecutor)
        } else {
            logger.info("{} tags were found, generating complex changelog", tags.size())
            changelogGenerator = new ComplexChangelogGenerator(gitExecutor, tags, !'beginning'.equals(extension.since))
        }

        def changelog = changelogGenerator.generateChangelog()
        if (extension.includeLines || extension.processLines) {
            changelog = filterOrProcessLines(changelog)
        }
        return changelog
    }

}
