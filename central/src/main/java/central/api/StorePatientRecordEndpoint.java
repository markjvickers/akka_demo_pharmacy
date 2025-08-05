package central.api;

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
import central.domain.StorePatientRecord;
import central.domain.StorePatientRecordId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import central.application.StorePatientRecordEntity;

@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
//@JWT(validate = JWT.JwtMethodMode.BEARER_TOKEN)
@HttpEndpoint("/patients")
public class StorePatientRecordEndpoint extends AbstractHttpEndpoint {

    private final ComponentClient componentClient;

    private static final Logger logger = LoggerFactory.getLogger(StorePatientRecordEndpoint.class);

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
            throw HttpException.error(StatusCodes.NOT_FOUND, "No such PatientRecord");
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
        logger.info("Deleting store patient record with id={}", store_patient_id);
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

}
