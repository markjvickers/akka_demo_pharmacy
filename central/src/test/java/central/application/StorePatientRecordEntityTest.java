package central.application;

import akka.Done;
import akka.javasdk.testkit.EventSourcedTestKit;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import central.domain.StorePatientRecord;
import central.domain.StorePatientRecordEvent;
import central.domain.StorePatientRecordEvent.StorePatientRecordCreated;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StorePatientRecordEntityTest {

    private static final Logger logger = LoggerFactory.getLogger(StorePatientRecordEntityTest.class);

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
    public void testCreatePatient() {

        var testKit = EventSourcedTestKit.of(StorePatientRecordEntity::new);

        {

            var result = testKit.method(StorePatientRecordEntity::create).invoke(record);
            assertEquals(Done.getInstance(), result.getReply());
            var created = result.getNextEventOfType(StorePatientRecordCreated.class);
            assertEquals(record, created.patientRecord());
            assertEquals(1, result.getAllEvents().size());
        }

        {
            // try and add the same record again
            var result = testKit
                    .method(StorePatientRecordEntity::create)
                    .invoke(record);
            assertEquals(0, result.getAllEvents().size());
        }

    }

    @Test
    public void testUpdateEventThatDoesNotExist() {
        var testKit = EventSourcedTestKit.of(StorePatientRecordEntity::new);
        {
            logger.info("attempt to update a record that does not exist");
            var result = testKit.method(StorePatientRecordEntity::update).invoke(record);
            assertTrue(result.isError());
            assertEquals("StorePatientRecord not found.", result.getError());
        }
    }

    @Test
    public void testUpdateEvent() {
        var testKit = EventSourcedTestKit.of(StorePatientRecordEntity::new);
        {
            var result = testKit.method(StorePatientRecordEntity::create).invoke(record);
            assertEquals(1, result.getAllEvents().size());
        }
    }

    @Test
    public void testDeleteEvent() {
        var testKit = EventSourcedTestKit.of(StorePatientRecordEntity::new);
        {
            var result = testKit.method(StorePatientRecordEntity::create).invoke(record);
            assertEquals(1, result.getAllEvents().size());

            var result2 = testKit.method(StorePatientRecordEntity::delete).invoke();
            assertEquals(1, result2.getAllEvents().size());
            var deleted = result2.getNextEventOfType(StorePatientRecordEvent.StorePatientRecordDeleted.class);
            assertNotNull(deleted);
        }
    }

    @Test
    public void testDeleteEventThatDoesNotExist() {
        var testKit = EventSourcedTestKit.of(StorePatientRecordEntity::new);
        {
            var result = testKit.method(StorePatientRecordEntity::delete).invoke();
            assertTrue(result.isError());
        }
    }

}
