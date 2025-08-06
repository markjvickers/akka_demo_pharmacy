package pharmacy.application.central.delivery;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.view.TableUpdater;
import akka.javasdk.view.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import akka.javasdk.annotations.Query;
import akka.javasdk.annotations.Table;
import pharmacy.domain.delivery.PatientRecordDeliveryEvent;

/**
 * The intent of this view is to add some observability to the patient record delivery process in
 * order to make the online/offline demo more effective.
 * The query syntax is somewhat restricted - I wanted to use a single query to get the combined counts,
 * but haven't found a way to do so.
 * Instead, the Endpoint will invoke it twice.
 */
@ComponentId("patient-record-delivery-view")
public class PatientRecordDeliveryView extends View {

    private static final Logger logger = LoggerFactory.getLogger(PatientRecordDeliveryView.class);

    @Query(
            """
            SELECT COUNT(*) as amount from patientRecordDelivery
            """
    )
    public QueryEffect<Count> getRequiredDeliveryCount() {
        return queryResult();
    }

    @Query(
            """
            SELECT COUNT(*) as amount from patientRecordDelivery where ok=true
            """
    )
    public QueryEffect<Count> getFinishedDeliveryCount() {
        return queryResult();
    }

    public record Count(Long amount) {}

    public record Delivery(String eventID, String patientId, Boolean ok) {

        public Delivery withOk() {
            return new Delivery(eventID, patientId, true);
        }

    }

    @Table("patientRecordDelivery")
    @Consume.FromEventSourcedEntity(value = PatientRecordDeliveryEntity.class, ignoreUnknown = true) // <4>
    public static class DeliveryUpdater extends TableUpdater<Delivery> {

        private String updateId() {
            return updateContext().eventSubject().get();
        }

        public Effect<Delivery> onRequired(PatientRecordDeliveryEvent.PatientRecordRequired event) {
            return effects().updateRow(new Delivery(updateId(), event.patientId(), false));
        }

        public Effect<Delivery> onDelivered(PatientRecordDeliveryEvent.PatientRecordDelivered ignore) {
            return effects().updateRow(rowState().withOk());
        }

    }

}