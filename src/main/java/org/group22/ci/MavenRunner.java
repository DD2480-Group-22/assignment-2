package org.group22.ci;

import org.apache.maven.shared.invoker.*;
import org.group22.utilities.Configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Collections;

public class MavenRunner {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MavenRunner.class);
    private final String projectId;
    private final String repositoryName;

    /**
     * Creates and initializes a MavenRunner object
     *
     * @param id             the id of the build
     * @param repositoryName the name of the repository
     */
    public MavenRunner(final String id, final String repositoryName) {
        this.projectId = id;
        this.repositoryName = repositoryName;
    }

    public void runProject() {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setBaseDirectory(new File(Configuration.PATH_TO_GIT + projectId + "/" + repositoryName));
        request.setGoals(Collections.singletonList("test"));
        request.setBatchMode(true);

        Invoker invoker = new DefaultInvoker();
        invoker.setMavenHome(new File(Configuration.M3_HOME));

        try {
            logger.info("Running test for Maven project in repository: {}", repositoryName);

            PrintStream writeToFile = new PrintStream(new FileOutputStream(Configuration.PATH_TO_REPORTS + projectId + ".txt"));
            InvocationOutputHandler outputHandler = new PrintStreamHandler(writeToFile, false);

            invoker.setOutputHandler(outputHandler);
            invoker.execute(request);

            InvocationResult result = invoker.execute(request);

            if (result.getExitCode() != 0) {
                throw new IllegalStateException("Build failed");
            }
        } catch (MavenInvocationException e) {
            logger.error("Error while trying to run testes", e);
        } catch (IllegalStateException e) {
            logger.error("Build failed", e);
        } catch (FileNotFoundException e) {
            logger.error("Could not find report file", e);
        }
    }
}
