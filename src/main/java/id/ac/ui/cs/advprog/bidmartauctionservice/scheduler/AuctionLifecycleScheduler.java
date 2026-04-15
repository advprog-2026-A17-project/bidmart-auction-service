package id.ac.ui.cs.advprog.bidmartauctionservice.scheduler;

import id.ac.ui.cs.advprog.bidmartauctionservice.client.WalletServiceClient;
import id.ac.ui.cs.advprog.bidmartauctionservice.dto.wallet.ConvertFundsRequest;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.Bid;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.Auction;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.enums.AuctionStatus;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.lifecycle.AuctionLifecycleStateMachine;
import id.ac.ui.cs.advprog.bidmartauctionservice.repository.AuctionRepository;
import id.ac.ui.cs.advprog.bidmartauctionservice.repository.BidRepository;
import id.ac.ui.cs.advprog.bidmartauctionservice.service.OutboxEventService;
import id.ac.ui.cs.advprog.bidmartauctionservice.service.policy.AuctionSettlementPolicy;
import lombok.RequiredArgsConstructor;
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
    private final OutboxEventService outboxEventService;
    private final AuctionSettlementPolicy settlementPolicy;

    @Scheduled(fixedDelayString = "${auction.lifecycle.fixed-delay-ms:30000}")
    @Transactional
    public void closeExpiredAuctions() {
        Instant now = Instant.now();
        activateDraftAuctions(now);

        List<AuctionStatus> statuses = Arrays.asList(AuctionStatus.ACTIVE, AuctionStatus.EXTENDED);
        List<Auction> expiredAuctions = auctionRepository.findEndedAuctionsByMultipleStatuses(statuses, now);

        for (Auction auction : expiredAuctions) {
            AuctionLifecycleStateMachine.enforceTransition(auction.getStatus(), AuctionStatus.CLOSED);
            auction.setStatus(AuctionStatus.CLOSED);
            auctionRepository.save(auction);

            AuctionStatus finalStatus = settlementPolicy.determineFinalStatus(auction);

            AuctionLifecycleStateMachine.enforceTransition(auction.getStatus(), finalStatus);
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

            outboxEventService.enqueueAuctionEnded(auction.getId(), finalStatus);
        }
    }

    private void activateDraftAuctions(Instant now) {
        List<Auction> draftAuctions = auctionRepository.findByStatusAndStartTimeBefore(AuctionStatus.DRAFT, now);
        for (Auction draftAuction : draftAuctions) {
            AuctionLifecycleStateMachine.enforceTransition(draftAuction.getStatus(), AuctionStatus.ACTIVE);
            draftAuction.setStatus(AuctionStatus.ACTIVE);
            auctionRepository.save(draftAuction);
        }
    }
}
