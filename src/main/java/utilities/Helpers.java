package utilities;

import ci.AWSFileUploader;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.MissingResourceException;

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

    /**
     * Handles the setup of the CI server and checks that all necessary values are set
     *
     * @param args command line arguments
     */
    public static void setUpConfiguration(@NotNull String[] args) {
        if (args.length != 2) throw new MissingResourceException("Missing configuration values", "", "");
        Configuration.BUCKET_NAME = args[0];
        Configuration.S3_BUCKET_ADDRESS = args[1];

        AWSFileUploader awsFileUploader = new AWSFileUploader();
        Configuration.PREVIOUS_BUILDS = awsFileUploader.getReports();

        if (Configuration.AWS_ACCESS_KEY_ID.isEmpty()) {
            throw new MissingResourceException("The AWS access key id is missing", "", "");
        } else if (Configuration.AWS_SECRET_KEY.isEmpty()) {
            throw new MissingResourceException("The AWS secret key is missing", "", "");
        } else if (Configuration.GITHUB_TOKEN.isEmpty()) {
            throw new MissingResourceException("The Github token is missing", "", "");
        } else if (Configuration.M3_HOME.isEmpty()) {
            throw new MissingResourceException("The M3 Home path is missing", "", "");
        } else if (Configuration.BUCKET_NAME.isEmpty()) {
            throw new MissingResourceException("The AWS bucket name is missing", "", "");
        } else if (Configuration.S3_BUCKET_ADDRESS.isEmpty()) {
            throw new MissingResourceException("The AWS bucket address is missing", "", "");
        }
    }

    /**
     * Adds a new report id to the {@code PREVIOUS_BUILDS} set.
     *
     * @param newReport the id of the new report
     */
    public static void updatePreviousBuilds(final String newReport) {
        Configuration.PREVIOUS_BUILDS.add(newReport);
    }
}
