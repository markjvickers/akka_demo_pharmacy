package central.patient.api;

import akka.javasdk.http.StrictResponse;
import akka.javasdk.testkit.TestKitSupport;
import central.patient.domain.StorePatientRecord;
import central.patient.domain.StorePatientRecordId;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class StorePatientRecordIntegrationTest extends TestKitSupport {

    private static final Logger logger = LoggerFactory.getLogger(StorePatientRecordIntegrationTest.class);

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
    public void patientRecordLifecycle() throws JsonProcessingException {

        var id = StorePatientRecordId.fromRecord(record);

        {
            logger.info("patient record does not yet exist");
            assertEquals("404 Not Found",  failedGet(id).status().toString());

            logger.info("can't delete");
            assertEquals("400 Bad Request",  delete(id).status().toString());

            logger.info("can't update");
            assertEquals("400 Bad Request", update(record).status().toString());
        }

        {
            logger.info("add record");
            assertEquals("200 OK", add(record).status().toString());
        }

        {
            logger.info("patient record should now exist");
            var getResult = get(id);
            assertEquals("200 OK",  getResult.status().toString());
            assertEquals(record,  getResult.body());
        }

        {
            logger.info("can no longer create");
            assertEquals("400 Bad Request", add(record).status().toString());
        }

        {
            logger.info("attempt to update");
            var call = update(record2);
            assertEquals("200 OK", call.status().toString());
        }

        {
            logger.info("Should be updated");
            var getResult = get(id);
            assertEquals("200 OK",  getResult.status().toString());
            assertEquals(record2,  getResult.body());
        }

        {
            logger.info("Can now delete");
            assertEquals("200 OK",  delete(id).status().toString());
        }

        {
            logger.info("Can no longer get");
            assertEquals("400 Bad Request",  failedGet(id).status().toString());
            logger.info("or delete");
            assertEquals("400 Bad Request",  delete(id).status().toString());
            logger.info("or add");
            assertEquals("400 Bad Request",  add(record2).status().toString());
            logger.info("or update");
            assertEquals("400 Bad Request",  update(record2).status().toString());
        }

    }

    private StrictResponse<akka.util.ByteString> add(StorePatientRecord record) {
        return httpClient
                .PUT("/patients/patient")
                .withRequestBody(record)
                .invoke();
    }

    private StrictResponse<akka.util.ByteString> update(StorePatientRecord record) {
        return httpClient.POST("/patients/patient").withRequestBody(record).invoke();
    }

    private StrictResponse<StorePatientRecord> get(StorePatientRecordId id) {
        return httpClient.GET("/patients/" + id)
                .responseBodyAs(StorePatientRecord.class)
                .invoke();
    }

    private StrictResponse<akka.util.ByteString> failedGet(StorePatientRecordId id) {
        return httpClient.GET("/patients/" + id).invoke();
    }

    private StrictResponse<akka.util.ByteString> delete(StorePatientRecordId id) {
       return httpClient.DELETE("/patients/" + id).invoke();
    }

}
