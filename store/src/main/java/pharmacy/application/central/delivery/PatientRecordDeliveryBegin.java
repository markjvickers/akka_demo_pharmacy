package pharmacy.application.central.delivery;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.consumer.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pharmacy.application.PatientRecordEntity;
import pharmacy.domain.PatientRecord;
import pharmacy.domain.PatientRecordEvent.PatientRecordCreated;
import pharmacy.domain.PatientRecordEvent.PatientRecordUpdated;
import pharmacy.domain.PatientRecordEvent.PatientRecordDeleted;

import java.util.Optional;

/**
 * Listens for patient record events, and begins the process for sending them.
 * Note: There's two steps here so that failures to deliver don't block subsequent delivery-requirement logging.
 */
//@ComponentId("patient-record-delivery-begin")
@Consume.FromEventSourcedEntity(value = PatientRecordEntity.class, ignoreUnknown = true)
public class PatientRecordDeliveryBegin extends Consumer {

    private static final Logger logger = LoggerFactory.getLogger(PatientRecordDeliveryBegin.class);

    private final ComponentClient componentClient;

    public PatientRecordDeliveryBegin(ComponentClient componentClient) {
        this.componentClient = componentClient;
    }

    public Effect onCreate(PatientRecordCreated event) {
        return beginDelivery("Created", Optional.of(event.patientRecord()));
    }

    public Effect onUpdate(PatientRecordUpdated event) {
        return beginDelivery("Updated", Optional.of(event.patientRecord()));
    }

    public Effect onDelete(PatientRecordDeleted ignore) {
        return beginDelivery("Deleted", Optional.empty());
    }

    /**
     * Common delivery algorithm for patient record CRUD.
     */
    private Consumer.Effect beginDelivery(String updateType, Optional<PatientRecord> record) {
        if (alreadyDelivered()) {
            logger.info("Already delivered {}, moving on", getUpdateId());
            return effects().done();
        } else {
            requireDelivery(updateType, record);
            return effects().done();
        }
    }

    private String pharmacyId() {
        return "101";
    }

    private String getPatientId() {
        var cloudEvent = messageContext().metadata().asCloudEvent();
        return cloudEvent.subject().get();
    }

    private String getUpdateId() {
        var cloudEvent = messageContext().metadata().asCloudEvent();
        return cloudEvent.sequenceString().get();
    }

    private akka.Done requireDelivery(String updateType, Optional<PatientRecord> record) {
        logger.info("Requiring delivery for {}", getUpdateId());
        return componentClient
                .forEventSourcedEntity(getUpdateId())
                .method(PatientRecordDeliveryEntity::create)
                .invoke(new PatientRecordDeliveryEntity.PatientRecordDeliveryRequest(updateType, record, pharmacyId(), getPatientId()));
    }

    private boolean alreadyDelivered() {
        return componentClient
                .forEventSourcedEntity(getUpdateId())
                .method(PatientRecordDeliveryEntity::getState)
                .invoke()
                .delivered();
    }

}
