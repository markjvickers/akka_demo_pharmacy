package pharmacy.api;

import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.HttpEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pharmacy.domain.PharmacyId;

@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
@HttpEndpoint("/pharmacy")
public class PharmacyEndpoint {

    private final PharmacyId pharmacyId;

    private static final Logger logger = LoggerFactory.getLogger(
        PharmacyEndpoint.class
    );

    public PharmacyEndpoint(PharmacyId pharmacyId) {
        this.pharmacyId = pharmacyId;
    }

    public record PharmacyInfo(
        String pharmacyId,
        String storeNumber,
        String displayName
    ) {}

    @Get("/info")
    public PharmacyInfo getPharmacyInfo() {
        logger.info("Get pharmacy info for pharmacy id={}", pharmacyId.id());
        
        String storeNumber = pharmacyId.id();
        String displayName = "Pharmacy Store #" + storeNumber;
        
        return new PharmacyInfo(
            pharmacyId.id(),
            storeNumber,
            displayName
        );
    }

    @Get("/id")
    public String getPharmacyId() {
        logger.info("Get pharmacy id={}", pharmacyId.id());
        return pharmacyId.id();
    }
} 