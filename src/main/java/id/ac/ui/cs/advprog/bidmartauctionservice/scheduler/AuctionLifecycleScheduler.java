package id.ac.ui.cs.advprog.bidmartauctionservice.scheduler;

import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.Auction;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.enums.AuctionStatus;
import id.ac.ui.cs.advprog.bidmartauctionservice.repository.AuctionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AuctionLifecycleScheduler {

    private final AuctionRepository auctionRepository;

    @Scheduled(fixedDelay = 30000) // Run every 30 seconds
    @Transactional
    public void closeExpiredAuctions() {
        Instant now = Instant.now();
        List<AuctionStatus> statuses = Arrays.asList(AuctionStatus.ACTIVE, AuctionStatus.EXTENDED);

        List<Auction> expiredAuctions = auctionRepository.findEndedAuctionsByMultipleStatuses(statuses, now);

        for (Auction auction : expiredAuctions) {
            if (auction.getCurrentHighestBid() != null &&
                auction.getCurrentHighestBid().compareTo(auction.getReservePrice()) >= 0) {
                auction.setStatus(AuctionStatus.WON);
            } else {
                auction.setStatus(AuctionStatus.UNSOLD);
            }
            auctionRepository.save(auction);
        }
    }
}
