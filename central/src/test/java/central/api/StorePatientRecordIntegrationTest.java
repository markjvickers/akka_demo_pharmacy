package central.api;

import static org.assertj.core.api.Assertions.assertThat;

import akka.javasdk.JsonSupport;
import akka.javasdk.http.StrictResponse;
import akka.javasdk.testkit.TestKitSupport;
import central.domain.StorePatientRecord;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;


public class StorePatientRecordIntegrationTest extends TestKitSupport {


    private final StorePatientRecord record = getRecord("001", "Alicia");
    private final StorePatientRecord record2 = getRecord("001", "Alice");

    private StorePatientRecord getRecord(String patientId, String firstName) {
        return new StorePatientRecord(
                "101",
                patientId,
                firstName,
                "Summers",
                Optional.of("Ally"),
                "1985-07-14",
                "555-123-4567",
                "PHN1234567",
                Optional.of("5B"),
                "123",
                "Main Street",
                "Vancouver",
                "BC",
                "V5K0A1",
                "Canada",
                "en",
                true);

    }

    @Test
    public void shouldCreatePatientRecord() throws JsonProcessingException {
        StrictResponse<String> call = httpClient
                .PUT("/patients/patient")
                .withRequestBody(record)
                .responseBodyAs(String.class)
                .invoke();

        assertThat(call.body()).isEqualTo("issuer: my-issuer, subject: my-subject");
    }

}
