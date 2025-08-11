package pharmacy.api;

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
import pharmacy.domain.PharmacyId;

@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
@HttpEndpoint("/store")
public class StaticContentEndpoint extends AbstractHttpEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(
        StaticContentEndpoint.class
    );

    private final PharmacyId pharmacyId;

    public StaticContentEndpoint(PharmacyId pharmacyId) {
        this.pharmacyId = pharmacyId;
    }

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
        } else if ("styles.css".equals(filename)) {
            return HttpResponse.create()
                .withStatus(StatusCodes.OK)
                .withEntity(
                    HttpEntities.create(
                        ContentTypes.create(
                            MediaTypes.TEXT_CSS,
                            HttpCharsets.UTF_8
                        ),
                        getCssContent()
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
                "/static/store/index.html"
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
                "/static/store/app.js"
            );
            if (inputStream == null) {
                logger.warn("app.js not found in resources");
                return getDefaultJavaScript();
            }

            byte[] content = inputStream.readAllBytes();
            inputStream.close();
            return new String(content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("Error reading app.js", e);
            return getDefaultJavaScript();
        }
    }

    private String getCssContent() {
        try {
            InputStream inputStream = getClass().getResourceAsStream(
                "/static/store/styles.css"
            );
            if (inputStream == null) {
                logger.warn("styles.css not found in resources");
                return getDefaultCss();
            }

            byte[] content = inputStream.readAllBytes();
            inputStream.close();
            return new String(content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("Error reading styles.css", e);
            return getDefaultCss();
        }
    }

    private String getDefaultHtml() {
        return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Pharmacy Store %s - Patient Management</title>
            <link rel="stylesheet" href="/store/static/styles.css">
        </head>
        <body>
            <div id="root">Loading...</div>
            <script src="/store/static/app.js"></script>
        </body>
        </html>
        """.formatted(pharmacyId.id());
    }

    private String getDefaultJavaScript() {
        return """
        // Default JavaScript content when app.js is not found
        console.log('Default JavaScript loaded for store %s');
        document.getElementById('root').innerHTML = `
            <div class="container">
                <h1>Pharmacy Store %s - Patient Management</h1>
                <p>JavaScript files not found. Please ensure static resources are properly deployed.</p>
                <div class="api-info">
                    <h3>Available API endpoints:</h3>
                    <ul>
                        <li><strong>GET</strong> /patients/{id} - Get patient by ID</li>
                        <li><strong>PUT</strong> /patients/patient - Create new patient</li>
                        <li><strong>PUT</strong> /patients/patient/{id} - Update patient</li>
                        <li><strong>DELETE</strong> /patients/patient/{id} - Delete patient</li>
                        <li><strong>POST</strong> /patients/patient/merge - Merge patients</li>
                        <li><strong>GET</strong> /patients/search - Search patients</li>
                        <li><strong>GET</strong> /patients/delivery/summary - Get delivery summary</li>
                    </ul>
                </div>
            </div>
        `;
        """.formatted(pharmacyId.id(), pharmacyId.id());
    }

    private String getDefaultCss() {
        return """
        /* Default CSS content when styles.css is not found */
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', sans-serif;
            margin: 0;
            padding: 20px;
            background-color: #f5f5f5;
            line-height: 1.6;
        }

        .container {
            max-width: 1200px;
            margin: 0 auto;
            background: white;
            padding: 30px;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }

        h1 {
            color: #2c3e50;
            border-bottom: 3px solid #3498db;
            padding-bottom: 10px;
        }

        .api-info {
            margin-top: 30px;
            padding: 20px;
            background-color: #ecf0f1;
            border-radius: 5px;
        }

        .api-info h3 {
            color: #34495e;
            margin-top: 0;
        }

        .api-info ul {
            list-style-type: none;
            padding: 0;
        }

        .api-info li {
            margin: 10px 0;
            padding: 10px;
            background: white;
            border-left: 4px solid #3498db;
            font-family: monospace;
        }

        .api-info strong {
            color: #e74c3c;
            font-weight: bold;
        }
        """;
    }
}
