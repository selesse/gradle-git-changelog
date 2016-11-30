package com.selesse.gitwrapper.fixtures;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class GitRepositoryBuilder {
    private File directory;

    GitRepositoryBuilder(File directory) {
        this.directory = directory;
    }

    public static GitRepositoryBuilderRunner create() {
        return new GitRepositoryBuilderRunner();
    }

    public File getDirectory() {
        return directory;
    }

    public void cleanUp() throws IOException {
        if (directory.isDirectory()) {
            FileUtils.deleteDirectory(directory);
        }
    }
}
