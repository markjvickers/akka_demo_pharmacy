package central.patient.application;

import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;
import akka.javasdk.eventsourcedentity.EventSourcedEntityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import central.patient.domain.StorePatientRecord;
import central.patient.domain.StorePatientRecordEvent;

import java.util.Optional;

@ComponentId("store-patient-record")
public class StorePatientRecordEntity
        extends EventSourcedEntity<StorePatientRecord, StorePatientRecordEvent> {


    private final String entityId;

    private static final Logger logger = LoggerFactory.getLogger(StorePatientRecordEntity.class);

    public StorePatientRecordEntity(EventSourcedEntityContext context) {
        this.entityId = context.entityId();
    }

    @Override
    public StorePatientRecord emptyState() {
        return null;
    }

    public Effect<Done> create(StorePatientRecord patientRecord) {
        if (Optional.ofNullable(currentState()).isPresent()) {
            logger.info("StorePatientRecord id={} already exists.", entityId);
            return effects().error("StorePatientRecord already exists.");
        }
        if (isDeleted()) {
            logger.info("StorePatientRecord has been expunged, id={}", entityId);
            return effects().error("StorePatientRecord expunged.");
        }
        return effects()
                .persist(new StorePatientRecordEvent.StorePatientRecordCreated(patientRecord))
                .thenReply(newState -> Done.getInstance());
    }

    public ReadOnlyEffect<Optional<StorePatientRecord>> getRecord() {
        if (isDeleted()) {
            return effects().error("StorePatientRecord expunged.");
        }
        return effects().reply(Optional.ofNullable(currentState()));
    }

    public Effect<Done> update(StorePatientRecord patientRecord) {
        if (isDeleted()) {
            return effects().error("StorePatientRecord expunged.");
        }
        if(currentState() == null)
            return effects().error("StorePatientRecord not found.");
        return effects()
                        .persist(new StorePatientRecordEvent.StorePatientRecordUpdated(patientRecord))
                        .thenReply(newState -> Done.getInstance());
    }

    public Effect<Done> delete() {
        if(currentState() == null)
            return effects().error("StorePatientRecord not found");
        else if(isDeleted())
            return effects().error("StorePatientRecord expunged");
        else return effects()
                    .persist(new StorePatientRecordEvent.StorePatientRecordDeleted())
                    .deleteEntity()
                    .thenReply(newState -> Done.getInstance());
    }

    public StorePatientRecord applyEvent(StorePatientRecordEvent event) {
        return switch (event) {
            case StorePatientRecordEvent.StorePatientRecordCreated evt -> evt.patientRecord();
            case StorePatientRecordEvent.StorePatientRecordUpdated evt -> evt.patientRecord();
            case StorePatientRecordEvent.StorePatientRecordDeleted evt -> currentState();
        };
    }

}