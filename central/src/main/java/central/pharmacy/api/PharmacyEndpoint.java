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
    // SEARCH (query-parameter style)
    // Clients call e.g.:
    //   GET /pharmacies/search/city?city=St.%20John's
    //   GET /pharmacies/search/province?province=ON
    //   GET /pharmacies/search/location?city=Toronto&province=ON
    //   GET /pharmacies/search/location?province=ON&postalCode=M5H1J9
    //   GET /pharmacies/search/term?term=Main%20Street
    // ------------------------------------------------------------

    @Get("/search/city")
    public List<Pharmacy> searchByCity(String city) {
        if (isBlank(city)) {
            throw HttpException.error(StatusCodes.BAD_REQUEST, "Missing required query parameter: city");
        }
        logger.info("Searching pharmacies by city (query): {}", city);
        return componentClient
                .forView()
                .method(PharmacySearchView::searchByCity)
                .invoke(city)
                .pharmacies();
    }

    @Get("/search/province")
    public List<Pharmacy> searchByProvince(String province) {
        if (isBlank(province)) {
            throw HttpException.error(StatusCodes.BAD_REQUEST, "Missing required query parameter: province");
        }
        logger.info("Searching pharmacies by province (query): {}", province);
        return componentClient
                .forView()
                .method(PharmacySearchView::searchByProvince)
                .invoke(province)
                .pharmacies();
    }

    @Get("/search/postal-code")
    public List<Pharmacy> searchByPostalCode(String postalCode) {
        if (isBlank(postalCode)) {
            throw HttpException.error(StatusCodes.BAD_REQUEST, "Missing required query parameter: postalCode");
        }
        logger.info("Searching pharmacies by postal code (query): {}", postalCode);
        return componentClient
                .forView()
                .method(PharmacySearchView::searchByPostalCode)
                .invoke(postalCode)
                .pharmacies();
    }

    @Get("/search/phone")
    public List<Pharmacy> searchByPhoneNumber(String phoneNumber) {
        if (isBlank(phoneNumber)) {
            throw HttpException.error(StatusCodes.BAD_REQUEST, "Missing required query parameter: phoneNumber");
        }
        logger.info("Searching pharmacies by phone number (query): {}", phoneNumber);
        return componentClient
                .forView()
                .method(PharmacySearchView::searchByPhoneNumber)
                .invoke(phoneNumber)
                .pharmacies();
    }

    @Get("/search/address")
    public List<Pharmacy> searchByAddress(String streetAddress) {
        if (isBlank(streetAddress)) {
            throw HttpException.error(StatusCodes.BAD_REQUEST, "Missing required query parameter: streetAddress");
        }
        logger.info("Searching pharmacies by address (query, LIKE): {}", streetAddress);
        return componentClient
                .forView()
                .method(PharmacySearchView::searchByAddress)
                .invoke(streetAddress)
                .pharmacies();
    }

    @Get("/search/term")
    public List<Pharmacy> searchByTerm(String term) {
        if (isBlank(term)) {
            throw HttpException.error(StatusCodes.BAD_REQUEST, "Missing required query parameter: term");
        }
        logger.info("Searching pharmacies by free-text term (query, LIKEs): {}", term);
        return componentClient
                .forView()
                .method(PharmacySearchView::searchByTerm)
                .invoke(term)
                .pharmacies();
    }

    /**
     * Flexible location search using query params.
     * Supports any of:
     *   - city + province + postalCode  → searchByAllLocation
     *   - city + province               → searchByCityAndProvince
     *   - province + postalCode         → searchByProvinceAndPostalCode
     *   - city + postalCode             → searchByCityAndPostalCode
     *   - Only one of (city | province | postalCode) → routes to corresponding single-field search
     */
    @Get("/search/location")
    public List<Pharmacy> searchByLocation(String city, String province, String postalCode) {
        final boolean hasCity = notBlank(city);
        final boolean hasProvince = notBlank(province);
        final boolean hasPostal = notBlank(postalCode);

        if (!hasCity && !hasProvince && !hasPostal) {
            throw HttpException.error(
                    StatusCodes.BAD_REQUEST,
                    "Provide at least one of: city, province, postalCode"
            );
        }

        // All three
        if (hasCity && hasProvince && hasPostal) {
            logger.info("Location search: city={}, province={}, postalCode={} (ALL)", city, province, postalCode);
            return componentClient
                    .forView()
                    .method(PharmacySearchView::searchByAllLocation)
                    .invoke(new PharmacySearchView.AllLocationCriteria(city, province, postalCode))
                    .pharmacies();
        }

        // Two-field combos
        if (hasCity && hasProvince) {
            logger.info("Location search: city={}, province={} (CITY+PROVINCE)", city, province);
            return componentClient
                    .forView()
                    .method(PharmacySearchView::searchByCityAndProvince)
                    .invoke(new PharmacySearchView.CityAndProvinceCriteria(city, province))
                    .pharmacies();
        }
        if (hasProvince && hasPostal) {
            logger.info("Location search: province={}, postalCode={} (PROVINCE+POSTAL)", province, postalCode);
            return componentClient
                    .forView()
                    .method(PharmacySearchView::searchByProvinceAndPostalCode)
                    .invoke(new PharmacySearchView.ProvinceAndPostalCodeCriteria(province, postalCode))
                    .pharmacies();
        }
        if (hasCity && hasPostal) {
            logger.info("Location search: city={}, postalCode={} (CITY+POSTAL)", city, postalCode);
            return componentClient
                    .forView()
                    .method(PharmacySearchView::searchByCityAndPostalCode)
                    .invoke(new PharmacySearchView.CityAndPostalCodeCriteria(city, postalCode))
                    .pharmacies();
        }

        // Single-field fallbacks
        if (hasCity) {
            logger.info("Location search: city={} (single)", city);
            return componentClient
                    .forView()
                    .method(PharmacySearchView::searchByCity)
                    .invoke(city)
                    .pharmacies();
        }
        if (hasProvince) {
            logger.info("Location search: province={} (single)", province);
            return componentClient
                    .forView()
                    .method(PharmacySearchView::searchByProvince)
                    .invoke(province)
                    .pharmacies();
        }
        // hasPostal only
        logger.info("Location search: postalCode={} (single)", postalCode);
        return componentClient
                .forView()
                .method(PharmacySearchView::searchByPostalCode)
                .invoke(postalCode)
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
