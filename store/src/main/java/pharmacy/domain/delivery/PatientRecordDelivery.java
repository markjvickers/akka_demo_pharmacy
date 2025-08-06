package pharmacy.domain.delivery;

public record PatientRecordDelivery(String patientId, boolean delivered) {

    public PatientRecordDelivery withDelivery() {
        return new PatientRecordDelivery(patientId, true);
    }

}
