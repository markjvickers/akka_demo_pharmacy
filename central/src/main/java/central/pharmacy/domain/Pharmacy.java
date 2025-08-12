package central.pharmacy.domain;

public record Pharmacy(
        String pharmacyId,
        String streetAddress,
        String city,
        String province,
        String postalCode,
        String phoneNumber,
        String version
) {}
