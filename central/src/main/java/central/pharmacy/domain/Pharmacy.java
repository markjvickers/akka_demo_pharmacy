package central.pharmacy.domain;

public record Pharmacy(
        String pharmacyId,
        String address,
        String phoneNumber,
        String version
) {}