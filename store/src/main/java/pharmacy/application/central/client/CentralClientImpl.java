package pharmacy.application.central.client;

import akka.javasdk.http.HttpClient;
import akka.javasdk.http.HttpClientProvider;
import akka.javasdk.http.StrictResponse;
import akka.util.ByteString;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pharmacy.application.central.client.domain.StorePatientRecord;

public class CentralClientImpl implements CentralClient {

    private final String CENTRAL_ROUTE = "store.central-route";
    private final HttpClient httpClient;

    private static final Logger logger = LoggerFactory.getLogger(
        CentralClientImpl.class
    );

    public CentralClientImpl(
        Config config,
        HttpClientProvider httpClientProvider
    ) {
        var route = config.getString(CENTRAL_ROUTE);
        logger.info("Configuring central client with route: '{}'", route);
        httpClient = httpClientProvider.httpClientFor(route.trim());
    }

    @Override
    public StrictResponse<ByteString> create(StorePatientRecord record) {
        logger.info("Creating patient record: {}", record);
        var result = httpClient
            .PUT("/patients/patient")
            .withRequestBody(record)
            .invoke();
        logger.info("Result of creation result: {}", result);
        return result;
    }

    @Override
    public StrictResponse<akka.util.ByteString> update(
        StorePatientRecord record
    ) {
        return httpClient
            .POST("/patients/patient")
            .withRequestBody(record)
            .invoke();
    }

    @Override
    public StrictResponse<StorePatientRecord> get(
        String pharmacyId,
        String patientId
    ) {
        return httpClient
            .GET("/patients/" + toId(pharmacyId, patientId))
            .responseBodyAs(StorePatientRecord.class)
            .invoke();
    }

    @Override
    public StrictResponse<akka.util.ByteString> delete(
        String pharmacyId,
        String patientId
    ) {
        return httpClient
            .DELETE("/patients/" + toId(pharmacyId, patientId))
            .invoke();
    }

    private String toId(String pharmacyId, String patientId) {
        return pharmacyId + "-" + patientId;
    }
}
