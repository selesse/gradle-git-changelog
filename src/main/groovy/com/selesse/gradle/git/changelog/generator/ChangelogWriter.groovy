package com.selesse.gradle.git.changelog.generator

interface ChangelogWriter {
    void writeChangelog(PrintStream printStream);
}