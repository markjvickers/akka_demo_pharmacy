package central.pharmacy.application;

import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;
import akka.javasdk.eventsourcedentity.EventSourcedEntityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import central.pharmacy.domain.Pharmacy;
import central.pharmacy.domain.PharmacyEvent;

import java.util.Optional;

@ComponentId("pharmacy")
public class PharmacyEntity
        extends EventSourcedEntity<Pharmacy, PharmacyEvent> {


    private final String entityId;

    private static final Logger logger = LoggerFactory.getLogger(PharmacyEntity.class);

    public PharmacyEntity(EventSourcedEntityContext context) {
        this.entityId = context.entityId();
    }

    @Override
    public Pharmacy emptyState() {
        return null;
    }

    public Effect<Done> create(Pharmacy pharmacy) {
        if (Optional.ofNullable(currentState()).isPresent()) {
            logger.info("Pharmacy id={} already exists.", entityId);
            return effects().error("Pharmacy already exists.");
        }
        if (isDeleted()) {
            logger.info("Pharmacy has been expunged, id={}", entityId);
            return effects().error("Pharmacy expunged.");
        }
        return effects()
                .persist(new PharmacyEvent.PharmacyCreated(pharmacy))
                .thenReply(newState -> Done.getInstance());
    }

    public ReadOnlyEffect<Optional<Pharmacy>> get() {
        if (isDeleted()) {
            return effects().error("Pharmacy expunged.");
        }
        return effects().reply(Optional.ofNullable(currentState()));
    }

    public Effect<Done> update(Pharmacy pharmacy) {
        if (isDeleted()) {
            return effects().error("Pharmacy expunged.");
        }
        if(currentState() == null)
            return effects().error("Pharmacy not found.");
        return effects()
                .persist(new PharmacyEvent.PharmacyUpdated(pharmacy))
                .thenReply(newState -> Done.getInstance());
    }

    public Effect<Done> delete() {
        if(currentState() == null)
            return effects().error("Pharmacy not found");
        else if(isDeleted())
            return effects().error("Pharmacy expunged");
        else return effects()
                    .persist(new PharmacyEvent.PharmacyDeleted())
                    .deleteEntity()
                    .thenReply(newState -> Done.getInstance());
    }

    public Pharmacy applyEvent(PharmacyEvent event) {
        return switch (event) {
            case PharmacyEvent.PharmacyCreated evt -> evt.pharmacy();
            case PharmacyEvent.PharmacyUpdated evt -> evt.pharmacy();
            case PharmacyEvent.PharmacyDeleted ignore -> currentState();
        };
    }

}