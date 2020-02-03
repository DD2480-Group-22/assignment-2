package utilities;

public class Configuration {
    public static final String GITHUB_TOKEN = System.getenv("GITHUB_TOKEN");
    public static final String M3_HOME = System.getenv("MAVEN_HOME");
    public static final String PATH_TO_RESOURCES = "./";
    public static final String PATH_TO_GIT = PATH_TO_RESOURCES + "git/";
    public static final String PATH_TO_REPORTS = PATH_TO_RESOURCES + "reports/";
}
