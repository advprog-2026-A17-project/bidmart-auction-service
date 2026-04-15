package id.ac.ui.cs.advprog.bidmartauctionservice.service.policy;

import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.Auction;

import java.time.Instant;

public interface AntiSnipingPolicy {
    void applyForBid(Auction auction, Instant bidTime);
}
