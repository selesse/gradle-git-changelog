package com.selesse.gradle.git.changelog

import com.google.common.collect.Lists
import com.selesse.gradle.git.changelog.model.Changelog
import com.selesse.gradle.git.changelog.model.CommitSet
import com.selesse.gradle.git.changelog.model.CompleteChangelogCommitSet
import com.selesse.gradle.git.changelog.model.TaggedCommitSet
import com.selesse.gradle.git.changelog.model.UnreleasedCommitSet
import org.ajoberstar.grgit.Commit
import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.Tag

class ChangelogParser {
    static Changelog generateChangelog(File gitDirectory) {
        def gitRepo = Grgit.open(currentDir: gitDirectory)
        List<Tag> tags = gitRepo.tag.list()
        Changelog changelog = buildChangelog(gitRepo, tags)
        gitRepo.close()
        return changelog
    }

    static Changelog generateChangelog(File gitDirectory, List<Tag> tags) {
        def gitRepo = Grgit.open(currentDir: gitDirectory)
        Changelog changelog = buildChangelog(gitRepo, tags)
        gitRepo.close()
        return changelog
    }

    static Changelog buildChangelog(Grgit gitRepo, List<Tag> tags) {
        Changelog changelog = new Changelog()

        if (tags.isEmpty()) {
            changelog.changelog << new CompleteChangelogCommitSet(commits: gitRepo.log())
            return changelog
        }

        tags.sort {
            t1, t2 -> t2.commit.date <=> t1.commit.date
        }

        // Build the first group: unreleased commits -- it might be empty if HEAD is a tag
        CommitSet unreleasedCommitSet = buildUnreleasedCommitSet(gitRepo, tags.first())
        if (unreleasedCommitSet.associatedCommits.size() > 0) {
            changelog.changelog << buildUnreleasedCommitSet(gitRepo, tags.first())
        }

        // Build the remaining groups by building the ranges of commits in chunks of two
        for (int i = 0; (i + 1) < tags.size(); i++) {
            Tag thisTag = tags.get(i)
            Tag previousTag = tags.get(i + 1)

            changelog.changelog << buildTaggedCommitSet(gitRepo, thisTag, previousTag)
        }

        changelog.changelog << buildTaggedCommitSetFromBeginning(gitRepo, tags.last())

        changelog
    }

    static CommitSet buildUnreleasedCommitSet(Grgit gitRepo, Tag lastTag) {
        Commit head = gitRepo.head()
        List<Commit> commits = gitRepo.log {
            range(lastTag.commit.id, head.id)
        }
        new UnreleasedCommitSet(commits: commits)
    }

    static CommitSet buildTaggedCommitSet(Grgit gitRepo, Tag thisTag, Tag previousTag) {
        List<Commit> commits = gitRepo.log {
            range(previousTag.commit.id, thisTag.commit.id)
        }
        new TaggedCommitSet(tag: thisTag, commits: commits)
    }

    static CommitSet buildTaggedCommitSetFromBeginning(Grgit gitRepo, Tag tag) {
        Commit firstCommit = gitRepo.log().last()
        List<Commit> commits = gitRepo.log {
            range(firstCommit.id, tag.commit.id)
        }
        List<Commit> newCommits = Lists.newArrayList(commits)
        newCommits.push(firstCommit)
        new TaggedCommitSet(tag: tag, commits: newCommits)
    }
}
