package pharmacy.application;

import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;
import akka.javasdk.eventsourcedentity.EventSourcedEntityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pharmacy.domain.PatientRecord;
import pharmacy.domain.PatientRecordEvent;

import java.util.ArrayList;
import java.util.Optional;

@ComponentId("patient-record")
public class PatientRecordEntity
        extends EventSourcedEntity<PatientRecord, PatientRecordEvent> {


    private final String entityId;

    private static final Logger logger = LoggerFactory.getLogger(PatientRecordEntity.class);

    public record PatientMergeRequest(
            PatientRecord updated,
            String mergedPatientId
    ) {}


    public PatientRecordEntity(EventSourcedEntityContext context) {
        this.entityId = context.entityId();
    }

    @Override
    public PatientRecord emptyState() {
        return null;
    }

    public Effect<Done> create(PatientRecord patientRecord) {
        if (Optional.ofNullable(currentState()).isPresent()) {
            logger.info("PatientRecord id={} already exists.", entityId);
            return effects().error("PatientRecord already exists.");
        }
        if (isDeleted()) {
            logger.info("PatientRecord has been expunged, id={}", entityId);
            return effects().error("PatientRecord expunged.");
        }
        return validate(patientRecord)
                .orElseGet(() -> {
                    var events = new ArrayList<PatientRecordEvent>();
                    events.add(new PatientRecordEvent.PatientRecordCreated(patientRecord));
                    if (patientRecord.smsOptInPref())
                        events.add(new PatientRecordEvent.PatientOptedInForSms());
                    return effects()
                            .persistAll(events)
                            .thenReply(newState -> Done.getInstance());
                }
        );
    }

    public ReadOnlyEffect<Optional<PatientRecord>> getRecord() {
        if (isDeleted()) {
            return effects().error("PatientRecord expunged.");
        }
        return effects().reply(Optional.ofNullable(currentState()));
    }

    public Effect<Done> update(PatientRecord patientRecord) {
        if (isDeleted()) {
            return effects().error("PatientRecord expunged.");
        }
        if(currentState() == null)
            return effects().error("PatientRecord not found.");
        return validate(patientRecord)
                .orElseGet(() -> {
                    var events = new ArrayList<PatientRecordEvent>();
                    events.add(new PatientRecordEvent.PatientRecordUpdated(patientRecord));
                    if(!currentState().smsOptInPref() && patientRecord.smsOptInPref())
                        events.add(new PatientRecordEvent.PatientOptedInForSms());
                    return effects()
                            .persistAll(events)
                            .thenReply(newState -> Done.getInstance());
                });
    }

    public Effect<Done> delete() {
        if(currentState() == null)
            return effects().error("PatientRecord not found");
        else if(isDeleted())
            return effects().error("PatientRecord expunged");
        else return effects()
                .persist(new PatientRecordEvent.PatientRecordDeleted(currentState().pharmacyId(), currentState().patientId()))
                .deleteEntity()
                .thenReply(newState -> Done.getInstance());
    }

    public Effect<Done> merge(PatientMergeRequest request) {
        if (isDeleted()) {
            return effects().error("PatientRecord expunged");
        }
        return validate(request.updated)
                .orElseGet(() -> {
                    var merged = new PatientRecordEvent.PatientRecordMerged(request.updated, request.mergedPatientId);
                    var updated = new PatientRecordEvent.PatientRecordUpdated(request.updated);
                    return effects()
                            .persist(merged, updated)
                            .thenReply(newState -> Done.getInstance());
                });
    }

    public PatientRecord applyEvent(PatientRecordEvent event) {
        return switch (event) {
            case PatientRecordEvent.PatientRecordCreated evt -> evt.patientRecord();
            case PatientRecordEvent.PatientRecordUpdated evt -> evt.patientRecord();
            case PatientRecordEvent.PatientRecordDeleted evt -> currentState();
            case PatientRecordEvent.PatientRecordMerged evt -> evt.updated();
            case PatientRecordEvent.PatientOptedInForSms evt -> currentState();
        };
    }

    private Optional<Effect<Done>> validate(PatientRecord patientRecord) {
        if (patientRecord.firstName().trim().isEmpty())
            return error("First name cannot be empty.");
        if (patientRecord.lastName().trim().isEmpty())
            return error("Last name cannot be empty.");
        if (patientRecord.phoneNumber().trim().isEmpty())
            return error("Phone number cannot be empty.");
        return Optional.empty();
    }

    private Optional<Effect<Done>> error(String msg) {
        return Optional.of(effects().error(msg));
    }

}