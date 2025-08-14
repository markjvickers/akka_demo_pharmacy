package central.app.api;

import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpCharsets;
import akka.http.javadsl.model.HttpEntities;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.MediaTypes;
import akka.http.javadsl.model.StatusCodes;
import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.http.AbstractHttpEndpoint;
import akka.javasdk.http.HttpResponses;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
@HttpEndpoint
public class StaticContentEndpoint extends AbstractHttpEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(
        StaticContentEndpoint.class
    );

    @Get("/")
    public HttpResponse serveRoot() {
        return createHtmlResponse();
    }

    @Get("/ui")
    public HttpResponse serveUi() {
        return createHtmlResponse();
    }

    @Get("/ui/{path}")
    public HttpResponse serveUiWithPath(String path) {
        // For SPA routing, always serve index.html for UI routes that don't contain file extensions
        if (!path.contains(".")) {
            return createHtmlResponse();
        }
        // For actual files, try to serve them or fallback to HTML
        return createHtmlResponse();
    }

    @Get("/static/{filename}")
    public HttpResponse serveStatic(String filename) {
        if ("app.js".equals(filename)) {
            return HttpResponse.create()
                .withStatus(StatusCodes.OK)
                .withEntity(
                    HttpEntities.create(
                        ContentTypes.create(
                            MediaTypes.APPLICATION_JAVASCRIPT,
                            HttpCharsets.UTF_8
                        ),
                        getJavaScriptContent()
                    )
                );
        } else if ("arch.png".equals(filename)) {
            return HttpResponse.create()
                .withStatus(StatusCodes.OK)
                .withEntity(
                    HttpEntities.create(
                        ContentTypes.create(MediaTypes.IMAGE_PNG),
                        getImageContent("arch.png")
                    )
                );
        }
        // For other static files, return not found
        return HttpResponses.notFound();
    }

    @Get("/favicon.ico")
    public HttpResponse serveFavicon() {
        // Return a simple 204 No Content for favicon to avoid 404 errors in browser
        logger.debug("Serving favicon request");
        return HttpResponses.noContent();
    }

    private HttpResponse createHtmlResponse() {
        return HttpResponse.create()
            .withStatus(StatusCodes.OK)
            .withEntity(
                HttpEntities.create(
                    ContentTypes.TEXT_HTML_UTF8,
                    getHtmlContent()
                )
            );
    }

    private String getHtmlContent() {
        try {
            InputStream inputStream = getClass().getResourceAsStream(
                "/static/index.html"
            );
            if (inputStream == null) {
                logger.warn("index.html not found in resources");
                return getDefaultHtml();
            }

            byte[] content = inputStream.readAllBytes();
            inputStream.close();
            return new String(content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("Error reading index.html", e);
            return getDefaultHtml();
        }
    }

    private String getJavaScriptContent() {
        try {
            InputStream inputStream = getClass().getResourceAsStream(
                "/static/app.js"
            );
            if (inputStream == null) {
                logger.warn("app.js not found in resources");
                return "console.log('JavaScript not found');";
            }

            byte[] content = inputStream.readAllBytes();
            inputStream.close();
            return new String(content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("Error reading app.js", e);
            return "console.log('Error loading JavaScript');";
        }
    }

    private byte[] getImageContent(String filename) {
        try {
            InputStream inputStream = getClass().getResourceAsStream(
                "/static/" + filename
            );
            if (inputStream == null) {
                logger.warn("{} not found in resources", filename);
                return new byte[0];
            }

            byte[] content = inputStream.readAllBytes();
            inputStream.close();
            return content;
        } catch (IOException e) {
            logger.error("Error reading {}", filename, e);
            return new byte[0];
        }
    }

    private String getDefaultHtml() {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <title>Central Pharmacy Management System</title>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
        </head>
        <body>
            <h1>Central Pharmacy Management System</h1>
            <p>Static files not found. Please ensure static resources are properly deployed.</p>
            <p>API endpoints are available at:</p>
            <ul>
                <li>/pharmacies/{id} - GET pharmacy by ID</li>
                <li>/pharmacies/pharmacy - PUT/POST to add/update pharmacy</li>
                <li>/patients/{id} - GET patient by ID</li>
                <li>/patients/patient - PUT/POST to add/update patient</li>
            </ul>
        </body>
        </html>
        """;
    }
}
