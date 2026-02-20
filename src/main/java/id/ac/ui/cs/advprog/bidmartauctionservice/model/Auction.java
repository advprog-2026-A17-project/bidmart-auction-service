package id.ac.ui.cs.advprog.bidmartauctionservice.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "auctions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Auction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String itemId;
    private String itemName;
    private Double currentHighestBid;
    private Double minIncrement;
    private Double reservePrice;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
}