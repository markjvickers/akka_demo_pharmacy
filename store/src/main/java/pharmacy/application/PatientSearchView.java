package pharmacy.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.annotations.Query;
import akka.javasdk.annotations.Table;
import akka.javasdk.view.TableUpdater;
import akka.javasdk.view.View;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pharmacy.domain.PatientRecord;
import pharmacy.domain.PatientRecordEvent;

@ComponentId("patient-search-view")
public class PatientSearchView extends View {

    private static final Logger logger = LoggerFactory.getLogger(
        PatientSearchView.class
    );

    // Define the Java record for search criteria
    public record FirstAndLastNameSearchCriteria(
        String firstName,
        String lastName
    ) {}

    public record PatientRecords(List<PatientRecord> patientRecords) {}

    @Query(
        """
        SELECT * as patientRecords FROM patientRecords
        WHERE text_search(firstName, :firstName)
        """
    )
    public QueryEffect<PatientRecords> searchByFirstName(String firstName) {
        logger.info("Searching patients by first name: {}", firstName);
        return queryResult();
    }

    @Query(
        """
        SELECT * as patientRecords FROM patientRecords
        WHERE text_search(lastName, :lastName)
        """
    )
    public QueryEffect<PatientRecords> searchByLastName(String lastName) {
        logger.info("Searching patients by last name: {}", lastName);
        return queryResult();
    }

    @Query(
        """
        SELECT * as patientRecords FROM patientRecords
        WHERE firstName = :firstName
        AND lastName = :lastName
        """
    )
    public QueryEffect<PatientRecords> searchByFirstAndLastName(
        FirstAndLastNameSearchCriteria criteria
    ) {
        logger.info(
            "Searching patients by first name: {} and last name: {}",
            criteria.firstName(),
            criteria.lastName()
        );
        return queryResult();
    }

    @Query(
        """
        SELECT * as patientRecords FROM patientRecords
        WHERE firstName = :searchTerm
        OR lastName = :searchTerm
        """
    )
    public QueryEffect<PatientRecords> searchByName(String searchTerm) {
        logger.info("Searching patients by name term: {}", searchTerm);
        return queryResult();
    }

    @Table("patientRecords")
    @Consume.FromEventSourcedEntity(
        value = PatientRecordEntity.class,
        ignoreUnknown = true
    )
    public static class PatientTableUpdater
        extends TableUpdater<PatientRecord> {

        private static final Logger logger = LoggerFactory.getLogger(
            PatientTableUpdater.class
        );

        public Effect<PatientRecord> onPatientRecordCreated(
            PatientRecordEvent.PatientRecordCreated event
        ) {
            logger.info(
                "Patient record created in search view: {}",
                event.patientRecord().patientId()
            );
            return effects().updateRow(event.patientRecord());
        }

        public Effect<PatientRecord> onPatientRecordUpdated(
            PatientRecordEvent.PatientRecordUpdated event
        ) {
            logger.info(
                "Patient record updated in search view: {}",
                event.patientRecord().patientId()
            );
            return effects().updateRow(event.patientRecord());
        }

        public Effect<PatientRecord> onPatientRecordDeleted(
            PatientRecordEvent.PatientRecordDeleted event
        ) {
            logger.info(
                "Patient record deleted from search view: {}",
                event.patientId()
            );
            return effects().deleteRow();
        }

        public Effect<PatientRecord> onPatientRecordMerged(
            PatientRecordEvent.PatientRecordMerged event
        ) {
            logger.info(
                "Patient record merged in search view: {}",
                event.updated().patientId()
            );
            return effects().updateRow(event.updated());
        }

        public Effect<PatientRecord> onPatientOptedInForSms(
            PatientRecordEvent.PatientOptedInForSms event
        ) {
            // SMS opt-in doesn't change the patient data, so keep current state
            // logger.info("Patient opted in for SMS: {}", rowState().patientId());
            return effects().updateRow(rowState());
        }
    }
}
