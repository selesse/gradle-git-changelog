package com.selesse.gradle.git.changelog

import org.gradle.api.Plugin
import org.gradle.api.Project

class GitLogPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.task('generateChangelog') << {
            println 'git log'.execute().text.trim()
        }
    }
}
