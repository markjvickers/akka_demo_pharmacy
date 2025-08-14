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
import central.pharmacy.application.PharmacyEntity;
import central.pharmacy.application.PharmacySearchView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

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
    public HttpResponse add(Pharmacy pharmacy) {
        var id = pharmacy.pharmacyId();
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
        var id = pharmacy.pharmacyId();
        logger.info("Updating pharmacy with id={}", id);
        componentClient
                .forEventSourcedEntity(id)
                .method(PharmacyEntity::update)
                .invoke(pharmacy);
        return HttpResponses.ok();
    }

    // Search endpoints
    @Get("/search/city/{city}")
    public List<Pharmacy> searchByCity(String city) {
        logger.info("Searching pharmacies by city: {}", city);
        return componentClient
                .forView()
                .method(PharmacySearchView::searchByCity)
                .invoke(city)
                .pharmacies();
    }

    @Get("/search/province/{province}")
    public List<Pharmacy> searchByProvince(String province) {
        logger.info("Searching pharmacies by province: {}", province);
        return componentClient
                .forView()
                .method(PharmacySearchView::searchByProvince)
                .invoke(province)
                .pharmacies();
    }

    @Get("/search/postal-code/{postalCode}")
    public List<Pharmacy> searchByPostalCode(String postalCode) {
        logger.info("Searching pharmacies by postal code: {}", postalCode);
        return componentClient
                .forView()
                .method(PharmacySearchView::searchByPostalCode)
                .invoke(postalCode)
                .pharmacies();
    }

    @Get("/search/phone/{phoneNumber}")
    public List<Pharmacy> searchByPhoneNumber(String phoneNumber) {
        logger.info("Searching pharmacies by phone number: {}", phoneNumber);
        return componentClient
                .forView()
                .method(PharmacySearchView::searchByPhoneNumber)
                .invoke(phoneNumber)
                .pharmacies();
    }

    @Get("/search/address/{streetAddress}")
    public List<Pharmacy> searchByAddress(String streetAddress) {
        logger.info("Searching pharmacies by address: {}", streetAddress);
        return componentClient
                .forView()
                .method(PharmacySearchView::searchByAddress)
                .invoke(streetAddress)
                .pharmacies();
    }

    @Get("/search/term/{searchTerm}")
    public List<Pharmacy> searchByTerm(String searchTerm) {
        logger.info("Searching pharmacies by term: {}", searchTerm);
        return componentClient
                .forView()
                .method(PharmacySearchView::searchByTerm)
                .invoke(searchTerm)
                .pharmacies();
    }

    @Post("/search/city-province")
    public List<Pharmacy> searchByCityAndProvince(CityAndProvinceRequest request) {
        logger.info("Searching pharmacies by city: {} and province: {}", 
                   request.city(), request.province());
        return componentClient
                .forView()
                .method(PharmacySearchView::searchByCityAndProvince)
                .invoke(new PharmacySearchView.CityAndProvinceCriteria(request.city(), request.province()))
                .pharmacies();
    }

    @Post("/search/province-postal")
    public List<Pharmacy> searchByProvinceAndPostalCode(ProvinceAndPostalRequest request) {
        logger.info("Searching pharmacies by province: {} and postal code: {}", 
                   request.province(), request.postalCode());
        return componentClient
                .forView()
                .method(PharmacySearchView::searchByProvinceAndPostalCode)
                .invoke(new PharmacySearchView.ProvinceAndPostalCodeCriteria(request.province(), request.postalCode()))
                .pharmacies();
    }

    @Post("/search/city-postal")
    public List<Pharmacy> searchByCityAndPostalCode(CityAndPostalRequest request) {
        logger.info("Searching pharmacies by city: {} and postal code: {}", 
                   request.city(), request.postalCode());
        return componentClient
                .forView()
                .method(PharmacySearchView::searchByCityAndPostalCode)
                .invoke(new PharmacySearchView.CityAndPostalCodeCriteria(request.city(), request.postalCode()))
                .pharmacies();
    }

    @Post("/search/location")
    public List<Pharmacy> searchByAllLocation(AllLocationRequest request) {
        logger.info("Searching pharmacies by city: {}, province: {} and postal code: {}", 
                   request.city(), request.province(), request.postalCode());
        return componentClient
                .forView()
                .method(PharmacySearchView::searchByAllLocation)
                .invoke(new PharmacySearchView.AllLocationCriteria(request.city(), request.province(), request.postalCode()))
                .pharmacies();
    }

    // Request record classes for POST endpoints
    public record CityAndProvinceRequest(String city, String province) {}
    public record ProvinceAndPostalRequest(String province, String postalCode) {}
    public record CityAndPostalRequest(String city, String postalCode) {}
    public record AllLocationRequest(String city, String province, String postalCode) {}

}
