package pharmacy.application;

import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;
import akka.javasdk.eventsourcedentity.EventSourcedEntityContext;
import akka.javasdk.http.StrictResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pharmacy.application.central.CentralClient;
import pharmacy.application.central.domain.StorePatientRecord;
import pharmacy.domain.PatientRecord;
import pharmacy.domain.PatientRecordEvent;
import pharmacy.domain.delivery.PatientRecordDelivery;
import pharmacy.domain.delivery.PatientRecordDeliveryEvent;
import akka.javasdk.client.ComponentClient;
import pharmacy.domain.PatientRecordEvent.*;
import java.util.Set;
import java.util.Optional;

@ComponentId("patient-record-delivery")
public class PatientRecordDeliveryEntity
        extends EventSourcedEntity<PatientRecordDelivery, PatientRecordDeliveryEvent> {


    private final String entityId;
    private final ComponentClient componentClient;
    private final CentralClient centralClient;

    private static final Logger logger = LoggerFactory.getLogger(PatientRecordDeliveryEntity.class);


    public PatientRecordDeliveryEntity(EventSourcedEntityContext context, ComponentClient componentClient, CentralClient centralClient) {
        this.entityId = context.entityId();
        this.componentClient = componentClient;
        this.centralClient = centralClient;
    }

    @Override
    public PatientRecordDelivery emptyState() {
        return null;
    }

    public Effect<Boolean> create(PatientRecordDelivery delivery) {
        if(currentState().delivered()) {
            logger.info("PatientRecordDelivery has been already been delivered, id={}", entityId);
            return effects().reply(true);
        }
        var deliveryResult = deliver(delivery);
        var updatedDelivery = new PatientRecordDelivery(
                delivery.updateId(),
                delivery.event(),
                deliveryResult
        );
        return effects()
                .persist(new PatientRecordDeliveryEvent.PatientRecordDelivered(updatedDelivery))
                .thenReply(s -> deliveryResult);
    }

    public ReadOnlyEffect<Optional<PatientRecordDelivery>> getRecord() {
        if (isDeleted()) {
            return effects().error("PatientRecordDelivery expunged.");
        }
        return effects().reply(Optional.ofNullable(currentState()));
    }

    public PatientRecordDelivery applyEvent(PatientRecordDeliveryEvent event) {
        return switch (event) {
            case PatientRecordDeliveryEvent.PatientRecordDelivered evt -> evt.patientRecordDelivery();
        };
    }

    private boolean deliver(PatientRecordDelivery delivery) {
        return switch (delivery.event()) {
           case PatientRecordCreated e -> forwardCreate(e.patientRecord());
           case PatientRecordUpdated e -> forwardUpdate(e.patientRecord());
           case PatientRecordDeleted e -> forwardDelete(e.pharmacyId(), e.patientId());
           default -> true;
        };
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