package server;

import ci.ProjectTester;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import utilities.Helpers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ContinuousIntegrationServer extends AbstractHandler {
    private static final Logger logger = Logger.getLogger(ContinuousIntegrationServer.class);

    public static void main(String[] args) throws Exception {
        Helpers.setUpConfiguration(args);
        Server server = new Server(8080);
        server.setHandler(new ContinuousIntegrationServer());
        server.start();
        server.join();
    }

    @Override
    public void handle(String target, @NotNull Request baseRequest, @NotNull HttpServletRequest request, @NotNull HttpServletResponse response)
            throws IOException {
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            String payload = IOUtils.toString(request.getReader());
            try {
                JSONObject jsonObject = new JSONObject(payload);
                ProjectTester projectTester = new ProjectTester(jsonObject);
                projectTester.processPush();
            } catch (JSONException e) {
                logger.error("Server failed while parsing received payload", e);
            }
        }
        response.getWriter().println("CI job running");
    }
}