package id.ac.ui.cs.advprog.bidmartauctionservice.service.policy;

import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.Auction;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.enums.AuctionStatus;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.lifecycle.AuctionLifecycleStateMachine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class ConfigurableAntiSnipingPolicy implements AntiSnipingPolicy {

    private final Duration thresholdDuration;
    private final Duration extensionDuration;

    public ConfigurableAntiSnipingPolicy(
            @Value("${auction.anti-sniping.threshold-seconds:120}") long thresholdSeconds,
            @Value("${auction.anti-sniping.extension-seconds:120}") long extensionSeconds
    ) {
        this.thresholdDuration = Duration.ofSeconds(thresholdSeconds);
        this.extensionDuration = Duration.ofSeconds(extensionSeconds);
    }

    @Override
    public void applyForBid(Auction auction, Instant bidTime) {
        Duration remainingTime = Duration.between(bidTime, auction.getEndTime());
        if (remainingTime.compareTo(thresholdDuration) >= 0) {
            return;
        }

        auction.setEndTime(bidTime.plus(extensionDuration));
        if (auction.getStatus() == AuctionStatus.ACTIVE) {
            AuctionLifecycleStateMachine.enforceTransition(auction.getStatus(), AuctionStatus.EXTENDED);
            auction.setStatus(AuctionStatus.EXTENDED);
        }
    }
}
