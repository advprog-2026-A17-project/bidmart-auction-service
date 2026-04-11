package id.ac.ui.cs.advprog.bidmartauctionservice.dto.wallet;

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
public class ReleaseFundsRequest {
    private UUID userId;
    private BigDecimal amount;
    private String description;
}
