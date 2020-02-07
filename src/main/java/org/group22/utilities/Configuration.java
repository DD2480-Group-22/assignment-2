package org.group22.utilities;

import java.util.Set;

public class Configuration {
    public static final String AWS_ACCESS_KEY_ID = System.getenv("AWS_ACCESS_KEY_ID");
    public static final String AWS_SECRET_KEY = System.getenv("AWS_SECRET_KEY");
    public static String S3_BUCKET_REGION;
    public static String BUCKET_NAME;
    public static Set<String> PREVIOUS_BUILDS;
    public static final int SERVER_PORT = System.getenv("PORT") == null ? 8080 : Integer.parseInt(System.getenv("PORT"));
    public static final String GITHUB_TOKEN = System.getenv("GITHUB_TOKEN");
    public static final String M3_HOME = System.getenv("MAVEN_HOME");
    public static final String PATH_TO_RESOURCES = "./";
    public static final String PATH_TO_GIT = PATH_TO_RESOURCES + "git/";
    public static final String PATH_TO_REPORTS = PATH_TO_RESOURCES + "reports/";
    public static final String PATH_TO_REPORTS_HTML = PATH_TO_RESOURCES + "reports_html/";
}
