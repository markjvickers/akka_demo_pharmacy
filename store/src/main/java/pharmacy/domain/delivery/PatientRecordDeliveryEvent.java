package pharmacy.domain.delivery;

import akka.javasdk.annotations.TypeName;
import pharmacy.domain.PatientRecord;

import java.util.Optional;

public sealed interface PatientRecordDeliveryEvent {

    @TypeName("patient-record-required")
    record PatientRecordRequired(String updateType, Optional<PatientRecord> record, String pharmacyId, String patientId) implements PatientRecordDeliveryEvent {}

    @TypeName("patient-record-delivered")
    record PatientRecordDelivered() implements PatientRecordDeliveryEvent {}

}
