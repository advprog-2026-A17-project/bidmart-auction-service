package id.ac.ui.cs.advprog.bidmartauctionservice.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bids")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    @Column(name = "bidder_id", nullable = false)
    private UUID bidderId;

    @Column(name = "bid_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal bidAmount;

    @CreationTimestamp
    @Column(name = "bid_time", nullable = false, updatable = false)
    private Instant bidTime;
}