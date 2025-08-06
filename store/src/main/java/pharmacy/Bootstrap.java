package pharmacy;

import akka.javasdk.DependencyProvider;
import akka.javasdk.ServiceSetup;
import akka.javasdk.annotations.Setup;
import akka.javasdk.http.HttpClientProvider;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pharmacy.application.central.client.CentralClient;
import pharmacy.application.central.client.CentralClientImpl;
import pharmacy.domain.PharmacyId;

@Setup
public class Bootstrap implements ServiceSetup {

    private final Logger logger = LoggerFactory.getLogger(getClass());
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
                if (clazz == CentralClient.class) {
                  return (T) new CentralClientImpl(config, httpClientProvider);
                }
                if (clazz == PharmacyId.class) {
                    return (T) new PharmacyId(config.getString("store.pharmacy-id"));
                }
                return null;
            }
        };
    }
}