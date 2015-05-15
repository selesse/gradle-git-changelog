package com.selesse.gradle.git.changelog.generator

import com.selesse.gradle.git.GitCommandExecutor

class SimpleChangelogGenerator implements ChangelogGenerator {
    GitCommandExecutor executor

    SimpleChangelogGenerator(GitCommandExecutor executor) {
        this.executor = executor
    }

    @Override
    String generateChangelog() {
        executor.getGitChangelog()
    }
}
