package id.ac.ui.cs.advprog.bidmartauctionservice.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.bidmartauctionservice.dto.outbox.AuctionEndedPayload;
import id.ac.ui.cs.advprog.bidmartauctionservice.dto.outbox.BidPlacedPayload;
import id.ac.ui.cs.advprog.bidmartauctionservice.event.AuctionEndedEvent;
import id.ac.ui.cs.advprog.bidmartauctionservice.event.BidPlacedEvent;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.Auction;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.Bid;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.OutboxEvent;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.enums.OutboxEventType;
import id.ac.ui.cs.advprog.bidmartauctionservice.repository.AuctionRepository;
import id.ac.ui.cs.advprog.bidmartauctionservice.repository.BidRepository;
import id.ac.ui.cs.advprog.bidmartauctionservice.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboxEventPublisherScheduler {

    private final OutboxEventRepository outboxEventRepository;
    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${outbox.publisher.fixed-delay-ms:10000}")
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findTop50ByPublishedFalseOrderByCreatedAtAsc();
        Instant now = Instant.now();

        for (OutboxEvent pendingEvent : pendingEvents) {
            publishEvent(pendingEvent);
            pendingEvent.markPublished(now);
            outboxEventRepository.save(pendingEvent);
        }
    }

    private void publishEvent(OutboxEvent outboxEvent) {
        OutboxEventType eventType = outboxEvent.getEventType();
        if (eventType == OutboxEventType.BID_PLACED) {
            publishBidPlaced(outboxEvent);
            return;
        }
        if (eventType == OutboxEventType.AUCTION_ENDED) {
            publishAuctionEnded(outboxEvent);
            return;
        }
        throw new IllegalStateException("Unsupported outbox event type: " + eventType);
    }

    private void publishBidPlaced(OutboxEvent outboxEvent) {
        BidPlacedPayload payload = readPayload(outboxEvent.getPayload(), BidPlacedPayload.class);
        Bid bid = bidRepository.findById(payload.getBidId())
                .orElseThrow(() -> new IllegalStateException("Bid not found for outbox event " + outboxEvent.getId()));
        eventPublisher.publishEvent(new BidPlacedEvent(this, bid));
    }

    private void publishAuctionEnded(OutboxEvent outboxEvent) {
        AuctionEndedPayload payload = readPayload(outboxEvent.getPayload(), AuctionEndedPayload.class);
        Auction auction = auctionRepository.findById(payload.getAuctionId())
                .orElseThrow(() -> new IllegalStateException("Auction not found for outbox event " + outboxEvent.getId()));
        eventPublisher.publishEvent(new AuctionEndedEvent(this, auction, payload.getFinalStatus()));
    }

    private <T> T readPayload(String payload, Class<T> payloadType) {
        try {
            return objectMapper.readValue(payload, payloadType);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to deserialize outbox payload", ex);
        }
    }
}
