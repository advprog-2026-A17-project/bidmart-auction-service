package id.ac.ui.cs.advprog.bidmartauctionservice.service.policy;

import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.Auction;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.enums.AuctionStatus;

public interface AuctionSettlementPolicy {
    AuctionStatus determineFinalStatus(Auction auction);
}
