package id.ac.ui.cs.advprog.bidmartauctionservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAuctionRequest {
    @NotNull
    private UUID listingId;

    @NotNull
    private UUID sellerId;

    @NotNull
    @Positive
    private BigDecimal startingPrice;

    @NotNull
    @Positive
    private BigDecimal minimumIncrement;

    @NotNull
    @Positive
    private BigDecimal reservePrice;

    @NotNull
    private Instant startTime;

    @NotNull
    private Instant endTime;
}
