package id.ac.ui.cs.advprog.bidmartauctionservice.dto;

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
public class BidResponseDTO {

    private UUID id;
    private UUID auctionId;
    private UUID bidderId;
    private BigDecimal bidAmount;
    private Instant bidTime;
}