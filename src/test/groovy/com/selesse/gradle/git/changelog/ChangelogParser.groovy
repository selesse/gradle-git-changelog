package com.selesse.gradle.git.changelog
import com.google.common.base.Splitter
import com.google.common.collect.LinkedHashMultimap
import com.google.common.collect.Multimap

import java.util.regex.Pattern

class ChangelogParser {
    List<String> tags
    Multimap<String, String> tagsAndTheirCommits

    ChangelogParser(String changelogString) {
        this.tags = []
        this.tagsAndTheirCommits = LinkedHashMultimap.create()

        parseChangelogString(changelogString)
    }

    def parseChangelogString(String changelogString) {
        def multiLineNewLine = Pattern.compile('\\r?\\n', Pattern.MULTILINE)
        List<String> changelog = Splitter.on(multiLineNewLine)
                .trimResults()
                .omitEmptyStrings()
                .splitToList(changelogString)

        def currentTag = changelog.get(0)

        for (int i = 0; i < changelog.size(); i++) {
            def currentLine = changelog.get(i)
            def nextLine = null
            if (i + 1 < changelog.size()) {
                nextLine = changelog.get(i + 1)
            }

            // The pattern is "Title\n-----" for tags
            if (nextLine != null && '-'.multiply(currentLine.length()).equals(nextLine)) {
                currentTag = currentLine
                tags.add(currentTag)
            } else {
                if (!currentLine.matches(/^-+$/)) {
                    tagsAndTheirCommits.put(currentTag, currentLine)
                }
            }
        }
    }

    def extractTagAndDate(String string) {
        def matches = string =~ ~/([^\s]+) \(([^)]+)\)/

        return [matches[0][1], matches[0][2]]
    }
}
