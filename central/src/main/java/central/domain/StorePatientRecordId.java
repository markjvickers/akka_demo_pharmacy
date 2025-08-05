package central.domain;

public record StorePatientRecordId(String pharmacyId, String patientId) {

    public static StorePatientRecordId fromRecord(StorePatientRecord record) {
        return new StorePatientRecordId(record.pharmacyId(), record.patientId());
    }

    @Override
    public String toString() {
        return pharmacyId + "-" + patientId;
    }

}
