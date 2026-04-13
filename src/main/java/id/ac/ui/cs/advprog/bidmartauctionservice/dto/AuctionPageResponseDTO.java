package id.ac.ui.cs.advprog.bidmartauctionservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionPageResponseDTO {
    private List<AuctionResponseDTO> items;
    private int page;
    private int size;
    private long totalItems;
    private int totalPages;
}
