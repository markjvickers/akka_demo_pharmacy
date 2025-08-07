package central.pharmacy.api;

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
import central.pharmacy.domain.Pharmacy;
import central.pharmacy.domain.PharmacyId;
import central.pharmacy.application.PharmacyEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
//@JWT(validate = JWT.JwtMethodMode.BEARER_TOKEN)
@HttpEndpoint("/pharmacies")
public class PharmacyEndpoint extends AbstractHttpEndpoint {

    private final ComponentClient componentClient;

    private static final Logger logger = LoggerFactory.getLogger(PharmacyEndpoint.class);

    public PharmacyEndpoint(ComponentClient componentClient) {
        this.componentClient = componentClient;
    }

    @Get("/{pharmacy_id}")
    public Pharmacy get(String pharmacy_id) {
        logger.info("Get pharmacy with id={}", pharmacy_id);
        var record = componentClient
                .forEventSourcedEntity(pharmacy_id)
                .method(PharmacyEntity::get) // <1>
                .invoke();
        if (record.isPresent()) {
            return record.get();
        } else {
            throw HttpException.error(StatusCodes.NOT_FOUND, "No such Pharmacy");
        }
    }

    @Put("/pharmacy")
    public HttpResponse addRecord(Pharmacy pharmacy) {
        var id = new PharmacyId(pharmacy.pharmacyId()).toString();
        logger.info("Adding pharmacy with id={}", id);
        componentClient
                .forEventSourcedEntity(id)
                .method(PharmacyEntity::create)
                .invoke(pharmacy);
        return HttpResponses.ok();
    }


    @Delete("/{pharmacy_id}")
    public HttpResponse deleteRecord(String pharmacy_id) {
        logger.info("Deleting pharmacy with id={}", pharmacy_id);
        componentClient
                .forEventSourcedEntity(pharmacy_id)
                .method(PharmacyEntity::delete)
                .invoke();
        return HttpResponses.ok();
    }

    @Post("/pharmacy")
    public HttpResponse updateRecord(Pharmacy pharmacy) {
        var id = new PharmacyId(pharmacy.pharmacyId()).toString();
        logger.info("Updating pharmacy with id={}", id);
        componentClient
                .forEventSourcedEntity(id)
                .method(PharmacyEntity::update)
                .invoke(pharmacy);
        return HttpResponses.ok();
    }

}
