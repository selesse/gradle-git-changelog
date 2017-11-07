package com.selesse.gradle.git

import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.Tag

class GitCommandExecutor {
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
}
