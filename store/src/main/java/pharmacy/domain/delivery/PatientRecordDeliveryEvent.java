package pharmacy.domain.delivery;

import akka.javasdk.annotations.TypeName;

public sealed interface PatientRecordDeliveryEvent {

    @TypeName("patient-record-required")
    record PatientRecordRequired(String patientId) implements PatientRecordDeliveryEvent {}

    @TypeName("patient-record-delivered")
    record PatientRecordDelivered() implements PatientRecordDeliveryEvent {}

}
