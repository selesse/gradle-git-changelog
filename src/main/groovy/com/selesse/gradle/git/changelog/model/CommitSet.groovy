package com.selesse.gradle.git.changelog.model

import org.ajoberstar.grgit.Commit

interface CommitSet {
    List<Commit> getAssociatedCommits()
}
