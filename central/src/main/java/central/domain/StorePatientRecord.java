package central.domain;

import java.util.Optional;

public record StorePatientRecord(
        String pharmacyId,
        String patientId,
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
) {

}
