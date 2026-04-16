package id.ac.ui.cs.advprog.bidmartauctionservice.service.policy;

import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.Auction;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.enums.AuctionStatus;
import org.springframework.stereotype.Component;

@Component
public class ReservePriceAuctionSettlementPolicy implements AuctionSettlementPolicy {
    @Override
    public AuctionStatus determineFinalStatus(Auction auction) {
        if (auction.getCurrentHighestBid() != null &&
                auction.getCurrentHighestBid().compareTo(auction.getReservePrice()) >= 0) {
            return AuctionStatus.WON;
        }
        return AuctionStatus.UNSOLD;
    }
}
