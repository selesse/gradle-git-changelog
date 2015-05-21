package com.selesse.gradle.git.changelog.generator

import com.selesse.gradle.git.GitCommandExecutor
import com.selesse.gradle.git.changelog.GitChangelogExtension
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

class MarkdownChangelogWriter extends BaseChangelogWriter {
    Logger logger = Logging.getLogger(MarkdownChangelogWriter)

    MarkdownChangelogWriter(GitChangelogExtension extension, GitCommandExecutor gitExecutor) {
        super(extension, gitExecutor)
    }

    @Override
    void writeChangelog(PrintStream printStream) {
        String content = generateChangelog()

        printStream.print(content)
        printStream.flush()
    }

    String generateChangelog() {
        def title = extension.title
        def heading = "$title\n${'='.multiply(title.length())}\n\n"

        return heading + generateChangelogContent()
    }

}
