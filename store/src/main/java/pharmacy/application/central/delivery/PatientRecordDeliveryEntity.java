package pharmacy.application.central.delivery;

import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;
import akka.javasdk.eventsourcedentity.EventSourcedEntityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pharmacy.domain.PatientRecord;
import pharmacy.domain.PatientRecordEvent;
import pharmacy.domain.delivery.PatientRecordDelivery;
import pharmacy.domain.delivery.PatientRecordDeliveryEvent;

import java.util.Optional;

/**
 * A record of deliveries.
 * For producing a view from which we can reify/visualize the number of delivered/undelivered messages.
 */
@ComponentId("patient-record-delivery")
public class PatientRecordDeliveryEntity
        extends EventSourcedEntity<PatientRecordDelivery, PatientRecordDeliveryEvent> {


    private final String entityId;

    private static final Logger logger = LoggerFactory.getLogger(PatientRecordDeliveryEntity.class);

    public PatientRecordDeliveryEntity(EventSourcedEntityContext context) {
        this.entityId = context.entityId();
    }

    public record PatientRecordDeliveryRequest(String updateType, Optional<PatientRecord> record, String pharmacyId, String patientRecordId) {}

    @Override
    public PatientRecordDelivery emptyState() {
        return new PatientRecordDelivery("", false);
    }

    public Effect<Done> create(String patientId) {
        if(currentState().isDefined()) {
            logger.info("PatientRecordDelivery already created, id={}", entityId);
            return effects().reply(Done.getInstance());
         }
        if(currentState().delivered())
            return alreadyDelivered();

        logger.info("PatientRecordDelivery required, id={}", entityId);
        return effects()
                .persist(new PatientRecordDeliveryEvent.PatientRecordRequired(patientId))
                .thenReply(s -> Done.done());
    }

    public Effect<Done> markAsDelivered() {
        if(!currentState().isDefined()) {
            logger.info("PatientRecordDelivery does not exist, id={}", entityId);
            return effects().reply(Done.getInstance());
        }
        if(currentState().delivered())
            return alreadyDelivered();
        logger.info("PatientRecordDelivery marking as delivered, id={}", entityId);
        return effects()
                .persist(new PatientRecordDeliveryEvent.PatientRecordDelivered())
                .thenReply(s -> Done.done());
    }

    private Effect<Done> alreadyDelivered() {
        logger.info("PatientRecordDelivery has been already been delivered, id={}", entityId);
        return effects().reply(Done.getInstance());
    }

    public ReadOnlyEffect<PatientRecordDelivery> getState() {
        return effects().reply(currentState());
    }

    public PatientRecordDelivery applyEvent(PatientRecordDeliveryEvent event) {
        return switch (event) {
            case PatientRecordDeliveryEvent.PatientRecordRequired evt -> new PatientRecordDelivery(evt.patientId(),false);
            case PatientRecordDeliveryEvent.PatientRecordDelivered ignore -> currentState().withDelivery();
        };
    }

}