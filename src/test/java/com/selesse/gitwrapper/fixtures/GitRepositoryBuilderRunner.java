package com.selesse.gitwrapper.fixtures;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;

public class GitRepositoryBuilderRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(GitRepositoryBuilderRunner.class);

    private File temporaryDirectory;

    GitRepositoryBuilderRunner() {
        temporaryDirectory = Files.createTempDir();
    }

    public GitRepositoryBuilderRunner runCommand(String command) throws IOException, InterruptedException {
        executeCommand(command);
        return this;
    }

    public GitRepositoryBuilderRunner runCommand(String... commandList) throws IOException, InterruptedException {
        executeCommandList(Lists.newArrayList(commandList));
        return this;
    }

    public GitRepositoryBuilderRunner createFile(String file, String contents) throws FileNotFoundException {
        addFile(file, contents);
        return this;
    }

    public GitRepositoryBuilder build() {
        return new GitRepositoryBuilder(temporaryDirectory);
    }

    private void executeCommand(String command) throws IOException, InterruptedException {
        List<String> commandSplitBySpace = Splitter.on(" ").splitToList(command);

        executeCommandList(commandSplitBySpace);
    }

    private void executeCommandList(List<String> commandList) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(commandList);
        processBuilder.directory(temporaryDirectory);
        Process process = processBuilder.start();
        process.waitFor();

        InputStream inputStream = process.getInputStream();

        LOGGER.info("Printing output of '{}' to stdout", commandList);
        ByteStreams.copy(inputStream, System.out);
    }

    private void addFile(String file, String contents) throws FileNotFoundException {
        LOGGER.info("Creating {}", file);

        File desiredFile = new File(temporaryDirectory, file);
        File desiredFileParent = desiredFile.getParentFile();

        if (!desiredFileParent.exists()) {
            boolean madeDirectories = desiredFileParent.mkdirs();
            if (!madeDirectories) {
                LOGGER.error("Error creating needed directories: {}", desiredFileParent.getAbsolutePath());
            }
        }

        PrintWriter printWriter = new PrintWriter(desiredFile);
        printWriter.println(contents);
        printWriter.flush();
        printWriter.close();
    }

}
