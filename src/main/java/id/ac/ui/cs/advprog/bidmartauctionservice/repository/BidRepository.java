package id.ac.ui.cs.advprog.bidmartauctionservice.repository;

import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BidRepository extends JpaRepository<Bid, UUID> {

    List<Bid> findByAuctionIdOrderByBidAmountDesc(UUID auctionId);

    Optional<Bid> findFirstByAuctionIdOrderByBidAmountDesc(UUID auctionId);
}