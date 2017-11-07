package com.selesse.gradle.git.changelog.model

import org.ajoberstar.grgit.Commit
import org.ajoberstar.grgit.Tag

class TaggedCommitSet implements NamedCommitSet {
    Tag tag
    List<Commit> commits

    @Override
    String getTitle() {
        return tag.name
    }

    @Override
    List<Commit> getAssociatedCommits() {
        commits
    }
}
