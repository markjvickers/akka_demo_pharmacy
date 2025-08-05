package pharmacy.domain.delivery;

import akka.javasdk.annotations.TypeName;

public sealed interface PatientRecordDeliveryEvent {

    @TypeName("patient-record-delivered")
    record PatientRecordDelivered(PatientRecordDelivery patientRecordDelivery) implements PatientRecordDeliveryEvent {}

}
