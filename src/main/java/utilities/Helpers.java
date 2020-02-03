package utilities;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Helpers {

    /**
     * Generates a {@code String} that is used as an id for each build. The function combines the head commit id supplied
     * by Github with the current time.
     *
     * @param headCommitId the head commit id
     * @return the id for the build
     */
    @NotNull
    public static String generateId(final String headCommitId) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        LocalDateTime now = LocalDateTime.now();
        return headCommitId + "_" + dtf.format(now);
    }

    /**
     * Extracts the head commit id from the JSON-object contained in the POST request from Github
     *
     * @param jsonObject the payload from the Github POST request
     * @return the head commit id
     */
    public static String getHeadCommitId(@NotNull JSONObject jsonObject) {
        return jsonObject.getJSONObject("head_commit").get("id").toString();
    }

    /**
     * Returns the name of the branch that caused the push event.
     *
     * @param jsonObject the payload from the Github POST request
     * @return the name of the Github branch
     */
    public static String getBranch(@NotNull JSONObject jsonObject) {
        String ref = jsonObject.get("ref").toString();
        return ref.split("^(refs/heads/)")[1];
    }

    /**
     * Returns the username of the person who triggered the push event.
     *
     * @param jsonObject the payload from the Github POST request
     * @return the username
     */
    public static String getAuthor(@NotNull JSONObject jsonObject) {
        return jsonObject.getJSONObject("pusher").get("name").toString();
    }

    /**
     * Returns the name of the Github repository that the POST request was sent from.
     *
     * @param jsonObject the payload from the Github POST request
     * @return the name of the repository
     */
    public static String getRepositoryName(@NotNull JSONObject jsonObject) {
        return jsonObject.getJSONObject("repository").get("name").toString();
    }

    /**
     * Returns the URL for cloning the specific Github repository using HTTPS.
     *
     * @param jsonObject the payload from the Github POST request
     * @return the cloning URL
     */
    public static String getCloneURL(@NotNull JSONObject jsonObject) {
        return jsonObject.getJSONObject("repository").get("clone_url").toString();
    }
}
