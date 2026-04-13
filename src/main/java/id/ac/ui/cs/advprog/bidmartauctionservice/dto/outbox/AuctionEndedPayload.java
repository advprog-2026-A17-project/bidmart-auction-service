package id.ac.ui.cs.advprog.bidmartauctionservice.dto.outbox;

import id.ac.ui.cs.advprog.bidmartauctionservice.model.enums.AuctionStatus;
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
public class AuctionEndedPayload {
    private UUID auctionId;
    private AuctionStatus finalStatus;
}
