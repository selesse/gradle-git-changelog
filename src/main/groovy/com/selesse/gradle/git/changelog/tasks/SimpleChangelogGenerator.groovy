package com.selesse.gradle.git.changelog.tasks

class SimpleChangelogGenerator implements ChangelogGenerator {
    String changelogFormat

    SimpleChangelogGenerator(String changelogFormat) {
        this.changelogFormat = changelogFormat
    }

    @Override
    String generateChangelog() {
        return ['git', 'log', "--pretty=format:${changelogFormat}"].execute().text.trim()
    }
}
