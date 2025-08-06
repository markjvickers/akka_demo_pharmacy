package pharmacy.application.central.delivery;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.consumer.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pharmacy.application.PatientRecordEntity;
import pharmacy.domain.PatientRecordEvent.PatientRecordCreated;
import pharmacy.domain.PatientRecordEvent.PatientRecordUpdated;
import pharmacy.domain.PatientRecordEvent.PatientRecordDeleted;

/**
 * Uses a separate consumer to log delivery requirements to decouple from deliverer.
 * That way, we can still log new delivery requirements even as the sender mechanism is blocked.
 * This allows us to show increases in the 'required delivery' number even as the 'delivery completed' number is static.
 * This is purely for the sake of a demo.
 */
@ComponentId("patient-record-delivery-requirements")
@Consume.FromEventSourcedEntity(value = PatientRecordEntity.class, ignoreUnknown = true)
public class RequirePatientRecordDelivery extends Consumer {

    private static final Logger logger = LoggerFactory.getLogger(RequirePatientRecordDelivery.class);

    private final ComponentClient componentClient;

    public RequirePatientRecordDelivery(ComponentClient componentClient) {
        this.componentClient = componentClient;
    }

    public Consumer.Effect onCreate(PatientRecordCreated event) {
        return requireDelivery();
    }

    public Consumer.Effect onUpdate(PatientRecordUpdated event) {
        return requireDelivery();
    }

    public Consumer.Effect onDelete(PatientRecordDeleted ignore) {
        return requireDelivery();
    }

    private String getPatientId() {
        return UpdateIdUtility.getPatientId(messageContext());
    }

    private String getUpdateId() {
        return UpdateIdUtility.getUpdateId(messageContext());
    }

    private Effect requireDelivery() {
        logger.info("Requiring delivery for update={} on patientId={}", getUpdateId(), getPatientId());
        componentClient
                .forEventSourcedEntity(getUpdateId())
                .method(PatientRecordDeliveryEntity::create)
                .invoke(getPatientId());
        return effects().done();
    }

}
