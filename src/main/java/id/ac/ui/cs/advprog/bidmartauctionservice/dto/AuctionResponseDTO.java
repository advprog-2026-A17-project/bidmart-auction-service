package id.ac.ui.cs.advprog.bidmartauctionservice.dto;

import id.ac.ui.cs.advprog.bidmartauctionservice.model.enums.AuctionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionResponseDTO {
    private UUID id;
    private UUID listingId;
    private UUID sellerId;
    private BigDecimal startingPrice;
    private BigDecimal minimumIncrement;
    private BigDecimal reservePrice;
    private BigDecimal currentHighestBid;
    private Instant startTime;
    private Instant endTime;
    private AuctionStatus status;
}
