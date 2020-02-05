package org.group22.ci;

import org.group22.utilities.Helpers;
import org.json.JSONObject;

public class ProjectTester {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ProjectTester.class);

    private final String branch;
    private final String author;
    private final String id;
    private final String repositoryName;
    private final String cloneURL;
    private boolean cloned = false;
    private boolean tested = false;
    private boolean reportSaved = false;
    private boolean cleanedUp = false;

    /**
     * Creates and initializes a ProjectTester object.
     *
     * @param jsonObject the payload from the Github POST request
     */
    public ProjectTester(JSONObject jsonObject) {
        id = Helpers.generateId(Helpers.getHeadCommitId(jsonObject));
        branch = Helpers.getBranch(jsonObject);
        author = Helpers.getAuthor(jsonObject);
        repositoryName = Helpers.getRepositoryName(jsonObject);
        cloneURL = Helpers.getCloneURL(jsonObject);
        logger.info("Started test of repository: {}, branch: {}, pushed by: {}, id: {}", repositoryName, branch, author, Helpers.getHeadCommitId(jsonObject));
    }

    /**
     * Creates and initializes a ProjectTester object.
     *
     * @param repositoryName the name of the repository
     * @param headCommitId   the id of the head commit
     * @param branch         the name of the branch
     * @param author         the name of the author
     * @param cloneURL       the URL used for cloning the repository
     */
    public ProjectTester(final String repositoryName, final String headCommitId, final String branch, final String author, final String cloneURL) {
        this.repositoryName = repositoryName;
        this.branch = branch;
        this.author = author;
        this.cloneURL = cloneURL;
        id = Helpers.generateId(headCommitId);
    }

    /**
     * Processes a POST request from Github.
     */
    public void processPush() {
        logger.info("Started test of repository: {}, branch: {}, pushed by: {}, id: {}", repositoryName, branch, author, id);

        GitRepositoryHandler gitRepositoryHandler = new GitRepositoryHandler(id, repositoryName, cloneURL);
        MavenRunner mavenRunner = new MavenRunner(id, repositoryName);
        AWSFileUploader awsFileUploader = new AWSFileUploader();

        for (int i = 0; i < 3 && !cloned; i++) {
            cloned = gitRepositoryHandler.cloneRepository();
        }

        for (int i = 0; i < 3 && cloned && !tested; i++) {
            tested = mavenRunner.runProject();
        }

        for (int i = 0; i < 3 && !reportSaved; i++) {
            reportSaved = awsFileUploader.uploadFile(id);
        }
    }
}
