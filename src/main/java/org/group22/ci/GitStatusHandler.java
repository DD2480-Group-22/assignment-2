package org.group22.ci;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.group22.utilities.Configuration;
import org.group22.utilities.Helpers;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class GitStatusHandler {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(GitStatusHandler.class);
    private String shaCommit;
    private String createCommitUrl;
    private static final String STATE = "state";
    private static final String DESCRIPTION = "description";
    private String repository;
    private String buildId;

    /**
     * Constructor for the GitStatusHandler class that needs the most information about a commit and project to create instantly the required url for the
     * Git Status API.
     *
     * @param repository: {@code String} with the repo name.
     * @param shaCommit:  {@code String} with the unique SHA hash identifying the commit.
     * @param owner:      {@code String} with the name of the owner of the repo.
     */
    GitStatusHandler(String repository, String shaCommit, String owner, String buildId) {
        this.repository = repository;
        this.shaCommit = shaCommit;
        this.buildId = buildId;
        String[] commitParams = {"repos", owner, repository, "statuses", shaCommit};
        String gitApiUrl = "https://api.github.com/";
        createCommitUrl = gitApiUrl + String.join("/", commitParams);
    }

    /**
     * Function used internally by {@code sendStatus} to set the status field to "waiting".
     *
     * @param partialJson: {@code JSONObject} the main json object with the "waiting" field.
     * @return partialJson: {@code JSONObject} the same object as before with one extra field.
     */
    @NotNull
    @Contract("_ -> param1")
    private JSONObject buildWaiting(@NotNull JSONObject partialJson) {
        partialJson.put(STATE, "waiting");
        partialJson.put(DESCRIPTION, "The build hasn't finished yet, try again in a while!");

        return partialJson;
    }

    /**
     * Function used internally by {@code sendStatus} if an error ocurrs while building the project (sets the status field to "error").
     *
     * @param partialJson: {@code JSONObject} the main json object with the "error" field.
     * @return partialJson: {@code JSONObject} the same object as before with one extra field.
     */
    @NotNull
    @Contract("_ -> param1")
    private JSONObject buildError(@NotNull JSONObject partialJson) {
        partialJson.put(STATE, "error");
        partialJson.put(DESCRIPTION, "An error occurred while trying to build your project!");

        return partialJson;
    }

    /**
     * Function used internally by {@code sendStatus} to set the status to "success" if the project build goes well.
     *
     * @param partialJson: {@code JSONObject} containing part of the necessary fields for a successful API communication.
     * @return partialJson: {@code JSONObject} containing the necessary data to interact with the API.
     */
    @NotNull
    @Contract("_-> param1")
    private JSONObject buildSuccess(@NotNull JSONObject partialJson) {
        partialJson.put(STATE, "success");
        partialJson.put(DESCRIPTION, "The build succeeded!");
        
        return partialJson;
    }
    
    /**
     * Function used internally by {@code sendStatus} to set the status to "failure" if the project build fails.
     *
     * @param partialJson: {@code JSONObject} containing part of the necessary fields for a successful API communication.
     * @return partialJson: {@code JSONObject} containing the necessary data to interact with the API.
     */
    @NotNull
    @Contract("_ -> param1")
    private JSONObject buildFailure(@NotNull JSONObject partialJson) {
        partialJson.put(STATE, "failure");
        partialJson.put(DESCRIPTION, "The build failed!");

        return partialJson;
    }


    /**
     * General function to interact with the REST Git Status API.
     *
     * @param buildStatus: {@code BuildStatus} representing one of the three possible states of the project build
     *                     (SUCCESS, FAILURE, ERROR,and WAITING).
     */
    public void sendStatus(BuildStatus buildStatus) {
        if ("".equals(shaCommit)) {
            logger.error("Error: tried to change the status of a commit without specifying an id!");
            throw new IllegalArgumentException("The sha for the head commit was not set");
        }

        JSONObject json = new JSONObject();

        // should fill here all the suggested values according to the API specs
        // https://developer.github.com/v3/repos/statuses/
        json.put("target_url", Helpers.reportAddressHTML(buildId));
        json.put("context", "Group-22-CI");

        if (BuildStatus.SUCCESS.equals(buildStatus)) {
            json = buildSuccess(json);
            logger.info("Setting build status for commit {} in repository {} to SUCCESS", shaCommit, repository);
        } else if (BuildStatus.FAILURE.equals(buildStatus)) {
            json = buildFailure(json);
            logger.info("Setting build status for commit {} in repository {} to SUCCESS", shaCommit, repository);
        } else if (BuildStatus.WAITING.equals(buildStatus)) {
            logger.info("Setting build status for commit {} in repository {} to WAITING", shaCommit, repository);
            json = buildWaiting(json);
        } else if (BuildStatus.ERROR.equals(buildStatus)) {
            json = buildError(json);
            logger.info("Setting build status for commit {} in repository {} to ERROR", shaCommit, repository);
        } else {
            logger.error("Error: non existent id for actions (1-3), yours was {}", buildStatus.value);
            return;
        }

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpPost request = new HttpPost(createCommitUrl);
            logger.info(createCommitUrl);
            StringEntity params = new StringEntity(json.toString());
            request.addHeader("content-type", "application/json");
            request.addHeader("Authorization", "token " + Configuration.GITHUB_TOKEN);
            request.setEntity(params);
            httpClient.execute(request);

        } catch (Exception e) {
            logger.error("Error: a problem appeared while trying to send back the build results", e);
        }
    }

    /**
     * Enum class holding {@code Integers} representing the status of the build of that is being tested.
     */
    public enum BuildStatus {
        SUCCESS(1),
        FAILURE(2),
        WAITING(3),
        ERROR(4);

        final int value;

        BuildStatus(final int value) {
            this.value = value;
        }
    }
}
