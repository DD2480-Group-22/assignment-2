package org.group22.utilities;

import org.apache.commons.io.FileUtils;
import org.group22.ci.AWSFileUploader;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Scanner;

public class Helpers {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Helpers.class);

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
     * Checks if a JSON object contains a JSON object with the key {@code head_commit} and if that object contains a
     * key with a value that is not {@code null}.
     *
     * @param jsonObject The JSON object that is being checked
     * @return {@code true} if the requirements are meet, {@code false} otherwise
     */
    public static boolean isPushEvent(@NotNull JSONObject jsonObject) {
        if (jsonObject.has("head_commit")) {
            return jsonObject.getJSONObject("head_commit").get("id") != null;
        }
        return false;
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
     * Handles the setup of the CI org.groupp22.server and checks that all necessary values are set
     *
     * @param args command line arguments
     */
    public static void setUpConfiguration(@NotNull String[] args) {
        if (args.length != 2) throw new MissingResourceException("Missing configuration values", "", "");
        Configuration.BUCKET_NAME = args[0];
        Configuration.S3_BUCKET_REGION = args[1];

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
        } else if (Configuration.S3_BUCKET_REGION.isEmpty()) {
            throw new MissingResourceException("The AWS region is missing", "", "");
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

    /**
     * Deletes the folder in the git directory with the name specified by {@code id}.
     *
     * @param id The name of the directory to delete
     */
    public static void cleanUp(final String id) {
        try {
            FileUtils.deleteDirectory(new File(Configuration.PATH_TO_GIT + id));
        } catch (IllegalArgumentException e) {
            logger.error("The directory {} does not exist or is not a directory", id, e);
        } catch (IOException e) {
            logger.error("Failed to delete the directory: {}", id, e);
        }
    }

    /**
     * Parses the output file from the Maven test and counts number of tests run, number of failures, number of errors,
     * and number of skipped tests. Returns a {@code Result} object containing the result from the parse.
     *
     * @param fileName The name of the file containing the Maven output
     * @return {@code Result} object
     * @throws FileNotFoundException If couldn't find report file
     */
    @NotNull
    @Contract("_ -> new")
    private static Result parseResultFile(final String fileName) throws FileNotFoundException {
        Scanner textScanner = new Scanner(new File(Configuration.PATH_TO_REPORTS + fileName + ".txt"));

        int testsRun = 0;
        int failures = 0;
        int errors = 0;
        int skipped = 0;

        // Fetch all the relevant results
        while (textScanner.hasNextLine()) {
            String textLine = textScanner.nextLine().substring(7);
            if (textLine.contains("Tests run")) {
                String[] numbers = textLine.split(",");
                testsRun += Integer.parseInt(numbers[0].split(":")[1].substring(1));
                failures += Integer.parseInt(numbers[1].split(":")[1].substring(1));
                errors += Integer.parseInt(numbers[2].split(":")[1].substring(1));
                skipped += Integer.parseInt(numbers[3].split(":")[1].substring(1));
            }
        }
        textScanner.close();

        return new Result(testsRun, failures, errors, skipped);
    }


    /**
     * Generates a {@code HTML} file based on the result of the Maven test. The generated {@code HTML} file is saved
     * to the system.
     *
     * @param fileName The name of the file containing the Maven test result
     * @throws IOException If there was an issue reading or writing to file
     */
    public static void txtToHTMLFile(final String fileName) throws IOException {
        final Result result = parseResultFile(fileName);
        final InputStream inputStream = Helpers.class.getClassLoader().getResourceAsStream("HTML/template.html");
        final Scanner htmlScanner;

        if (inputStream != null) {
            htmlScanner = new Scanner(inputStream);
        } else {
            throw new IOException("Can't find HTML template");
        }

        final FileWriter htmlWriter = new FileWriter(Configuration.PATH_TO_REPORTS_HTML + fileName + ".html");
        final int height = 100 / result.testRun;
        final Map<String, String> map = substitutionMap(result, height);

        while (htmlScanner.hasNextLine()) {
            String htmlLine = htmlScanner.nextLine();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (htmlLine.contains(entry.getKey())) {
                    htmlLine = htmlLine.replace(entry.getKey(), entry.getValue());
                }
            }

            htmlWriter.write(htmlLine);
        }

        htmlScanner.close();
        htmlWriter.close();
    }

    /**
     * Generates the index {@code HTML} landing page as a {@code String}.
     *
     * @return The {@code HTML} page as a {@code String}
     */
    @NotNull
    public static String generateIndex() {
        StringBuilder stringBuilderList = new StringBuilder();
        stringBuilderList.append("<!DOCTYPE html> <html lang=\"en\">");
        stringBuilderList.append("<head>" + "<meta charset=\"UTF-8\">" + "<title>CI Server</title>" + "</head>");
        stringBuilderList.append("<body>");
        stringBuilderList.append("<h1>Index Group 22 - CI Server</h1>");
        stringBuilderList.append("<h2>List of previous builds on the CI server</h2>");
        stringBuilderList.append("<ul>");
        for (String id : Configuration.PREVIOUS_BUILDS) {
            stringBuilderList.append("<li>");
            stringBuilderList.append("<a href=\"");
            stringBuilderList.append(reportAddress(id));
            stringBuilderList.append("\">");
            stringBuilderList.append(id);
            stringBuilderList.append("</a>");
            stringBuilderList.append("</li>");
        }
        stringBuilderList.append("</ul>");
        stringBuilderList.append("</body>");
        stringBuilderList.append("</html>");

        return stringBuilderList.toString();
    }

    /**
     * Creates the URL to a report stored on AWS.
     *
     * @param id The id of the report
     * @return The URL to the report
     */
    @NotNull
    @Contract(pure = true)
    public static String reportAddress(final String id) {
        return "https://" + Configuration.BUCKET_NAME + ".s3." +
                Configuration.S3_BUCKET_REGION + ".amazonaws.com/reports/" + id + ".txt";
    }

    /**
     * Generates map for replacing placeholder values in HTML template with values from {@code Result} object.
     *
     * @param result The {@code Result} object
     * @param height The height
     * @return A map with placeholder as key and result as value
     */
    @NotNull
    private static Map<String, String> substitutionMap(@NotNull final Result result, final int height) {
        Map<String, String> map = new HashMap<>();
        for (String[] data : new String[][]{
                {"[tr]", Integer.toString(result.getTestRun())},
                {"[fl]", Integer.toString(result.getFailures())},
                {"[er]", Integer.toString(result.getErrors())},
                {"[sk]", Integer.toString(result.getSkipped())},
                {"[trHeight]", Integer.toString(result.getTestRun() * height)},
                {"[flHeight]", Integer.toString(result.getFailures() * height)},
                {"[erHeight]", Integer.toString(result.getErrors() * height)},
                {"[skHeight]", Integer.toString(result.getSkipped() * height)},
        }) {
            if (map.put(data[0], data[1]) != null) {
                throw new IllegalStateException("Duplicate key");
            }
        }
        return map;
    }

    /**
     * Helper class for storing the values parsed from Maven result file.
     */
    private static class Result {
        private final int testRun;
        private final int failures;
        private final int errors;
        private final int skipped;

        public Result(final int testRun, final int failures, final int errors, final int skipped) {
            this.testRun = testRun;
            this.failures = failures;
            this.errors = errors;
            this.skipped = skipped;
        }

        /**
         * Getter function for the {@code testRun} field.
         *
         * @return The value of {@code testRun}
         */
        public int getTestRun() {
            return testRun;
        }

        /**
         * Getter function for the {@code failures} field.
         *
         * @return The value of {@code failures}
         */
        public int getFailures() {
            return failures;
        }

        /**
         * Getter function for the {@code errors} field.
         *
         * @return The value of {@code errors}
         */
        public int getErrors() {
            return errors;
        }

        /**
         * Getter function for the {@code skipped} field.
         *
         * @return The value of {@code skipped}
         */
        public int getSkipped() {
            return skipped;
        }
    }
}
