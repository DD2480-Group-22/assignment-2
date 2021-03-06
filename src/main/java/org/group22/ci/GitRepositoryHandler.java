package org.group22.ci;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.group22.utilities.Configuration;

import java.io.File;

public class GitRepositoryHandler {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(GitRepositoryHandler.class);
    private final String id;
    private final String repositoryName;
    private final String cloneURL;
    private final String branch;

    /**
     * Creates a GitRepositoryHandler object
     *
     * @param id             the build id for the project
     * @param repositoryName the name of the repository
     * @param cloneURL       the URL used for cloning the repository
     */
    public GitRepositoryHandler(final String id, final String repositoryName, final String cloneURL, final String branch) {
        this.id = id;
        this.repositoryName = repositoryName;
        this.cloneURL = cloneURL;
        this.branch = branch;
    }

    /**
     * Clones specified github repository
     *
     * @return {@code true} if the repository was successfully cloned, otherwise {@code false}
     */
    public boolean cloneRepository() {
        logger.info("Cloning repository: {}, branch: {}", repositoryName, branch);

        CloneCommand cloneCommand = Git.cloneRepository();
        cloneCommand.setURI(cloneURL);
        cloneCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(Configuration.GITHUB_TOKEN, ""));
        cloneCommand.setDirectory(new File(Configuration.PATH_TO_GIT + id + "/" + repositoryName));
        if (!"master".equals(branch)) cloneCommand.setBranch(branch);

        try {
            cloneCommand.call();
            return true;
        } catch (TransportException e) {
            logger.error("Transport operation from Github remote failed", e);
        } catch (InvalidRemoteException e) {
            logger.error("Invalid remote repository", e);
        } catch (GitAPIException e) {
            logger.error("Something went wrong with the Github API", e);
        }

        return false;
    }
}
