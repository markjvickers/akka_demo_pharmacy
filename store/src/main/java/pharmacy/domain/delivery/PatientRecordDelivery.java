package pharmacy.domain.delivery;

import pharmacy.domain.PatientRecordEvent;

public record PatientRecordDelivery(String updateId, PatientRecordEvent event, Boolean delivered) {
}
