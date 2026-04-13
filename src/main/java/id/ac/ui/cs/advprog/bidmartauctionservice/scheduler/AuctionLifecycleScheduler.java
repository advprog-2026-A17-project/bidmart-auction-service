package id.ac.ui.cs.advprog.bidmartauctionservice.scheduler;

import id.ac.ui.cs.advprog.bidmartauctionservice.client.WalletServiceClient;
import id.ac.ui.cs.advprog.bidmartauctionservice.dto.wallet.ConvertFundsRequest;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.Bid;
import id.ac.ui.cs.advprog.bidmartauctionservice.event.AuctionEndedEvent;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.Auction;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.enums.AuctionStatus;
import id.ac.ui.cs.advprog.bidmartauctionservice.repository.AuctionRepository;
import id.ac.ui.cs.advprog.bidmartauctionservice.repository.BidRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuctionLifecycleScheduler {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final WalletServiceClient walletServiceClient;
    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(fixedDelay = 30000) // Run every 30 seconds
    @Transactional
    public void closeExpiredAuctions() {
        Instant now = Instant.now();
        List<AuctionStatus> statuses = Arrays.asList(AuctionStatus.ACTIVE, AuctionStatus.EXTENDED);

        List<Auction> expiredAuctions = auctionRepository.findEndedAuctionsByMultipleStatuses(statuses, now);

        for (Auction auction : expiredAuctions) {
            AuctionStatus finalStatus;
            if (auction.getCurrentHighestBid() != null &&
                auction.getCurrentHighestBid().compareTo(auction.getReservePrice()) >= 0) {
                finalStatus = AuctionStatus.WON;
            } else {
                finalStatus = AuctionStatus.UNSOLD;
            }

            auction.setStatus(finalStatus);
            auctionRepository.save(auction);

            if (finalStatus == AuctionStatus.WON) {
                Optional<Bid> highestBid = bidRepository.findFirstByAuctionIdOrderByBidAmountDesc(auction.getId());
                highestBid.ifPresent(bid -> {
                    ConvertFundsRequest convertRequest = ConvertFundsRequest.builder()
                            .userId(bid.getBidderId())
                            .amount(bid.getBidAmount())
                            .description("Auction won " + auction.getId())
                            .build();
                    walletServiceClient.convertFunds(convertRequest);
                });
            }

            eventPublisher.publishEvent(new AuctionEndedEvent(this, auction, finalStatus));
        }
    }
}
