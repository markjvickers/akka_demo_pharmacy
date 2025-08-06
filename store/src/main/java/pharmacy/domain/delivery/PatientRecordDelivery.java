package pharmacy.domain.delivery;

import pharmacy.domain.PatientRecord;
import pharmacy.domain.PatientRecordEvent;

import java.util.Optional;

public record PatientRecordDelivery(String updateType, Optional<PatientRecord> patientRecord, String pharmacyId, String patientId, boolean delivered) {

    public PatientRecordDelivery withDelivery() {
        return new PatientRecordDelivery(updateType, patientRecord, pharmacyId, patientId,true);
    }

    public boolean isDefined() {
        return !updateType.isEmpty();
    }

}
