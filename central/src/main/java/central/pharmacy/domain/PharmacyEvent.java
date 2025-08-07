package central.pharmacy.domain;

import akka.javasdk.annotations.TypeName;

public sealed interface PharmacyEvent {

    @TypeName("pharmacy-created")
    record PharmacyCreated(Pharmacy pharmacy) implements PharmacyEvent {}

    @TypeName("pharmacy-updated")
    record PharmacyUpdated(Pharmacy pharmacy) implements PharmacyEvent {}

    @TypeName("pharmacy-deleted")
    record PharmacyDeleted() implements PharmacyEvent {}

}
