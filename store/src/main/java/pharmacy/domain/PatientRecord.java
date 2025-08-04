package pharmacy.domain;

import java.util.Optional;

public record PatientRecord(
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

    // Java, you so tedious
    public PatientRecord withSmsOptInPref(boolean newPref) {
        return new PatientRecord(
                pharmacyId,
                patientId,
                firstName,
                lastName,
                prefName,
                dateOfBirth,
                phoneNumber,
                provHealthNumber,
                unitNumber,
                streetNumber,
                streetName,
                city,
                province,
                postalCode,
                country,
                langPref,
                newPref
        );
    }


}
