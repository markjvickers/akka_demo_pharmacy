package pharmacy.application.central.delivery;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.consumer.Consumer;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.http.StrictResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pharmacy.application.PatientRecordEntity;
import pharmacy.application.central.client.CentralClient;
import pharmacy.application.central.client.domain.StorePatientRecord;
import pharmacy.domain.PatientRecord;
import pharmacy.domain.PatientRecordEvent.PatientRecordCreated;
import pharmacy.domain.PatientRecordEvent.PatientRecordUpdated;
import pharmacy.domain.PatientRecordEvent.PatientRecordDeleted;
import pharmacy.domain.delivery.PatientRecordDelivery;

import java.util.Set;
import java.util.function.Supplier;

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
           requireDelivery();
           var ok = delivery.get();
           if (ok) {
               markAsDelivered();
               return effects().done();
           } else
               throw new RuntimeException("Delivery failed");
       }
    }

    private String getPatientId() {
        var cloudEvent = messageContext().metadata().asCloudEvent();
        var subject = cloudEvent.subject().get();
        return subject;
    }

    private String getUpdateId() {
        var cloudEvent = messageContext().metadata().asCloudEvent();
        var updateId = cloudEvent.sequenceString().get();
        return updateId;
    }

    private akka.Done requireDelivery() {
        logger.info("Requiring delivery for {}", getUpdateId());
        return componentClient
                .forEventSourcedEntity(getUpdateId())
                .method(PatientRecordDeliveryEntity::create)
                .invoke(getPatientId());
    }

    private akka.Done markAsDelivered() {
        logger.info("Marking as delivered for {}", getUpdateId());
        return componentClient
                .forEventSourcedEntity(getUpdateId())
                .method(PatientRecordDeliveryEntity::markAsDelivered)
                .invoke();
    }

    private boolean alreadyDelivered() {
        return componentClient
                .forEventSourcedEntity(getUpdateId())
                .method(PatientRecordDeliveryEntity::getState)
                .invoke()
                .delivered();
    }

    private boolean forwardCreate(PatientRecord record) {
        logger.info("forwarding create for patient record patientId={}, updateId={}", record.patientId(), getUpdateId());
        var result = centralClient.create(fromPatientRecord(record));
        return isValid(result, Set.of("200 OK", "400 Bad Request"));
    }

    private boolean forwardUpdate(PatientRecord record) {
        logger.info("forwarding update for patient record patientId={}, updateId={}", record.patientId(), getUpdateId());
        var result = centralClient.update(fromPatientRecord(record));
        return isValid(result, Set.of("200 OK"));
    }

    private boolean forwardDelete(String pharmacyId, String patientId) {
        logger.info("forwarding delete for patient record patientId={}, updateId={}", patientId, getUpdateId());
        var result = centralClient.delete(pharmacyId, patientId);
        return isValid(result, Set.of("200 OK"));
    }

    private boolean isValid(StrictResponse<?> response, Set<String> allowedStatuses) {
        var status = response.status();
        var allowed = allowedStatuses.contains(response.status().toString());
        logger.info("isValid check, status={}, allowed={}", status, allowed);
        return allowed;
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