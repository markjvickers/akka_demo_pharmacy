package pharmacy.application.central.delivery;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.consumer.Consumer;
import akka.javasdk.http.StrictResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pharmacy.application.central.client.CentralClient;
import pharmacy.application.central.client.domain.StorePatientRecord;
import pharmacy.domain.PatientRecord;
import pharmacy.domain.PatientRecordEvent;
import pharmacy.domain.delivery.PatientRecordDeliveryEvent;

import java.util.Set;
import java.util.function.Supplier;

/**
 * Listens for patient record events, and begins the process for sending them.
 * Note: There's two steps here so that failures to deliver don't block subsequent delivery-requirement logging.
 */
//@ComponentId("patient-record-delivery-complete")
@Consume.FromEventSourcedEntity(value = PatientRecordDeliveryEntity.class, ignoreUnknown = true)
public class PatientRecordDeliveryComplete extends Consumer {

    private static final Logger logger = LoggerFactory.getLogger(PatientRecordDeliveryComplete.class);

    private final ComponentClient componentClient;
    private final CentralClient centralClient;

    public PatientRecordDeliveryComplete(ComponentClient componentClient, CentralClient centralClient) {
        this.componentClient = componentClient;
        this.centralClient = centralClient;
    }

    public Consumer.Effect onRequired(PatientRecordDeliveryEvent.PatientRecordRequired event) {
        return switch (event.updateType()) {
            case "Created" -> deliver(() -> forwardCreate(event.record().get()));
            case "Updated" -> deliver(() -> forwardUpdate(event.record().get()));
            case "Deleted" -> deliver(() -> forwardDelete(event.pharmacyId(), event.patientId()));
            default -> effects().done();
        };
    }

    /**
     * Common delivery algorithm for patient record CRUD.
     */
    private Consumer.Effect deliver(Supplier<Boolean> delivery) {
        var ok = delivery.get();
        if (ok) {
            markAsDelivered();
            return effects().done();
        } else
            throw new RuntimeException("Delivery failed");
    }

    //The entity id of the PatientRecordDelivery entity matches the sequence number of the PatientRecordEntity.
    //The problem here is that updates could get out of order. Not so good.
    private String getUpdateId() {
        return messageContext().eventSubject().get();
    }

    private akka.Done markAsDelivered() {
        logger.info("Marking as delivered for {}", getUpdateId());
        return componentClient
                .forEventSourcedEntity(getUpdateId())
                .method(PatientRecordDeliveryEntity::markAsDelivered)
                .invoke();
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
