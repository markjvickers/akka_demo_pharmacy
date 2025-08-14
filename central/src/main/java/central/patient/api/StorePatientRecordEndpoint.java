package central.patient.api;

import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.javasdk.annotations.Acl;
//import akka.javasdk.annotations.JWT;
import akka.javasdk.annotations.http.Delete;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.annotations.http.Post;
import akka.javasdk.annotations.http.Put;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.http.AbstractHttpEndpoint;
import akka.javasdk.http.HttpException;
import akka.javasdk.http.HttpResponses;
import central.patient.application.StorePatientRecordEntity;
import central.patient.application.StorePatientRecordView;
import central.patient.domain.StorePatientRecord;
import central.patient.domain.StorePatientRecordId;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// @Acl(allow = @Acl.Matcher(service = "*"))
@Acl(allow = @Acl.Matcher(principal = Acl.Principal.ALL))
//@JWT(validate = JWT.JwtMethodMode.BEARER_TOKEN)
@HttpEndpoint("/patients")
public class StorePatientRecordEndpoint extends AbstractHttpEndpoint {

    private final ComponentClient componentClient;

    private static final Logger logger = LoggerFactory.getLogger(
        StorePatientRecordEndpoint.class
    );

    public StorePatientRecordEndpoint(ComponentClient componentClient) {
        this.componentClient = componentClient;
    }

    @Get("/{store_patient_id}")
    public StorePatientRecord get(String store_patient_id) {
        logger.info("Get patient with id={}", store_patient_id);
        var record = componentClient
            .forEventSourcedEntity(store_patient_id)
            .method(StorePatientRecordEntity::getRecord) // <1>
            .invoke();
        if (record.isPresent()) {
            return record.get();
        } else {
            throw HttpException.error(
                StatusCodes.NOT_FOUND,
                "No such PatientRecord"
            );
        }
    }

    @Put("/patient")
    public HttpResponse addRecord(StorePatientRecord record) {
        var id = StorePatientRecordId.fromRecord(record).toString();
        logger.info("Adding patient record with id={}", id);
        componentClient
            .forEventSourcedEntity(id)
            .method(StorePatientRecordEntity::create)
            .invoke(record);
        return HttpResponses.ok();
    }

    @Delete("/{store_patient_id}")
    public HttpResponse deleteRecord(String store_patient_id) {
        logger.info(
            "Deleting store patient record with id={}",
            store_patient_id
        );
        componentClient
            .forEventSourcedEntity(store_patient_id)
            .method(StorePatientRecordEntity::delete)
            .invoke();
        return HttpResponses.ok();
    }

    @Post("/patient")
    public HttpResponse updateRecord(StorePatientRecord record) {
        var id = StorePatientRecordId.fromRecord(record).toString();
        logger.info("Updating patient record with id={}", id);
        componentClient
            .forEventSourcedEntity(id.toString())
            .method(StorePatientRecordEntity::update)
            .invoke(record);
        return HttpResponses.ok();
    }

    public record StorePatientSearchCriteria(
        Optional<String> storeId,
        Optional<String> province,
        Optional<String> firstName,
        Optional<String> lastName,
        Optional<String> healthNumber
    ) {}

    private String clean(Optional<String> st) {
        return st
            .filter(s -> !s.trim().isEmpty())
            .map(String::trim)
            .orElse(null);
    }

    @Post("/search")
    public List<StorePatientRecord> searchStorePatients(
        StorePatientSearchCriteria criteria
    ) {
        logger.info(
            "Store patient search request - storeId: {}, province: {}, firstName: {}, lastName: {}, healthNumber: {}",
            criteria.storeId().orElse("N/A"),
            criteria.province().orElse("N/A"),
            criteria.firstName().orElse("N/A"),
            criteria.lastName().orElse("N/A"),
            criteria.healthNumber().orElse("N/A")
        );

        String storeId = clean(criteria.storeId());
        String province = clean(criteria.province());
        String firstName = clean(criteria.firstName());
        String lastName = clean(criteria.lastName());
        String healthNumber = clean(criteria.healthNumber());

        // Count non-null criteria
        int criteriaCount = 0;
        if (storeId != null) criteriaCount++;
        if (province != null) criteriaCount++;
        if (firstName != null) criteriaCount++;
        if (lastName != null) criteriaCount++;
        if (healthNumber != null) criteriaCount++;

        if (criteriaCount == 0) {
            throw HttpException.badRequest(
                "At least one search parameter must be provided"
            );
        }

        // Handle all possible combinations based on non-null criteria
        if (criteriaCount == 5) {
            // All criteria provided
            return componentClient
                .forView()
                .method(StorePatientRecordView::searchByAllCriteria)
                .invoke(
                    new StorePatientRecordView.AllCriteria(
                        storeId,
                        province,
                        firstName,
                        lastName,
                        healthNumber
                    )
                )
                .patientRecords();
        } else if (criteriaCount == 4) {
            // Four criteria combinations
            if (
                storeId != null &&
                province != null &&
                firstName != null &&
                lastName != null
            ) {
                // Missing healthNumber - use searchByStoreIdAndFullName + filter by province
                return componentClient
                    .forView()
                    .method(StorePatientRecordView::searchByStoreIdAndFullName)
                    .invoke(
                        new StorePatientRecordView.StoreIdAndNameCriteria(
                            storeId,
                            firstName,
                            lastName
                        )
                    )
                    .patientRecords()
                    .stream()
                    .filter(p -> province.equals(p.province()))
                    .toList();
            }
            // Add other 4-criteria combinations as needed
        } else if (criteriaCount == 3) {
            // Three criteria combinations
            if (storeId != null && firstName != null && lastName != null) {
                return componentClient
                    .forView()
                    .method(StorePatientRecordView::searchByStoreIdAndFullName)
                    .invoke(
                        new StorePatientRecordView.StoreIdAndNameCriteria(
                            storeId,
                            firstName,
                            lastName
                        )
                    )
                    .patientRecords();
            } else if (
                province != null && firstName != null && lastName != null
            ) {
                return componentClient
                    .forView()
                    .method(StorePatientRecordView::searchByProvinceAndFullName)
                    .invoke(
                        new StorePatientRecordView.ProvinceAndNameCriteria(
                            province,
                            firstName,
                            lastName
                        )
                    )
                    .patientRecords();
            }
            // Add other 3-criteria combinations as needed
        } else if (criteriaCount == 2) {
            // Two criteria combinations
            if (storeId != null && firstName != null) {
                return componentClient
                    .forView()
                    .method(StorePatientRecordView::searchByStoreIdAndFirstName)
                    .invoke(
                        new StorePatientRecordView.StoreIdAndFirstNameCriteria(
                            storeId,
                            firstName
                        )
                    )
                    .patientRecords();
            } else if (storeId != null && lastName != null) {
                return componentClient
                    .forView()
                    .method(StorePatientRecordView::searchByStoreIdAndLastName)
                    .invoke(
                        new StorePatientRecordView.StoreIdAndLastNameCriteria(
                            storeId,
                            lastName
                        )
                    )
                    .patientRecords();
            } else if (storeId != null && province != null) {
                return componentClient
                    .forView()
                    .method(StorePatientRecordView::searchByStoreIdAndProvince)
                    .invoke(
                        new StorePatientRecordView.StoreIdAndProvinceCriteria(
                            storeId,
                            province
                        )
                    )
                    .patientRecords();
            } else if (storeId != null && healthNumber != null) {
                return componentClient
                    .forView()
                    .method(
                        StorePatientRecordView::searchByStoreIdAndHealthNumber
                    )
                    .invoke(
                        new StorePatientRecordView.StoreIdAndHealthNumberCriteria(
                            storeId,
                            healthNumber
                        )
                    )
                    .patientRecords();
            } else if (firstName != null && lastName != null) {
                return componentClient
                    .forView()
                    .method(StorePatientRecordView::searchByFullName)
                    .invoke(
                        new StorePatientRecordView.FullNameCriteria(
                            firstName,
                            lastName
                        )
                    )
                    .patientRecords();
            } else if (province != null && firstName != null) {
                return componentClient
                    .forView()
                    .method(
                        StorePatientRecordView::searchByProvinceAndFirstName
                    )
                    .invoke(
                        new StorePatientRecordView.ProvinceAndFirstNameCriteria(
                            province,
                            firstName
                        )
                    )
                    .patientRecords();
            } else if (province != null && lastName != null) {
                return componentClient
                    .forView()
                    .method(StorePatientRecordView::searchByProvinceAndLastName)
                    .invoke(
                        new StorePatientRecordView.ProvinceAndLastNameCriteria(
                            province,
                            lastName
                        )
                    )
                    .patientRecords();
            }
        } else {
            // Single criteria
            if (storeId != null) {
                return componentClient
                    .forView()
                    .method(StorePatientRecordView::searchByStoreId)
                    .invoke(storeId)
                    .patientRecords();
            } else if (province != null) {
                return componentClient
                    .forView()
                    .method(StorePatientRecordView::searchByProvince)
                    .invoke(province)
                    .patientRecords();
            } else if (firstName != null) {
                return componentClient
                    .forView()
                    .method(StorePatientRecordView::searchByFirstName)
                    .invoke(firstName)
                    .patientRecords();
            } else if (lastName != null) {
                return componentClient
                    .forView()
                    .method(StorePatientRecordView::searchByLastName)
                    .invoke(lastName)
                    .patientRecords();
            } else if (healthNumber != null) {
                return componentClient
                    .forView()
                    .method(StorePatientRecordView::searchByHealthNumber)
                    .invoke(healthNumber)
                    .patientRecords();
            }
        }

        // Fallback - should not reach here
        throw HttpException.badRequest("Unable to process search criteria");
    }
}
