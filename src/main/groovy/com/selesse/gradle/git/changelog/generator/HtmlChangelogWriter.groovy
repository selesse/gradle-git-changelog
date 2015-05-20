package com.selesse.gradle.git.changelog.generator

import com.selesse.gradle.git.GitCommandExecutor
import com.selesse.gradle.git.changelog.GitChangelogExtension
import org.apache.velocity.Template
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader

class HtmlChangelogWriter extends BaseChangelogWriter {
    File velocityTemplate

    HtmlChangelogWriter(GitChangelogExtension extension, GitCommandExecutor gitExecutor) {
        super(extension, gitExecutor)
        this.velocityTemplate = extension.velocityTemplate
    }

    @Override
    void writeChangelog(PrintStream printStream) {
        String templateName = velocityTemplate.name

        Properties velocityProperties = new Properties()
        if (velocityTemplate.absolutePath.contains('.jar!')) {
            velocityProperties.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath")
            velocityProperties.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.name)
            templateName = "velocity/${templateName}"
        } else {
            velocityProperties.setProperty(RuntimeConstants.RESOURCE_LOADER, "file")
            velocityProperties.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, velocityTemplate.parentFile.absolutePath)
        }
        VelocityEngine velocityEngine = new VelocityEngine(velocityProperties)
        velocityEngine.init()

        Template template = velocityEngine.getTemplate(templateName)

        VelocityContext context = getVelocityContext()

        Writer writer = new StringWriter()
        template.merge(context, writer)

        printStream.println(writer.toString())
        printStream.flush()
    }

    private VelocityContext getVelocityContext() {
        VelocityContext context = new VelocityContext()

        context.put('tags', getTagList())

        return context
    }
}
