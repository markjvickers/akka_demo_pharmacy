package pharmacy.application.central.client;
import akka.javasdk.http.StrictResponse;
import akka.util.ByteString;
import pharmacy.application.central.client.domain.StorePatientRecord;

public interface CentralClient {

    StrictResponse<ByteString> create(StorePatientRecord record);

    StrictResponse<akka.util.ByteString> update(StorePatientRecord record);

    StrictResponse<StorePatientRecord> get(String pharmacyId, String patientId);

    StrictResponse<akka.util.ByteString> delete(String pharmacyId, String patientId);

}
