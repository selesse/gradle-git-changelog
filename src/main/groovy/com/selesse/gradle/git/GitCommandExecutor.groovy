package com.selesse.gradle.git

import com.google.common.base.Splitter
import com.selesse.gradle.git.changelog.generator.ComplexChangelogGenerator
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

class GitCommandExecutor {
    Logger logger = Logging.getLogger(ComplexChangelogGenerator)
    private File executionContext
    private String changelogFormat

    GitCommandExecutor(String changelogFormat) {
        this.changelogFormat = changelogFormat
    }

    GitCommandExecutor(String changelogFormat, File context) {
        this.changelogFormat = changelogFormat
        this.executionContext = context
    }


    public List<String> getTags() {
        Splitter.on("\n").omitEmptyStrings().trimResults().splitToList(
                executeCommand('git', 'for-each-ref', '--format=%(objectname) | %(taggerdate)', 'refs/tags')
        )
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

    public String getGitChangelog() {
        executeCommand('git', 'log', "--pretty=format:${changelogFormat}")
    }

    public String getGitChangelog(String reference) {
        logger.info("Getting Git changelog for {}", reference)
        executeCommand((getBaseGitCommand() + reference) as String[])
    }

    public String getGitChangelog(String firstReference, String secondReference) {
        logger.info("Getting Git changelog for {}...{}", firstReference, secondReference)
        executeCommand((getBaseGitCommand() + "${firstReference}...${secondReference}") as String[])
    }

    public String getTagName(String commit) {
        executeCommand('git', 'describe', '--tags', commit)
    }

    public String getTagDate(String tag) {
        executeCommand('git', 'log', '-1', '--format=%ai', tag)
    }

    public String getLatestCommit() {
        executeCommand('git', 'log', '-1', '--pretty=format:%H')
    }
}
