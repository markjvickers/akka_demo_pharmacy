package pharmacy.application.central;

import akka.javasdk.http.HttpClient;
import akka.javasdk.http.HttpClientProvider;
import akka.javasdk.http.StrictResponse;
import akka.util.ByteString;
import com.typesafe.config.Config;
import pharmacy.application.central.domain.StorePatientRecord;

public class CentralClientImpl implements CentralClient {

    private final String CENTRAL_ROUTE = "store.central-route";
    private final HttpClient httpClient;

    public CentralClientImpl(Config config, HttpClientProvider httpClientProvider) {
        var route = config.getString(CENTRAL_ROUTE);
        httpClient = httpClientProvider.httpClientFor(route);
    }

    @Override
    public StrictResponse<ByteString> create(StorePatientRecord record) {
        return httpClient
                .PUT("/patients/patient")
                .withRequestBody(record)
                .invoke();
    }

    @Override
    public StrictResponse<akka.util.ByteString> update(StorePatientRecord record) {
        return httpClient.POST("/patients/patient").withRequestBody(record).invoke();
    }

    @Override
    public StrictResponse<StorePatientRecord> get(String pharmacyId, String patientId) {
        return httpClient.GET("/patients/" + toId(pharmacyId, patientId))
                .responseBodyAs(StorePatientRecord.class)
                .invoke();
    }

    @Override
    public StrictResponse<akka.util.ByteString> delete(String pharmacyId, String patientId) {
        return httpClient.DELETE("/patients/" + toId(pharmacyId, patientId)).invoke();
    }

    private String toId(String pharmacyId, String patientId) {
        return pharmacyId + "-" + patientId;
    }

}
