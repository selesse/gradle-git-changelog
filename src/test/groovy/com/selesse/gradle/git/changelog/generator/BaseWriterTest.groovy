package com.selesse.gradle.git.changelog.generator

import com.google.common.base.Joiner
import com.google.common.base.Splitter
import com.selesse.gitwrapper.fixtures.GitRepositoryBuilder
import com.selesse.gitwrapper.fixtures.GitRepositoryBuilderRunner
import com.selesse.gradle.git.GitCommandExecutor
import org.gradle.api.Project

class BaseWriterTest {
    public static ChangelogWriter createMarkdownWriter(Project project, File file) {
        GitCommandExecutor gitCommandExecutor = new GitCommandExecutor('%s (%an)', file)
        return new MarkdownChangelogWriter(project.extensions.changelog, gitCommandExecutor)
    }

    public static String writeMarkdownChangelog(MarkdownChangelogWriter writer) {
        def byteArrayOutputStream = new ByteArrayOutputStream()
        writer.writeChangelog(new PrintStream(byteArrayOutputStream))

        List<String> changelogContent = Splitter.on('\n').splitToList(byteArrayOutputStream.toString('UTF-8'))
        // Markdown changelogs have 'title\n-----\n', we don't care about those first few lines...
        return Joiner.on('\n').join(changelogContent.subList(3, changelogContent.size()))
    }

    public static ChangelogWriter createHtmlWriter(Project project, File file) {
        GitCommandExecutor gitCommandExecutor = new GitCommandExecutor('%s (%an)', file)
        return new HtmlChangelogWriter(project.extensions.changelog, gitCommandExecutor)
    }

    public static String writeHtmlChangelog(HtmlChangelogWriter writer) {
        def byteArrayOutputStream = new ByteArrayOutputStream()
        writer.writeChangelog(new PrintStream(byteArrayOutputStream))

        return byteArrayOutputStream.toString('UTF-8')
    }

    public static GitRepositoryBuilderRunner createGitRepositoryBuilderRunner() {
        GitRepositoryBuilder.create()
                .runCommand('git init')
                .runCommand('git', 'config', 'user.name', 'Test Account')
                .runCommand('git', 'config', 'user.email', 'test@example.com')
                .createFile('README.md', 'Hello world!')
                .runCommand('git add README.md')
                .runCommand('git', 'commit', '-m', 'Initial commit from the past')
                .runCommand('git', 'commit', '--amend', '--date=2015-05-01 00:00:00 -0400', '-C', 'HEAD')
                .createFile('CONTRIBUTORS.md', 'This is a list of contributors to this project')
                .runCommand('git add CONTRIBUTORS.md')
                .runCommand('git', 'commit', '-m', 'Add contributors')
                .runCommand('git rm CONTRIBUTORS.md')
                .runCommand('git', 'commit', '-m', '[ci skip] Remove contributors')
    }
}
