package pharmacy.application.central.delivery;

import akka.javasdk.consumer.MessageContext;

public class UpdateIdUtility {

    public static String getUpdateId(MessageContext messageContext) {
        var cloudEvent = messageContext.metadata().asCloudEvent();
        var seqNum = cloudEvent.sequenceString().get();
        var patientId = messageContext.eventSubject().get();
        return patientId + "_" + seqNum;
    }

    public static String getPatientId(MessageContext messageContext) {
        var cloudEvent = messageContext.metadata().asCloudEvent();
        return cloudEvent.subject().get();
    }


}
