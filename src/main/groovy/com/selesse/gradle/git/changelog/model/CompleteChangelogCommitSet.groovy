package com.selesse.gradle.git.changelog.model

import org.ajoberstar.grgit.Commit

class CompleteChangelogCommitSet implements CommitSet {
    List<Commit> commits

    @Override
    List<Commit> getAssociatedCommits() {
        return commits
    }
}
