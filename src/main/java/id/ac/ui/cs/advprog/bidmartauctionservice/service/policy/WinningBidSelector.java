package id.ac.ui.cs.advprog.bidmartauctionservice.service.policy;

import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.Bid;

import java.util.Optional;
import java.util.UUID;

public interface WinningBidSelector {
    Optional<Bid> findWinningBid(UUID auctionId);
}
