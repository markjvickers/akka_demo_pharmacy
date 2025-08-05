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

// tag::top[]

@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
//@JWT(validate = JWT.JwtMethodMode.BEARER_TOKEN)
@HttpEndpoint("/patients")
public class StorePatientRecordEndpoint extends AbstractHttpEndpoint {

    private final ComponentClient componentClient;

    private static final Logger logger = LoggerFactory.getLogger(StorePatientRecordEndpoint.class);

    public StorePatientRecordEndpoint(ComponentClient componentClient) {
        this.componentClient = componentClient;
    }

//    @Get("/{cartId}")
//    public ShoppingCartView.Cart get(String cartId) {
//        logger.info("Get cart id={}", cartId);
//
//        var userId = requestContext().getJwtClaims().subject().get();
//
//        var cart = componentClient
//                .forView()
//                .method(ShoppingCartView::getCart) // <1>
//                .invoke(cartId);
//
//        if (cart.userId().trim().equals(userId)) {
//            return cart;
//        } else {
//            throw HttpException.error(StatusCodes.NOT_FOUND, "no such cart");
//        }
//    }

    @Put("/patient")
    public HttpResponse addRecord(StorePatientRecord record) {
        var id = StorePatientRecordId.fromRecord(record).toString();
        logger.info("Adding patient record with id={}, patient_id=", id);
        componentClient
                .forEventSourcedEntity(id)
                .method(StorePatientRecordEntity::create)
                .invoke(record);
        return HttpResponses.ok();
    }


    @Delete("/patient")
    public HttpResponse deleteRecord(StorePatientRecordId id) {
        logger.info("Deleting patient record with id={}", id);
        componentClient
                .forEventSourcedEntity(id.toString())
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
