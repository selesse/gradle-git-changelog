package com.selesse.gradle.git.changelog.generator
import com.google.common.base.Joiner
import com.google.common.base.MoreObjects
import com.google.common.base.Splitter
import com.selesse.gradle.git.GitCommandExecutor

class ComplexChangelogGenerator implements ChangelogGenerator {
     final Map<String, String> tagAndDateMap
     final GitCommandExecutor executor

     ComplexChangelogGenerator(GitCommandExecutor executor, List<String> tags) {
          this.executor = executor
          this.tagAndDateMap = tags.collectEntries {
               def tagAndDate = Splitter.on("|").omitEmptyStrings().trimResults().splitToList(it) as List<String>
               if (tagAndDate.size() != 2) {
                    tagAndDate = [tagAndDate.get(0), null]
               }
               [(tagAndDate.get(0)):tagAndDate.get(1)]
          } as Map<String, String>
     }

     String generateChangelog() {
          List<String> changelogs = []

          def dateCommitMap = tagAndDateMap.keySet().collectEntries {
               [(executor.getCommitDate(it)): it]
          } as Map<String, String>

          def dates = dateCommitMap.keySet().sort()

          appendFirstCommitChangeLog(dates, dateCommitMap, tagAndDateMap, changelogs)

          for (int i = 0; (i + 1) < dates.size(); i++) {
               def firstCommit = dateCommitMap.get(dates.get(i))

               def secondCommitDate = dates.get(i + 1)
               def secondCommit = dateCommitMap.get(secondCommitDate)

               secondCommitDate = MoreObjects.firstNonNull(tagAndDateMap.get(secondCommit), executor.getTagDate(secondCommit))

               def sectionTitle = "${executor.getTagName(secondCommit)} (${secondCommitDate})"
               def sectionChangelog = executor.getGitChangelog(firstCommit, secondCommit)

               changelogs << getChangelogSection(sectionTitle, sectionChangelog)
          }

          appendLastCommitChangelog(dateCommitMap, dates, changelogs)

          def reverseChangelogs = changelogs.reverse()
          return Joiner.on("\n").join(reverseChangelogs)
     }

     private void appendLastCommitChangelog(Map<String, String> dateCommitMap, List<String> dates, ArrayList<String> changelogs) {
          def firstCommit = dateCommitMap.get(dates.last())
          def secondCommit = executor.getLatestCommit()
          def changelog = executor.getGitChangelog(firstCommit, secondCommit)
          if (changelog.length() > 0) {
               changelogs << getChangelogSection("Unreleased", changelog)
          }
     }

     private List appendFirstCommitChangeLog(List<String> dates, Map<String, String> dateCommitMap, Map<String,
             String> tagAndDateMap, List<String> changelogs) {
          def secondCommitDate = dates.get(0)
          def secondCommit = dateCommitMap.get(secondCommitDate)

          secondCommitDate = MoreObjects.firstNonNull(tagAndDateMap.get(secondCommit), executor.getTagDate(secondCommit))

          def sectionTitle = "${executor.getTagName(secondCommit)} (${secondCommitDate})"
          def sectionChangelog = executor.getGitChangelog(secondCommit)
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

}
