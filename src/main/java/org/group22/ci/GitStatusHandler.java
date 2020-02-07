package org.group22.ci;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.group22.utilities.Configuration;
import org.group22.utilities.Helpers;
import org.json.JSONObject;

import java.io.IOException;


public class GitStatusHandler {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(GitRepositoryHandler.class);
    private String repoName;
    private String shaCommit;
    private String ownerName;
    private String gitApiUrl = "https://api.github.com/";
    private String createCommitUrl;

    /**
     * Constructor for the GitStatusHandler class that needs the most information about a commit and project to create instantly the required url for the
     * Git Status API.
     *
     * @param repo:  {@code String} with the repo name.
     * @param sha:   {@code String} with the unique SHA hash identifying the commit.
     * @param owner: {@code String} with the name of the owner of the repo.
     */
    GitStatusHandler(String repo, String sha, String owner) {
        repoName = repo;
        shaCommit = sha;
        ownerName = owner;
        String[] commitParams = {"repos", ownerName, repoName, "statuses", shaCommit};
        createCommitUrl = gitApiUrl + String.join("/", commitParams);
    }

    /**
     * Constructor that does not need a specific SHA for the commit, but needs to call the setter for the commit hash later to set a status.
     *
     * @param repo:  {@code String} with the repo name.
     * @param owner: {@code String} with the name of the owner of the repo.
     */
    GitStatusHandler(String repo, String owner) {
        repoName = repo;
        shaCommit = "";
        ownerName = owner;
    }

    /**
     * Setter for the SHA identifying the commit for which we want to update it's build status.
     *
     * @param sha: {@code String} for the unique SHA hash of the commit.
     */
    public void setCommit(String sha) {
        shaCommit = sha;
        String[] commitParams = {"repos", ownerName, repoName, "statuses", shaCommit};
        createCommitUrl = gitApiUrl + String.join("/", commitParams);
    }


    /**
     * Function used internally by {@code sendStatus} to set the status field to "waiting".
     *
     * @param partialJson: {@code JSONObject} the main json object with the "waiting" field.
     * @return partialJson: {@code JSONObject} the same object as before with one extra field.
     */
    public JSONObject buildWaiting(JSONObject partialJson) {
        partialJson.put("state", "waiting");
        partialJson.put("description", "The build hasn't finished yet, try again in a while!");

        return partialJson;
    }

    /**
     * Function used internally by {@code sendStatus} if an error ocurrs while building the project (sets the status field to "error").
     *
     * @param partialJson: {@code JSONObject} the main json object with the "error" field.
     * @return partialJson: {@code JSONObject} the same object as before with one extra field.
     */
    public JSONObject buildError(JSONObject partialJson) {
        partialJson.put("state", "error");
        partialJson.put("description", "An error ocurred while trying to build your project!");

        return partialJson;
    }

    /**
     * Function used internally by {@code sendStatus} to set the status to "success" if the project build goes well.
     *
     * @param partialJson: {@code JSONObject} containing part of the necessary fields for a successful API communication.
     * @return partialJson: {@code JSONObject} containing the necessary data to interact with the API.
     */    
    public JSONObject buildSuccess(JSONObject partialJson) {
        partialJson.put("state", "success");
        partialJson.put("description", "The build succeeded!");
        
        return partialJson;
    }
    
    /**
     * Function used internally by {@code sendStatus} to set the status to "failure" if the project build fails.
     *
     * @param partialJson: {@code JSONObject} containing part of the necessary fields for a successful API communication.
     * @return partialJson: {@code JSONObject} containing the necessary data to interact with the API.
     */
    public JSONObject buildFailure(JSONObject partialJson) {
        partialJson.put("state", "failure");
        partialJson.put("description", "The build failed!");

        return partialJson;
    }

    /**
     * General function to interact with the REST Git Status API.
     *
     * @param messageId:  {@code int} representing one of the 4 possible build status (success, failure, waiting or error) to send back.
     * @throws IOException: throws {@code IOException} only if an unexpected error occurs trying to send the POST Request.
     */
    public void sendStatus(int messageId) throws IOException {
        if ("".equals(shaCommit)) {
            logger.error("Error: tried to change the status of a commit without specifying an id!");
            return;
        }

        JSONObject json = new JSONObject();

        // should fill here all the suggested values according to the API specs
        // https://developer.github.com/v3/repos/statuses/
        json.put("target_url", Helpers.reportAddress(shaCommit));
        json.put("context", "Group-22-CI");

        // switch deciding which action to take and send back as the current commit status
        switch (messageId) {
            case 1:
                json = buildSuccess(json);
                break;
            case 2:
            	json = buildFailure(json);
            	break;
            case 3:
                json = buildWaiting(json);
                break;
            case 4:
                json = buildError(json);
                break; 
            default:
                logger.error("Error: non existent id for actions (1-4), yours was {}", messageId);
                return;
        }


        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        try {
            HttpPost request = new HttpPost(createCommitUrl);
            StringEntity params = new StringEntity(json.toString());
            request.addHeader("content-type", "application/json");
            request.addHeader("Authorization", Configuration.GITHUB_TOKEN);
            request.setEntity(params);
            httpClient.execute(request);
        } catch (Exception ex) {
            logger.error("Error: a problem appeared while trying to send back the build results", ex);
        } finally {
            httpClient.close();
        }
    }

}
