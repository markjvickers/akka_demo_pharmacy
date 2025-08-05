package pharmacy.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.consumer.Consumer;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.http.StrictResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pharmacy.application.central.CentralClient;
import pharmacy.application.central.domain.StorePatientRecord;
import pharmacy.domain.PatientRecord;
import pharmacy.domain.PatientRecordEvent;
import pharmacy.domain.PatientRecordEvent.PatientRecordCreated;
import pharmacy.domain.PatientRecordEvent.PatientRecordUpdated;
import pharmacy.domain.PatientRecordEvent.PatientRecordDeleted;
import pharmacy.domain.delivery.PatientRecordDelivery;
import akka.javasdk.http.StrictResponse;
import pharmacy.application.central.domain.StorePatientRecord;
import pharmacy.domain.PatientRecord;
import pharmacy.domain.PatientRecordEvent;
import pharmacy.domain.PatientRecordEvent.*;
import java.util.Set;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import akka.javasdk.client.ComponentClient;
import pharmacy.application.central.CentralClient;

/**
 * Listens for patient record events, delivers them to central, and records the outcome.
 */
@ComponentId("patient-record-deliverer")
@Consume.FromEventSourcedEntity(value = PatientRecordEntity.class, ignoreUnknown = true)
public class PatientRecordDeliverer extends Consumer {

    private static final Logger logger = LoggerFactory.getLogger(PatientRecordDeliverer.class);

    private final ComponentClient componentClient;
    private final CentralClient centralClient;

    public PatientRecordDeliverer(ComponentClient componentClient, CentralClient centralClient) {
        this.componentClient = componentClient;
        this.centralClient = centralClient;
    }

    public Effect onCreate(PatientRecordCreated event) {
        return deliver(() -> forwardCreate(event.patientRecord()));
    }

    public Effect onUpdate(PatientRecordUpdated event) {
        return deliver(() -> forwardUpdate(event.patientRecord()));
    }

    public Effect onDelete(PatientRecordDeleted event) {
        return deliver(() -> forwardDelete(event.pharmacyId(), event.patientId()));
    }

    /**
     * Common delivery algorithm for patient record CRUD.
     */
    private Effect deliver(Supplier<Boolean> delivery) {
       if (alreadyDelivered()) {
           logger.info("Already delivered {}, moving on", getUpdateId());
           return effects().done();
       } else {
           var ok = delivery.get();
           if (ok)
               return effects().done();
           else
               throw new RuntimeException("Delivery failed");
       }
    }

    private String getUpdateId() {
        return messageContext().eventSubject().get();
    }

    private boolean alreadyDelivered() {
        return componentClient
                .forEventSourcedEntity(getUpdateId())
                .method(PatientRecordDeliveryEntity::getState)
                .invoke()
                .delivered();
    }

    private boolean forwardCreate(PatientRecord record) {
        var result = centralClient.create(fromPatientRecord(record));
        return isValid(result, Set.of("200 OK", "400 Bad Request"));
    }

    private boolean forwardUpdate(PatientRecord record) {
        var result = centralClient.update(fromPatientRecord(record));
        return isValid(result, Set.of("200 OK"));
    }

    private boolean forwardDelete(String pharmacyId, String patientId) {
        var result = centralClient.delete(pharmacyId, patientId);
        return isValid(result, Set.of("200 OK"));
    }

    private boolean isValid(StrictResponse<?> response, Set<String> allowedStatuses) {
        return allowedStatuses.contains(response.status().toString());
    }

    private StorePatientRecord fromPatientRecord(PatientRecord pr) {
        return new StorePatientRecord(
                pr.pharmacyId(),
                pr.patientId(),
                pr.firstName(),
                pr.lastName(),
                pr.prefName(),
                pr.dateOfBirth(),
                pr.phoneNumber(),
                pr.provHealthNumber(),
                pr.unitNumber(),
                pr.streetNumber(),
                pr.streetName(),
                pr.city(),
                pr.province(),
                pr.postalCode(),
                pr.country(),
                pr.langPref(),
                pr.smsOptInPref()
        );
    }

}