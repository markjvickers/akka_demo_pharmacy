package central.patient.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.annotations.Query;
import akka.javasdk.annotations.Table;
import akka.javasdk.view.TableUpdater;
import akka.javasdk.view.View;
import central.patient.domain.StorePatientRecord;
import central.patient.domain.StorePatientRecordEvent;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ComponentId("store-patient-record-view")
public class StorePatientRecordView extends View {

    private static final Logger logger = LoggerFactory.getLogger(
        StorePatientRecordView.class
    );

    // Define the Java record for search criteria
    public record StorePatientSearchCriteria(
        Optional<String> storeId,
        Optional<String> province,
        Optional<String> firstName,
        Optional<String> lastName,
        Optional<String> healthNumber
    ) {}

    public record StoreIdAndNameCriteria(
        String storeId,
        String firstName,
        String lastName
    ) {}

    public record StoreIdAndFirstNameCriteria(
        String storeId,
        String firstName
    ) {}

    public record StoreIdAndLastNameCriteria(String storeId, String lastName) {}

    public record StoreIdAndProvinceCriteria(String storeId, String province) {}

    public record StoreIdAndHealthNumberCriteria(
        String storeId,
        String healthNumber
    ) {}

    public record FullNameCriteria(String firstName, String lastName) {}

    public record ProvinceAndFirstNameCriteria(
        String province,
        String firstName
    ) {}

    public record ProvinceAndLastNameCriteria(
        String province,
        String lastName
    ) {}

    public record ProvinceAndNameCriteria(
        String province,
        String firstName,
        String lastName
    ) {}

    public record AllCriteria(
        String storeId,
        String province,
        String firstName,
        String lastName,
        String healthNumber
    ) {}

    public record StorePatientRecords(
        List<StorePatientRecord> patientRecords
    ) {}

    @Query(
        """
        SELECT * as patientRecords FROM storePatientRecords
        WHERE pharmacyId = :storeId
        """
    )
    public QueryEffect<StorePatientRecords> searchByStoreId(String storeId) {
        logger.info("Searching patients by store ID: {}", storeId);
        return queryResult();
    }

    @Query(
        """
        SELECT * as patientRecords FROM storePatientRecords
        WHERE province = :province
        """
    )
    public QueryEffect<StorePatientRecords> searchByProvince(String province) {
        logger.info("Searching patients by province: {}", province);
        return queryResult();
    }

    @Query(
        """
        SELECT * as patientRecords FROM storePatientRecords
        WHERE firstName = :firstName
        """
    )
    public QueryEffect<StorePatientRecords> searchByFirstName(
        String firstName
    ) {
        logger.info("Searching patients by first name: {}", firstName);
        return queryResult();
    }

    @Query(
        """
        SELECT * as patientRecords FROM storePatientRecords
        WHERE lastName = :lastName
        """
    )
    public QueryEffect<StorePatientRecords> searchByLastName(String lastName) {
        logger.info("Searching patients by last name: {}", lastName);
        return queryResult();
    }

    @Query(
        """
        SELECT * as patientRecords FROM storePatientRecords
        WHERE provHealthNumber = :healthNumber
        """
    )
    public QueryEffect<StorePatientRecords> searchByHealthNumber(
        String healthNumber
    ) {
        logger.info("Searching patients by health number: {}", healthNumber);
        return queryResult();
    }

    @Query(
        """
        SELECT * as patientRecords FROM storePatientRecords
        WHERE pharmacyId = :storeId
        AND firstName = :firstName
        """
    )
    public QueryEffect<StorePatientRecords> searchByStoreIdAndFirstName(
        StoreIdAndFirstNameCriteria criteria
    ) {
        logger.info(
            "Searching patients by store ID: {} and first name: {}",
            criteria.storeId(),
            criteria.firstName()
        );
        return queryResult();
    }

    @Query(
        """
        SELECT * as patientRecords FROM storePatientRecords
        WHERE pharmacyId = :storeId
        AND lastName = :lastName
        """
    )
    public QueryEffect<StorePatientRecords> searchByStoreIdAndLastName(
        StoreIdAndLastNameCriteria criteria
    ) {
        logger.info(
            "Searching patients by store ID: {} and last name: {}",
            criteria.storeId(),
            criteria.lastName()
        );
        return queryResult();
    }

    @Query(
        """
        SELECT * as patientRecords FROM storePatientRecords
        WHERE pharmacyId = :storeId
        AND firstName = :firstName
        AND lastName = :lastName
        """
    )
    public QueryEffect<StorePatientRecords> searchByStoreIdAndFullName(
        StoreIdAndNameCriteria criteria
    ) {
        logger.info(
            "Searching patients by store ID: {}, first name: {} and last name: {}",
            criteria.storeId(),
            criteria.firstName(),
            criteria.lastName()
        );
        return queryResult();
    }

    @Query(
        """
        SELECT * as patientRecords FROM storePatientRecords
        WHERE pharmacyId = :storeId
        AND province = :province
        """
    )
    public QueryEffect<StorePatientRecords> searchByStoreIdAndProvince(
        StoreIdAndProvinceCriteria criteria
    ) {
        logger.info(
            "Searching patients by store ID: {} and province: {}",
            criteria.storeId(),
            criteria.province()
        );
        return queryResult();
    }

    @Query(
        """
        SELECT * as patientRecords FROM storePatientRecords
        WHERE pharmacyId = :storeId
        AND provHealthNumber = :healthNumber
        """
    )
    public QueryEffect<StorePatientRecords> searchByStoreIdAndHealthNumber(
        StoreIdAndHealthNumberCriteria criteria
    ) {
        logger.info(
            "Searching patients by store ID: {} and health number: {}",
            criteria.storeId(),
            criteria.healthNumber()
        );
        return queryResult();
    }

    @Query(
        """
        SELECT * as patientRecords FROM storePatientRecords
        WHERE firstName = :firstName
        AND lastName = :lastName
        """
    )
    public QueryEffect<StorePatientRecords> searchByFullName(
        FullNameCriteria criteria
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
        SELECT * as patientRecords FROM storePatientRecords
        WHERE province = :province
        AND firstName = :firstName
        """
    )
    public QueryEffect<StorePatientRecords> searchByProvinceAndFirstName(
        ProvinceAndFirstNameCriteria criteria
    ) {
        logger.info(
            "Searching patients by province: {} and first name: {}",
            criteria.province(),
            criteria.firstName()
        );
        return queryResult();
    }

    @Query(
        """
        SELECT * as patientRecords FROM storePatientRecords
        WHERE province = :province
        AND lastName = :lastName
        """
    )
    public QueryEffect<StorePatientRecords> searchByProvinceAndLastName(
        ProvinceAndLastNameCriteria criteria
    ) {
        logger.info(
            "Searching patients by province: {} and last name: {}",
            criteria.province(),
            criteria.lastName()
        );
        return queryResult();
    }

    @Query(
        """
        SELECT * as patientRecords FROM storePatientRecords
        WHERE province = :province
        AND firstName = :firstName
        AND lastName = :lastName
        """
    )
    public QueryEffect<StorePatientRecords> searchByProvinceAndFullName(
        ProvinceAndNameCriteria criteria
    ) {
        logger.info(
            "Searching patients by province: {}, first name: {} and last name: {}",
            criteria.province(),
            criteria.firstName(),
            criteria.lastName()
        );
        return queryResult();
    }

    @Query(
        """
        SELECT * as patientRecords FROM storePatientRecords
        WHERE pharmacyId = :storeId
        AND province = :province
        AND firstName = :firstName
        AND lastName = :lastName
        AND provHealthNumber = :healthNumber
        """
    )
    public QueryEffect<StorePatientRecords> searchByAllCriteria(
        AllCriteria criteria
    ) {
        logger.info(
            "Searching patients by all criteria - store ID: {}, province: {}, first name: {}, last name: {}, health number: {}",
            criteria.storeId(),
            criteria.province(),
            criteria.firstName(),
            criteria.lastName(),
            criteria.healthNumber()
        );
        return queryResult();
    }

    @Table("storePatientRecords")
    @Consume.FromEventSourcedEntity(
        value = StorePatientRecordEntity.class,
        ignoreUnknown = true
    )
    public static class StorePatientTableUpdater
        extends TableUpdater<StorePatientRecord> {

        private static final Logger logger = LoggerFactory.getLogger(
            StorePatientTableUpdater.class
        );

        public Effect<StorePatientRecord> onStorePatientRecordCreated(
            StorePatientRecordEvent.StorePatientRecordCreated event
        ) {
            logger.info(
                "Store patient record created in search view: store={}, patient={}",
                event.patientRecord().pharmacyId(),
                event.patientRecord().patientId()
            );
            return effects().updateRow(event.patientRecord());
        }

        public Effect<StorePatientRecord> onStorePatientRecordUpdated(
            StorePatientRecordEvent.StorePatientRecordUpdated event
        ) {
            logger.info(
                "Store patient record updated in search view: store={}, patient={}",
                event.patientRecord().pharmacyId(),
                event.patientRecord().patientId()
            );
            return effects().updateRow(event.patientRecord());
        }

        public Effect<StorePatientRecord> onStorePatientRecordDeleted(
            StorePatientRecordEvent.StorePatientRecordDeleted event
        ) {
            logger.info("Store patient record deleted from search view");
            return effects().deleteRow();
        }
    }
}
