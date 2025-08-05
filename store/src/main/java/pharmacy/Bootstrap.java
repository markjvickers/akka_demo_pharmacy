package pharmacy;

import akka.javasdk.DependencyProvider;
import akka.javasdk.ServiceSetup;
import akka.javasdk.annotations.Setup;
import akka.javasdk.http.HttpClientProvider;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Setup
public class Bootstrap implements ServiceSetup {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String CENTRAL_ROUTE = "store.central-route";
    private final HttpClientProvider httpClientProvider;
    private final Config config;

    public Bootstrap(Config config, HttpClientProvider httpClientProvider) {
        this.httpClientProvider = httpClientProvider;
        this.config = config;
    }

    @Override
    public DependencyProvider createDependencyProvider() {
        return new DependencyProvider() {
            @SuppressWarnings("unchecked")
            @Override
            public <T> T getDependency(Class<T> clazz) {
//                if (clazz == WeatherService.class) {
//                    if (
//                            System.getenv(WEATHER_API_KEY) != null &&
//                                    !System.getenv(WEATHER_API_KEY).isEmpty()
//                    ) {
//                        return (T) new WeatherServiceImpl(httpClientProvider);
//                    } else {
//                        // If the API key is not set, return a fake implementation
//                        return (T) new FakeWeatherService();
//                    }
//                }
                return null;
            }
        };
    }
}