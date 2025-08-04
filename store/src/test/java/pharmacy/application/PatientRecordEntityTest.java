package pharmacy.application;

import akka.Done;
import akka.javasdk.testkit.EventSourcedTestKit;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pharmacy.domain.PatientRecord;
import pharmacy.domain.PatientRecordEvent;
import pharmacy.domain.PatientRecordEvent.PatientRecordCreated;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PatientRecordEntityTest {

    private static final Logger logger = LoggerFactory.getLogger(PatientRecordEntityTest.class);

    private final PatientRecord record = getRecord("001", "Alicia", false);
    private final PatientRecord record2 = getRecord("001", "Alice", false);
    private final PatientRecord record_with_optin = getRecord("002", "Alicia", true);

    private PatientRecord getRecord(String patientId, String firstName, Boolean smsOptIn) {
        return new PatientRecord(
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
                smsOptIn);

    }

    @Test
    public void testCreatePatient() {

        var testKit = EventSourcedTestKit.of(PatientRecordEntity::new);

        {

            var result = testKit.method(PatientRecordEntity::create).invoke(record);
            assertEquals(Done.getInstance(), result.getReply());
            var created = result.getNextEventOfType(PatientRecordCreated.class);
            assertEquals(record, created.patientRecord());
            assertEquals(1, result.getAllEvents().size());
        }

        {
            // try and add the same record again
            var result = testKit
                    .method(PatientRecordEntity::create)
                    .invoke(record);
            assertEquals(0, result.getAllEvents().size());
        }

    }

    @Test
    public void testCreatePatient_withOptIn() {
        var testKit = EventSourcedTestKit.of(PatientRecordEntity::new);
        {
            var result = testKit.method(PatientRecordEntity::create).invoke(record_with_optin);
            assertEquals(Done.getInstance(), result.getReply());
            var created = result.getNextEventOfType(PatientRecordCreated.class);
            assertEquals(record_with_optin, created.patientRecord());
            assertEquals(2, result.getAllEvents().size());
            assertNotNull(result.getNextEventOfType(PatientRecordEvent.PatientOptedInForSms.class));
        }
    }


    @Test
    public void testUpdateEventThatDoesNotExist() {
        var testKit = EventSourcedTestKit.of(PatientRecordEntity::new);
        {
            logger.info("attempt to update a record that does not exist");
            var result = testKit.method(PatientRecordEntity::update).invoke(record);
            assertTrue(result.isError());
            assertEquals("PatientRecord not found.", result.getError());
        }
    }

    @Test
    public void testUpdateEvent() {
        var testKit = EventSourcedTestKit.of(PatientRecordEntity::new);
        {
            var result = testKit.method(PatientRecordEntity::create).invoke(record);
            assertEquals(1, result.getAllEvents().size());

            var result2 = testKit.method(PatientRecordEntity::update).invoke(record2);
            assertEquals(1, result2.getAllEvents().size());
            var update = result2.getNextEventOfType(PatientRecordEvent.PatientRecordUpdated.class);
            assertEquals(record2, update.patientRecord());
        }
    }

    @Test
    public void testUpdateEvent_changeOptIn() {
        var testKit = EventSourcedTestKit.of(PatientRecordEntity::new);
        {
            var result = testKit.method(PatientRecordEntity::create).invoke(record);
            assertEquals(1, result.getAllEvents().size());

            var updated = getRecord(record.patientId(), record.firstName(), true);
            var result2 = testKit.method(PatientRecordEntity::update).invoke(updated);
            assertEquals(2, result2.getAllEvents().size());
            var update = result2.getNextEventOfType(PatientRecordEvent.PatientRecordUpdated.class);
            assertEquals(updated, update.patientRecord());
            var optIn = result2.getNextEventOfType(PatientRecordEvent.PatientOptedInForSms.class);
            assertNotNull(optIn);
        }
    }

    @Test
    public void testDeleteEvent() {
        var testKit = EventSourcedTestKit.of(PatientRecordEntity::new);
        {
            var result = testKit.method(PatientRecordEntity::create).invoke(record);
            assertEquals(1, result.getAllEvents().size());

            var result2 = testKit.method(PatientRecordEntity::delete).invoke();
            assertEquals(1, result2.getAllEvents().size());
            var deleted = result2.getNextEventOfType(PatientRecordEvent.PatientRecordDeleted.class);
            assertNotNull(deleted);
        }
    }

    @Test
    public void testDeleteEventThatDoesNotExist() {
        var testKit = EventSourcedTestKit.of(PatientRecordEntity::new);
        {
            var result = testKit.method(PatientRecordEntity::delete).invoke();
            assertTrue(result.isError());
        }
    }

}
