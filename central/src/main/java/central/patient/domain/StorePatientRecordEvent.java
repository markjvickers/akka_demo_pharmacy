package central.patient.domain;

import akka.javasdk.annotations.TypeName;

public sealed interface StorePatientRecordEvent {

    @TypeName("store-patient-record-created")
    record StorePatientRecordCreated(StorePatientRecord patientRecord) implements StorePatientRecordEvent {}

    @TypeName("store-patient-record-updated")
    record StorePatientRecordUpdated(StorePatientRecord patientRecord) implements StorePatientRecordEvent {}

    @TypeName("store-patient-record-deleted")
    record StorePatientRecordDeleted() implements StorePatientRecordEvent {}

}