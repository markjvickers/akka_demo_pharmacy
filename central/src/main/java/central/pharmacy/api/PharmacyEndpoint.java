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

    // ------------------------------------------------------------
    // CRUD by ID (IDs are good candidates for path segments)
    // ------------------------------------------------------------

    @Get("/{pharmacy_id}")
    public Pharmacy get(String pharmacy_id) {
        logger.info("Get pharmacy with id={}", pharmacy_id);
        var record = componentClient
                .forEventSourcedEntity(pharmacy_id)
                .method(PharmacyEntity::get)
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

    @Delete("/{pharmacy_id}")
    public HttpResponse deleteRecord(String pharmacy_id) {
        logger.info("Deleting pharmacy with id={}", pharmacy_id);
        componentClient
                .forEventSourcedEntity(pharmacy_id)
                .method(PharmacyEntity::delete)
                .invoke();
        return HttpResponses.ok();
    }

    // ------------------------------------------------------------
    // SEARCH (path-parameter style per Akka SDK validation rules)
    // ------------------------------------------------------------

    @Get("/search/city/{city}")
    public List<Pharmacy> searchByCity(String city) {
        if (isBlank(city)) {
            throw HttpException.error(StatusCodes.BAD_REQUEST, "Missing required path parameter: city");
        }
        logger.info("Searching pharmacies by city: {}", city);
        return componentClient
                .forView()
                .method(PharmacySearchView::searchByCity)
                .invoke(city)
                .pharmacies();
    }

    @Get("/search/province/{province}")
    public List<Pharmacy> searchByProvince(String province) {
        if (isBlank(province)) {
            throw HttpException.error(StatusCodes.BAD_REQUEST, "Missing required path parameter: province");
        }
        logger.info("Searching pharmacies by province: {}", province);
        return componentClient
                .forView()
                .method(PharmacySearchView::searchByProvince)
                .invoke(province)
                .pharmacies();
    }

    @Get("/search/postal-code/{postalCode}")
    public List<Pharmacy> searchByPostalCode(String postalCode) {
        if (isBlank(postalCode)) {
            throw HttpException.error(StatusCodes.BAD_REQUEST, "Missing required path parameter: postalCode");
        }
        logger.info("Searching pharmacies by postal code: {}", postalCode);
        return componentClient
                .forView()
                .method(PharmacySearchView::searchByPostalCode)
                .invoke(postalCode)
                .pharmacies();
    }

    @Get("/search/phone/{phoneNumber}")
    public List<Pharmacy> searchByPhoneNumber(String phoneNumber) {
        if (isBlank(phoneNumber)) {
            throw HttpException.error(StatusCodes.BAD_REQUEST, "Missing required path parameter: phoneNumber");
        }
        logger.info("Searching pharmacies by phone number: {}", phoneNumber);
        return componentClient
                .forView()
                .method(PharmacySearchView::searchByPhoneNumber)
                .invoke(phoneNumber)
                .pharmacies();
    }

    @Get("/search/address/{streetAddress}")
    public List<Pharmacy> searchByAddress(String streetAddress) {
        if (isBlank(streetAddress)) {
            throw HttpException.error(StatusCodes.BAD_REQUEST, "Missing required path parameter: streetAddress");
        }
        logger.info("Searching pharmacies by address (LIKE): {}", streetAddress);
        return componentClient
                .forView()
                .method(PharmacySearchView::searchByAddress)
                .invoke(streetAddress)
                .pharmacies();
    }

    @Get("/search/term/{term}")
    public List<Pharmacy> searchByTerm(String term) {
        if (isBlank(term)) {
            throw HttpException.error(StatusCodes.BAD_REQUEST, "Missing required path parameter: term");
        }
        logger.info("Searching pharmacies by free-text term (LIKEs): {}", term);
        return componentClient
                .forView()
                .method(PharmacySearchView::searchByTerm)
                .invoke(term)
                .pharmacies();
    }

    // Explicit location combinations
    @Get("/search/location/city-province/{city}/{province}")
    public List<Pharmacy> searchByCityAndProvince(String city, String province) {
        if (isBlank(city) || isBlank(province)) {
            throw HttpException.error(StatusCodes.BAD_REQUEST, "Both city and province are required");
        }
        logger.info("Location search (CITY+PROVINCE): city={}, province={}", city, province);
        return componentClient
                .forView()
                .method(PharmacySearchView::searchByCityAndProvince)
                .invoke(new PharmacySearchView.CityAndProvinceCriteria(city, province))
                .pharmacies();
    }

    @Get("/search/location/province-postal/{province}/{postalCode}")
    public List<Pharmacy> searchByProvinceAndPostalCode(String province, String postalCode) {
        if (isBlank(province) || isBlank(postalCode)) {
            throw HttpException.error(StatusCodes.BAD_REQUEST, "Both province and postalCode are required");
        }
        logger.info("Location search (PROVINCE+POSTAL): province={}, postalCode={}", province, postalCode);
        return componentClient
                .forView()
                .method(PharmacySearchView::searchByProvinceAndPostalCode)
                .invoke(new PharmacySearchView.ProvinceAndPostalCodeCriteria(province, postalCode))
                .pharmacies();
    }

    @Get("/search/location/city-postal/{city}/{postalCode}")
    public List<Pharmacy> searchByCityAndPostalCode(String city, String postalCode) {
        if (isBlank(city) || isBlank(postalCode)) {
            throw HttpException.error(StatusCodes.BAD_REQUEST, "Both city and postalCode are required");
        }
        logger.info("Location search (CITY+POSTAL): city={}, postalCode={}", city, postalCode);
        return componentClient
                .forView()
                .method(PharmacySearchView::searchByCityAndPostalCode)
                .invoke(new PharmacySearchView.CityAndPostalCodeCriteria(city, postalCode))
                .pharmacies();
    }

    // ------------------------------------------------------------
    // Utilities
    // ------------------------------------------------------------

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static boolean notBlank(String s) {
        return !isBlank(s);
    }
}
