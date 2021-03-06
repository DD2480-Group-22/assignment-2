package org.group22;

import org.group22.server.ContinuousIntegrationServer;

public class App {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(App.class);

    /**
     * Entry point for the program.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        try {
            ContinuousIntegrationServer server = new ContinuousIntegrationServer();
            server.runServer(args);
        } catch (Exception e) {
            logger.error("Something went wrong while running the CI-Server", e);
        }
    }
}
