package pharmacy.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.consumer.Consumer;
import akka.javasdk.client.ComponentClient;
import pharmacy.domain.PatientRecordEvent;
import pharmacy.domain.PatientRecordEvent.PatientRecordCreated;
import pharmacy.domain.PatientRecordEvent.PatientRecordUpdated;
import pharmacy.domain.PatientRecordEvent.PatientRecordDeleted;
import pharmacy.domain.delivery.PatientRecordDelivery;

/**
 * Listens for patient record events, and starts the delivery process (which will send them to Central)
 */
@ComponentId("patient-record-router")
@Consume.FromEventSourcedEntity(value = PatientRecordEntity.class, ignoreUnknown = true)
public class PatientRecordRouter extends Consumer {

    private final ComponentClient componentClient;

    public PatientRecordRouter(ComponentClient componentClient) {
        this.componentClient = componentClient;
    }

    public Effect onCreate(PatientRecordCreated event) {
        return requestDelivery(toDelivery(event));
    }

    public Effect onUpdate(PatientRecordUpdated event) {
        return requestDelivery(toDelivery(event));
    }

    public Effect onDelete(PatientRecordDeleted event) {
        return requestDelivery(toDelivery(event));
    }

    private PatientRecordDelivery toDelivery(PatientRecordEvent event) {
        var id = messageContext().eventSubject().get();
        return new PatientRecordDelivery(id, event, false);
    }

    private Effect requestDelivery(PatientRecordDelivery delivery) {
        componentClient.forEventSourcedEntity(delivery.updateId())
                .method(PatientRecordDeliveryEntity::create)
                .invoke(delivery);
        return effects().done();
    }

}