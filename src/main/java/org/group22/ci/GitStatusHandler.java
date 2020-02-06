package org.group22.ci;

import java.io.IOException;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;


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
	 * @param repo: {@code String} with the repo name.
	 * @param sha: {@code String} with the unique SHA hash identifying the commit.
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
	 * Constructor that does not need a specific SHA for the commit, but needs to call the setter for the commit SHA later for setting a status.
	 * @param repo: {@code String} with the repo name.
	 * @param owner: {@code String} with the name of the owner of the repo.
	 */
	GitStatusHandler(String repo, String owner) {
		repoName = repo;
		shaCommit = "";
		ownerName = owner;
	}
	
	/**
	 * Setter for the SHA identifying the commit for which we want to update it's build status.
	 * @param sha: {@code String} for the unique SHA hash of the commit.
	 */
	public void setCommit(String sha) {
		shaCommit = sha;
		String[] commitParams = {"repos", ownerName, repoName, "statuses", shaCommit};
		createCommitUrl = gitApiUrl + String.join("/", commitParams);
	}
	
	/**
	 * Function to report back the status for the build of the project to the Git Status API.
	 * @param mavenResult: the building results as a boolean, where {@code true} represents a successful build, and {@code false} the contrary.
	 * @throws IOException: only if some error occurs trying to close the httpClient object at the end of a post request.
	 */
	public void sendStatus(boolean mavenResult) throws IOException {
		
		if (shaCommit == "") {
			logger.error("Error: tried to change the status of a commit without specifying the id!");
			return;
		}
		
		JSONObject json = new JSONObject();
		
		// should fill here all the suggested values according to the API specs
		// https://developer.github.com/v3/repos/statuses/
		json.put("target_url", "https://our.super.url.for.our.ci");
		json.put("context", "Group-22-CI");
		if (mavenResult) { 
			json.put("state", "success");
			json.put("description", "The build succeeded!");
		} else {
			json.put("state", "failure");
			json.put("description", "The build failed!");
		}

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();

		try {
		    HttpPost request = new HttpPost(createCommitUrl);
		    StringEntity params = new StringEntity(json.toString());
		    request.addHeader("content-type", "application/json");
		    request.setEntity(params);
		    httpClient.execute(request);
		} catch (Exception ex) {
			logger.error("Error while sending back the build results", ex);
		} finally {
		    httpClient.close();
		}
	}
	
}
