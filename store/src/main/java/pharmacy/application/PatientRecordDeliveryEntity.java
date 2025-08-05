package pharmacy.application;

import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;
import akka.javasdk.eventsourcedentity.EventSourcedEntityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pharmacy.domain.delivery.PatientRecordDelivery;
import pharmacy.domain.delivery.PatientRecordDeliveryEvent;

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

    @Override
    public PatientRecordDelivery emptyState() {
        return new PatientRecordDelivery(false);
    }

    public Effect<Done> create(PatientRecordDelivery delivery) {
        if(currentState().delivered()) {
            logger.info("PatientRecordDelivery has been already been delivered, id={}", entityId);
            return effects().reply(Done.getInstance());
        }
        return effects()
                .persist(new PatientRecordDeliveryEvent.PatientRecordDelivered(delivery))
                .thenReply(s -> Done.done());
    }

    public ReadOnlyEffect<PatientRecordDelivery> getState() {
        return effects().reply(currentState());
    }

    public PatientRecordDelivery applyEvent(PatientRecordDeliveryEvent event) {
        return switch (event) {
            case PatientRecordDeliveryEvent.PatientRecordDelivered evt -> evt.patientRecordDelivery();
        };
    }

}