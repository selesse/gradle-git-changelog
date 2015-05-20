package com.selesse.gradle.git.changelog.generator

import com.google.common.base.Joiner
import com.google.common.base.Splitter
import com.selesse.gradle.git.GitCommandExecutor
import com.selesse.gradle.git.changelog.GitChangelogExtension
import org.gradle.api.GradleException

abstract class BaseChangelogWriter implements ChangelogWriter {
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

    List<String> getTagList() {
        def tags
        if (extension.since == 'last_tag') {
            def lastTag = gitExecutor.getLastTag()
            if (lastTag.isEmpty()) {
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
}
