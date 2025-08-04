package pharmacy.api;


import akka.http.javadsl.model.HttpResponse;
import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.Delete;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.annotations.http.Post;
import akka.javasdk.annotations.http.Put;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.http.HttpResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;
import pharmacy.application.PatientRecordEntity;
import pharmacy.domain.PatientRecord;

@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
@HttpEndpoint("/patients")
public class PatientRecordEndpoint {

    private final ComponentClient componentClient;

    private static final Logger logger = LoggerFactory.getLogger(PatientRecordEndpoint.class);

    public PatientRecordEndpoint(ComponentClient componentClient) {
        this.componentClient = componentClient;
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
        return componentClient
                .forEventSourcedEntity(patientId)
                .method(PatientRecordEntity::getRecord)
                .invoke();
    }

    @Put("/patient")
    public String create(PatientCreateRequest request) {
        var patientId = java.util.UUID.randomUUID().toString();
        var record = getPatientRecordFromCreateRequest(request, patientId);
        componentClient
                .forEventSourcedEntity(patientId)
                .method(PatientRecordEntity::create)
                .invoke(record);
        return patientId;
    }

    @Delete("/patient/{patientId}")
    public HttpResponse delete(String patientId) {
        componentClient
                .forEventSourcedEntity(patientId)
                .method(PatientRecordEntity::delete)
                .invoke();
        return HttpResponses.ok();
    }

    private PatientRecord getPatientRecordFromCreateRequest(PatientCreateRequest r, String patientId) {
        //TODO: need to inject pharmacyId somehow
        var pharmacyId = "101";
        return new PatientRecord(
            pharmacyId,
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
