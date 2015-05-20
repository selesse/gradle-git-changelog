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

    String generateChangelogContent() {
        List<String> tags = getTagList()

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
