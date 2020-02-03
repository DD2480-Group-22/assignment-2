package utilities;

import java.util.Set;

public class Configuration {
    public static final String AWS_ACCESS_KEY_ID = System.getenv("AWS_ACCESS_KEY_ID");
    public static final String AWS_SECRET_KEY = System.getenv("AWS_SECRET_KEY");
    public static String S3_BUCKET_ADDRESS;
    public static String BUCKET_NAME;
    public static Set<String> PREVIOUS_BUILDS;
    public static final String GITHUB_TOKEN = System.getenv("GITHUB_TOKEN");
    public static final String M3_HOME = System.getenv("MAVEN_HOME");
    public static final String PATH_TO_RESOURCES = "./";
    public static final String PATH_TO_GIT = PATH_TO_RESOURCES + "git/";
    public static final String PATH_TO_REPORTS = PATH_TO_RESOURCES + "reports/";
}
