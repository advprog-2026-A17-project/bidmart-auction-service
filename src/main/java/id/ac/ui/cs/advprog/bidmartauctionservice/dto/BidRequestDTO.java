package id.ac.ui.cs.advprog.bidmartauctionservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidRequestDTO {

    @NotNull(message = "Bidder ID cannot be null")
    private UUID bidderId;

    @NotNull(message = "Bid amount cannot be null")
    @DecimalMin(value = "0.01", message = "Bid amount must be greater than 0")
    private BigDecimal bidAmount;
}