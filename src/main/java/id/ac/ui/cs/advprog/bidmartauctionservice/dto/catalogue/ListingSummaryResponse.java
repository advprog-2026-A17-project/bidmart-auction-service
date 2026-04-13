package id.ac.ui.cs.advprog.bidmartauctionservice.dto.catalogue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListingSummaryResponse {
    private String id;
    private String sellerId;
    private String status;
}
