package org.group22.server;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.group22.ci.ProjectTester;
import org.group22.utilities.Configuration;
import org.group22.utilities.Helpers;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ContinuousIntegrationServer extends AbstractHandler {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ContinuousIntegrationServer.class);

    /**
     * Sets up and starts the CI server.
     *
     * @param args Command line arguments
     * @throws Exception If something goes wrong with the server
     */
    public void runServer(String[] args) throws Exception {
        Helpers.setUpConfiguration(args);
        Server server = new Server(Configuration.SERVER_PORT);
        server.setHandler(new ContinuousIntegrationServer());
        server.start();
        server.join();
    }

    /**
     * Handles HTTP requests sent to the server. Calls appropriate helper function based on the typ of request.
     *
     * @param target      The target
     * @param baseRequest The baseRequest
     * @param request     The request
     * @param response    The response
     * @throws IOException If something goes wring while parsing the response
     */
    @Override
    public void handle(String target, @NotNull Request baseRequest, @NotNull HttpServletRequest request, @NotNull HttpServletResponse response)
            throws IOException {
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            handleGetRequest(request, response);
        } else if ("POST".equalsIgnoreCase(request.getMethod())) {
            handlePostRequest(request, response);
        }
    }


    /**
     * Handles {@code GET} requests that are sent to the server. If the URL query string contains a id of a generated report the
     * client is redirected to the report. If the URL does not contain a valid id the client is sent a HTML document
     * in the format of a {@code String} that contains a list to all the generated reports stored on AWS.
     *
     * @param request  The request
     * @param response The response
     * @throws IOException If something goes wrong while parsing the request or sending the response
     */
    private void handleGetRequest(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws IOException {
        if (request.getRequestURI().matches("/reports/[a-z0-9_-]+")) {
            final String reportId = request.getRequestURI().replace("/reports/", "");
            logger.info("Request for build report for id: {}", reportId);
            if (Configuration.PREVIOUS_BUILDS.contains(reportId)) {
                final String reportURL = Helpers.reportAddress(reportId);
                response.sendRedirect(reportURL);
                return;
            } else {
                logger.info("URL for non-existing report was requested, id: {}", reportId);
            }
        }
        response.getWriter().println(Helpers.generateIndex());
        response.getWriter().flush();
    }

    /**
     * Handles {@code POST} requests that are sent to the server. Starts the CI process for the repository specified by
     * the request.
     *
     * @param request  The request
     * @param response The response
     * @throws IOException If something goes wrong while parsing the request or sending the response
     */
    private void handlePostRequest(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws IOException {
        String payload = IOUtils.toString(request.getReader());
        try {
            JSONObject jsonObject = new JSONObject(payload);
            ProjectTester projectTester = new ProjectTester(jsonObject);
            projectTester.processPush();
        } catch (JSONException e) {
            logger.error("Server failed while parsing received payload", e);
        }
        response.getWriter().println("CI job running");
    }
}