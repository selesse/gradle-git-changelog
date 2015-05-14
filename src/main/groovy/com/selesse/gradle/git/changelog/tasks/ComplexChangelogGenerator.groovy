package com.selesse.gradle.git.changelog.tasks

import com.google.common.base.Joiner
import com.google.common.base.MoreObjects
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

class ComplexChangelogGenerator implements ChangelogGenerator {
     Logger logger = Logging.getLogger(ComplexChangelogGenerator)
     final String changelogFormat
     final Map<String, String> tagAndDateMap

     ComplexChangelogGenerator(String changelogFormat, Map<String, String> tagAndDateMap) {
          this.changelogFormat = changelogFormat
          this.tagAndDateMap = tagAndDateMap
     }

     String generateChangelog() {
          List<String> changelogs = []

          def dateCommitMap = tagAndDateMap.keySet().collectEntries {
               [(['git', 'log', '-1', '--format=%ai', it].execute().text.trim()): it]
          } as Map<String, String>

          def dates = (dateCommitMap.keySet() as List<String>).sort()

          appendFirstCommitChangeLog(dates, dateCommitMap, tagAndDateMap, changelogs)

          for (int i = 0; (i + 1) < dates.size(); i++) {
               def firstCommit = dateCommitMap.get(dates.get(i))

               def secondCommitDate = dates.get(i + 1)
               def secondCommit = dateCommitMap.get(secondCommitDate)

               secondCommitDate = MoreObjects.firstNonNull(tagAndDateMap.get(secondCommit), getTagDate(secondCommit))

               def sectionTitle = "${getTagName(secondCommit)} (${secondCommitDate})"
               def sectionChangelog = getGitChangelog(firstCommit, secondCommit)

               changelogs << getChangelogSection(sectionTitle, sectionChangelog)
          }

          appendLastCommitChangelog(dateCommitMap, dates, changelogs)

          def reverseChangelogs = changelogs.reverse()
          return Joiner.on("\n").join(reverseChangelogs)
     }

     private void appendLastCommitChangelog(Map<String, String> dateCommitMap, List<String> dates, ArrayList<String> changelogs) {
          def firstCommit = dateCommitMap.get(dates.last())
          def secondCommit = getLatestCommit()
          if (firstCommit != secondCommit) {
               changelogs << getChangelogSection("Unreleased", getGitChangelog(firstCommit, secondCommit))
          }
     }

     private List appendFirstCommitChangeLog(List<String> dates, Map<String, String> dateCommitMap, Map<String,
             String> tagAndDateMap, List<String> changelogs) {
          def secondCommitDate = dates.get(0)
          def secondCommit = dateCommitMap.get(secondCommitDate)

          secondCommitDate = MoreObjects.firstNonNull(tagAndDateMap.get(secondCommit), getTagDate(secondCommit))

          def sectionTitle = "${getTagName(secondCommit)} (${secondCommitDate})"
          def sectionChangelog = getGitChangelog(secondCommit)
          changelogs << getChangelogSection(sectionTitle, sectionChangelog)
     }

     String getChangelogSection(String sectionTitle, String sectionChangelog) {
          StringBuilder changelogSection = new StringBuilder()
          changelogSection.append(sectionTitle)
                  .append('\n')
                  .append('-'.multiply(sectionTitle.length()))
                  .append('\n')
                  .append(sectionChangelog)
                  .append('\n')
          return changelogSection.toString()
     }

     private String[] getBaseGitCommand() {
          return ['git', 'log', "--pretty=format:${changelogFormat}"]
     }

     private String getGitChangelog(String reference) {
          logger.info("Getting Git changelog for {}", reference)
          return ((getBaseGitCommand() + [reference]) as List<String>).execute().text.trim()
     }

     private String getGitChangelog(String firstReference, String secondReference) {
          logger.info("Getting Git changelog for {}...{}", firstReference, secondReference)
          return ((getBaseGitCommand() + ["${firstReference}...${secondReference}"]) as List<String>)
                  .execute().text.trim()
     }

     private String getTagName(String commit) {
          return ['git', 'describe', '--tags', commit].execute().text.trim()
     }

     private String getTagDate(String tag) {
          return ['git', 'log', '-1', '--format=%ai', tag].execute().text.trim()
     }

     private String getLatestCommit() {
          return ['git', 'log', '-1', '--pretty=format:%H'].execute().text.trim()
     }
}
