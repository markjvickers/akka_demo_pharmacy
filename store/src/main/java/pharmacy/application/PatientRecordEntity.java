package pharmacy.application;

import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;
import akka.javasdk.eventsourcedentity.EventSourcedEntityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pharmacy.domain.PatientRecord;
import pharmacy.domain.PatientRecordEvent;

import java.util.Optional;

@ComponentId("patient-record")
public class PatientRecordEntity
        extends EventSourcedEntity<PatientRecord, PatientRecordEvent> {


    private final String entityId;

    private static final Logger logger = LoggerFactory.getLogger(PatientRecordEntity.class);

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
        return validate(patientRecord)
                .orElseGet(() ->
                    effects()
                            .persist(new PatientRecordEvent.PatientRecordCreated(patientRecord))
                            .thenReply(newState -> Done.getInstance())
        );
    }

    public ReadOnlyEffect<PatientRecord> getRecord() {
        return effects().reply(currentState());
    }

    public Effect<Done> update(PatientRecord patientRecord) {
        return validate(patientRecord)
                .orElseGet(() ->
                        effects()
                                .persist(new PatientRecordEvent.PatientRecordUpdated(patientRecord))
                                .thenReply(newState -> Done.getInstance())
                );
    }

    public Effect<Done> delete() {
        return currentState() == null ?
            effects().error("PatientRecord not found") :
            effects()
                .persist(new PatientRecordEvent.PatientRecordDeleted())
                .deleteEntity()
                .thenReply(newState -> Done.getInstance());
    }

    public Effect<Done> merge(PatientRecord patientRecord, String mergeWithPatientId) {
        return validate(patientRecord)
                .orElseGet(() -> {
                    var merged = new PatientRecordEvent.PatientRecordMerged(patientRecord, mergeWithPatientId);
                    var updated = new PatientRecordEvent.PatientRecordUpdated(patientRecord);
                    return effects()
                            .persist(merged, updated)
                            .thenReply(newState -> Done.getInstance());
                });
    }

    public Effect<Done> withSMSOptIn(Boolean smsOptIn) {
        return effects()
                .persist(new PatientRecordEvent.PatientOptedInForSms(smsOptIn))
                .thenReply(newState -> Done.getInstance());
    }

    public PatientRecord applyEvent(PatientRecordEvent event) {
        return switch (event) {
            case PatientRecordEvent.PatientRecordCreated evt -> evt.patientRecord();
            case PatientRecordEvent.PatientRecordUpdated evt -> evt.patientRecord();
            case PatientRecordEvent.PatientRecordDeleted evt -> null;
            case PatientRecordEvent.PatientRecordMerged evt -> evt.updated();
            case PatientRecordEvent.PatientOptedInForSms evt -> currentState().withSmsOptInPref(evt.smsOptIn());
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