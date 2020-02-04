package utilities;

import ci.AWSFileUploader;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

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
        if (args.length != 3) throw new MissingResourceException("Missing configuration values", "", "");
        Configuration.BUCKET_NAME = args[0];
        Configuration.S3_BUCKET_REGION = args[1];
        Configuration.SERVER_PORT = Integer.parseInt(args[2]);

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
    
    
    public static void txtToHTMLFile(File textFile) throws IOException {
    	Scanner textScanner = new Scanner(textFile);
    	
    	int testsRun = 0;
		int failures = 0;
		int errors   = 0;
		int skipped  = 0;
    	
    	while(textScanner.hasNextLine()) {
    		String textLine = textScanner.nextLine().substring(7);
    		
    		if(textLine.contains("Tests run")) {
    			String[] numbers = textLine.split(",");
    			testsRun += Integer.parseInt( numbers[0].split(":")[1].substring(1) );
    			failures += Integer.parseInt( numbers[1].split(":")[1].substring(1) );
    			errors   += Integer.parseInt( numbers[2].split(":")[1].substring(1) );
    			skipped  += Integer.parseInt( numbers[3].split(":")[1].substring(1) );
    			
    		}
    	}
    	textScanner.close();
    	
    		
    	// Read from the html template and write to new html file with placeholder values replaced
    	Scanner htmlScanner = new Scanner(new File("template.html"));
    	FileWriter htmlWriter = new FileWriter("results.html");
    		
    	while(htmlScanner.hasNextLine()) {
    		String htmlLine = htmlScanner.nextLine();
    			
    		if(htmlLine.contains("[tr]"))
    			htmlLine = htmlLine.replace("[tr]", ""+testsRun);
    		if(htmlLine.contains("[fl]"))
    			htmlLine = htmlLine.replace("[fl]", ""+failures);
    		if(htmlLine.contains("[er]"))
    			htmlLine = htmlLine.replace("[er]", ""+errors);
    		if(htmlLine.contains("[sk]"))
    			htmlLine = htmlLine.replace("[sk]", ""+skipped);
    		
    		int height = 100 / testsRun;
    		
    		if(htmlLine.contains("[trHeight]"))
    			htmlLine = htmlLine.replace("[trHeight]", ""+testsRun*height);
    		if(htmlLine.contains("[flHeight]"))
    			htmlLine = htmlLine.replace("[flHeight]", ""+failures*height);
    		if(htmlLine.contains("[erHeight]"))
    			htmlLine = htmlLine.replace("[erHeight]", ""+errors*height);
    		if(htmlLine.contains("[skHeight]"))
    			htmlLine = htmlLine.replace("[skHeight]", ""+skipped*height);
    			
    			
    		htmlWriter.write(htmlLine);
    	}
    		
    	htmlScanner.close();
    	htmlWriter.close();
    }
}
