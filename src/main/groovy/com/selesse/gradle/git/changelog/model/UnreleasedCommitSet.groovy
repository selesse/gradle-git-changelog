package com.selesse.gradle.git.changelog.model

import org.ajoberstar.grgit.Commit

class UnreleasedCommitSet implements NamedCommitSet {
    List<Commit> commits

    @Override
    String getTitle() {
        return 'Unreleased'
    }

    @Override
    List<Commit> getAssociatedCommits() {
        return commits
    }
}
