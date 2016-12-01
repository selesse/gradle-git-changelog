package com.selesse.gradle.git.changelog.functional

import org.gradle.testkit.runner.GradleRunner

class ProjectRunner {
    static def buildGenerateChangelog(File projectDir) {
        return GradleRunner.create()
                .withProjectDir(projectDir)
                .withArguments('generateChangelog')
                .withPluginClasspath()
                .forwardStdError(new BufferedWriter(new OutputStreamWriter(System.err)))
                .forwardStdOutput(new BufferedWriter(new OutputStreamWriter(System.out)))
                .build()

    }
}
