package central.pharmacy.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.annotations.Query;
import akka.javasdk.annotations.Table;
import akka.javasdk.view.TableUpdater;
import akka.javasdk.view.View;
import central.pharmacy.domain.Pharmacy;
import central.pharmacy.domain.PharmacyEvent;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ComponentId("pharmacy-search-view")
public class PharmacySearchView extends View {

    private static final Logger logger = LoggerFactory.getLogger(
        PharmacySearchView.class
    );

    // Define Java records for search criteria
    public record CitySearchCriteria(String city) {}

    public record ProvinceSearchCriteria(String province) {}

    public record CityAndProvinceCriteria(String city, String province) {}

    public record PostalCodeSearchCriteria(String postalCode) {}

    public record PhoneNumberSearchCriteria(String phoneNumber) {}

    public record AddressSearchCriteria(String streetAddress) {}

    public record ProvinceAndPostalCodeCriteria(String province, String postalCode) {}

    public record CityAndPostalCodeCriteria(String city, String postalCode) {}

    public record AllLocationCriteria(String city, String province, String postalCode) {}

    public record Pharmacies(List<Pharmacy> pharmacies) {}

    @Query(
        """
        SELECT * as pharmacies FROM pharmacies
        WHERE city = :city
        """
    )
    public QueryEffect<Pharmacies> searchByCity(String city) {
        logger.info("Searching pharmacies by city: {}", city);
        return queryResult();
    }

    @Query(
        """
        SELECT * as pharmacies FROM pharmacies
        WHERE province = :province
        """
    )
    public QueryEffect<Pharmacies> searchByProvince(String province) {
        logger.info("Searching pharmacies by province: {}", province);
        return queryResult();
    }

    @Query(
        """
        SELECT * as pharmacies FROM pharmacies
        WHERE city = :city
        AND province = :province
        """
    )
    public QueryEffect<Pharmacies> searchByCityAndProvince(
        CityAndProvinceCriteria criteria
    ) {
        logger.info(
            "Searching pharmacies by city: {} and province: {}",
            criteria.city(),
            criteria.province()
        );
        return queryResult();
    }

    @Query(
        """
        SELECT * as pharmacies FROM pharmacies
        WHERE postalCode = :postalCode
        """
    )
    public QueryEffect<Pharmacies> searchByPostalCode(String postalCode) {
        logger.info("Searching pharmacies by postal code: {}", postalCode);
        return queryResult();
    }

    @Query(
        """
        SELECT * as pharmacies FROM pharmacies
        WHERE phoneNumber = :phoneNumber
        """
    )
    public QueryEffect<Pharmacies> searchByPhoneNumber(String phoneNumber) {
        logger.info("Searching pharmacies by phone number: {}", phoneNumber);
        return queryResult();
    }

    @Query(
        """
        SELECT * as pharmacies FROM pharmacies
        WHERE streetAddress LIKE '%' || :streetAddress || '%'
        """
    )
    public QueryEffect<Pharmacies> searchByAddress(String streetAddress) {
        logger.info("Searching pharmacies by address: {}", streetAddress);
        return queryResult();
    }

    @Query(
        """
        SELECT * as pharmacies FROM pharmacies
        WHERE province = :province
        AND postalCode = :postalCode
        """
    )
    public QueryEffect<Pharmacies> searchByProvinceAndPostalCode(
        ProvinceAndPostalCodeCriteria criteria
    ) {
        logger.info(
            "Searching pharmacies by province: {} and postal code: {}",
            criteria.province(),
            criteria.postalCode()
        );
        return queryResult();
    }

    @Query(
        """
        SELECT * as pharmacies FROM pharmacies
        WHERE city = :city
        AND postalCode = :postalCode
        """
    )
    public QueryEffect<Pharmacies> searchByCityAndPostalCode(
        CityAndPostalCodeCriteria criteria
    ) {
        logger.info(
            "Searching pharmacies by city: {} and postal code: {}",
            criteria.city(),
            criteria.postalCode()
        );
        return queryResult();
    }

    @Query(
        """
        SELECT * as pharmacies FROM pharmacies
        WHERE city = :city
        AND province = :province
        AND postalCode = :postalCode
        """
    )
    public QueryEffect<Pharmacies> searchByAllLocation(
        AllLocationCriteria criteria
    ) {
        logger.info(
            "Searching pharmacies by city: {}, province: {} and postal code: {}",
            criteria.city(),
            criteria.province(),
            criteria.postalCode()
        );
        return queryResult();
    }

    @Query(
        """
        SELECT * as pharmacies FROM pharmacies
        WHERE city LIKE '%' || :searchTerm || '%'
        OR streetAddress LIKE '%' || :searchTerm || '%'
        OR phoneNumber LIKE '%' || :searchTerm || '%'
        """
    )
    public QueryEffect<Pharmacies> searchByTerm(String searchTerm) {
        logger.info("Searching pharmacies by term: {}", searchTerm);
        return queryResult();
    }

    @Table("pharmacies")
    @Consume.FromEventSourcedEntity(
        value = PharmacyEntity.class,
        ignoreUnknown = true
    )
    public static class PharmacyTableUpdater
        extends TableUpdater<Pharmacy> {

        private static final Logger logger = LoggerFactory.getLogger(
            PharmacyTableUpdater.class
        );

        public Effect<Pharmacy> onPharmacyCreated(
            PharmacyEvent.PharmacyCreated event
        ) {
            logger.info(
                "Pharmacy created in search view: {}",
                event.pharmacy().pharmacyId()
            );
            return effects().updateRow(event.pharmacy());
        }

        public Effect<Pharmacy> onPharmacyUpdated(
            PharmacyEvent.PharmacyUpdated event
        ) {
            logger.info(
                "Pharmacy updated in search view: {}",
                event.pharmacy().pharmacyId()
            );
            return effects().updateRow(event.pharmacy());
        }

        public Effect<Pharmacy> onPharmacyDeleted(
            PharmacyEvent.PharmacyDeleted event
        ) {
            logger.info(
                "Pharmacy deleted from search view"
            );
            return effects().deleteRow();
        }
    }
}
