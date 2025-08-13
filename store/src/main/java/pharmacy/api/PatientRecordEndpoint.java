package pharmacy.api;

import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.Delete;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.annotations.http.Post;
import akka.javasdk.annotations.http.Put;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.http.HttpException;
import akka.javasdk.http.HttpResponses;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pharmacy.application.PatientRecordEntity;
import pharmacy.application.PatientRecordEntity.PatientMergeRequest;
import pharmacy.application.PatientSearchView;
import pharmacy.application.central.delivery.PatientRecordDeliverySummary;
import pharmacy.application.central.delivery.PatientRecordDeliveryView;
import pharmacy.domain.PatientRecord;
import pharmacy.domain.PharmacyId;

@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
@HttpEndpoint("/patients")
public class PatientRecordEndpoint {

    private final ComponentClient componentClient;
    private final PharmacyId pharmacyId;

    private static final Logger logger = LoggerFactory.getLogger(
        PatientRecordEndpoint.class
    );

    public PatientRecordEndpoint(
        ComponentClient componentClient,
        PharmacyId pharmacyId
    ) {
        this.componentClient = componentClient;
        this.pharmacyId = pharmacyId;
    }

    public record PatientCreateRequest(
        String firstName,
        String lastName,
        Optional<String> prefName,
        String dateOfBirth,
        String phoneNumber,
        String provHealthNumber,
        Optional<String> unitNumber,
        String streetNumber,
        String streetName,
        String city,
        String province,
        String postalCode,
        String country,
        String langPref,
        boolean smsOptInPref
    ) {}

    @Get("/{patientId}")
    public PatientRecord get(String patientId) {
        logger.info("Get patient id={}", patientId);
        var record = componentClient
            .forEventSourcedEntity(patientId)
            .method(PatientRecordEntity::getRecord)
            .invoke();
        if (record.isPresent()) return record.get();
        else throw HttpException.error(
            StatusCodes.NOT_FOUND,
            "Patient not found"
        );
    }

    @Put("/patient")
    public String create(PatientCreateRequest request) {
        var patientId = java.util.UUID.randomUUID().toString();
        logger.info("Handling create patient request, id={}", patientId);
        var record = getPatientRecordFromCreateRequest(request, patientId);
        componentClient
            .forEventSourcedEntity(patientId)
            .method(PatientRecordEntity::create)
            .invoke(record);
        return patientId;
    }

    @Post("/patient/merge")
    public HttpResponse merge(PatientMergeRequest mergeRequest) {
        logger.info(
            "Request to merge patient id={} with patient {}",
            mergeRequest.updated().patientId(),
            mergeRequest.mergedPatientId()
        );
        componentClient
            .forEventSourcedEntity(mergeRequest.updated().patientId())
            .method(PatientRecordEntity::merge)
            .invoke(mergeRequest);
        return HttpResponses.ok();
    }

    @Put("/patient/{patientId}")
    public HttpResponse update(String patientId, PatientRecord record) {
        logger.info("Request to update patient id={}", patientId);
        componentClient
            .forEventSourcedEntity(patientId)
            .method(PatientRecordEntity::update)
            .invoke(record);
        return HttpResponses.ok();
    }

    @Delete("/patient/{patientId}")
    public HttpResponse delete(String patientId) {
        logger.info("Request to delete patient id={}", patientId);
        componentClient
            .forEventSourcedEntity(patientId)
            .method(PatientRecordEntity::delete)
            .invoke();
        return HttpResponses.ok();
    }

    @Get("/delivery/summary")
    public PatientRecordDeliverySummary getDeliverySummary() {
        var required = componentClient
            .forView()
            .method(PatientRecordDeliveryView::getRequiredDeliveryCount)
            .invoke()
            .amount();

        var finished = componentClient
            .forView()
            .method(PatientRecordDeliveryView::getFinishedDeliveryCount)
            .invoke()
            .amount();

        return new PatientRecordDeliverySummary(
            required,
            finished,
            (required - finished)
        );
    }

    public record PatientSearchCriteria(
        Optional<String> firstName,
        Optional<String> lastName
    ) {}

    private String clean(Optional<String> st) {
        return st
            .filter(s -> !s.trim().isEmpty())
            .map(String::trim)
            .orElse(null);
    }

    @Post("/search")
    public List<PatientRecord> searchPatients(PatientSearchCriteria criteria) {
        logger.info(
            "Search request - firstName: {}, lastName: {}",
            criteria.firstName().orElse("N/A"),
            criteria.lastName().orElse("N/A")
        );

        String firstName = clean(criteria.firstName());
        String lastName = clean(criteria.lastName());

        if (firstName != null) {
            // Search by first name only
            return componentClient
                .forView()
                .method(PatientSearchView::searchByFirstName)
                .invoke(firstName)
                .patientRecords();
        } else if (lastName != null) {
            // Search by last name only
            return componentClient
                .forView()
                .method(PatientSearchView::searchByLastName)
                .invoke(lastName)
                .patientRecords();
        } else {
            // No search parameters provided
            throw HttpException.badRequest(
                "At least one search parameter (firstName, lastName) must be provided"
            );
        }
    }

    private PatientRecord getPatientRecordFromCreateRequest(
        PatientCreateRequest r,
        String patientId
    ) {
        return new PatientRecord(
            pharmacyId.id(),
            patientId,
            r.firstName,
            r.lastName,
            r.prefName,
            r.dateOfBirth,
            r.phoneNumber,
            r.provHealthNumber,
            r.unitNumber,
            r.streetNumber,
            r.streetName,
            r.city,
            r.province,
            r.postalCode,
            r.country,
            r.langPref,
            r.smsOptInPref
        );
    }
}
