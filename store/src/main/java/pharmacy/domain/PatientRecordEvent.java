package pharmacy.domain;

import akka.javasdk.annotations.TypeName;

public sealed interface PatientRecordEvent {

    @TypeName("patient-record-created")
    record PatientRecordCreated(PatientRecord patientRecord) implements PatientRecordEvent {}

    @TypeName("patient-record-updated")
    record PatientRecordUpdated(PatientRecord patientRecord) implements PatientRecordEvent {}

    @TypeName("patient-record-deleted")
    record PatientRecordDeleted() implements PatientRecordEvent {}

    @TypeName("patient-record-merged")
    record PatientRecordMerged(PatientRecord updated, String mergedWithPatientId) implements PatientRecordEvent {}

    @TypeName("patient-opted-in-for-sms")
    record PatientOptedInForSms(PatientRecord patientRecord) implements PatientRecordEvent {}

}