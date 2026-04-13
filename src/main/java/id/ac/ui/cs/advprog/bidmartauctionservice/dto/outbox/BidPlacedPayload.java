package id.ac.ui.cs.advprog.bidmartauctionservice.dto.outbox;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BidPlacedPayload {
    private UUID bidId;
}
