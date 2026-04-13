package id.ac.ui.cs.advprog.bidmartauctionservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.bidmartauctionservice.dto.outbox.AuctionEndedPayload;
import id.ac.ui.cs.advprog.bidmartauctionservice.dto.outbox.BidPlacedPayload;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.OutboxEvent;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.enums.AuctionStatus;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.enums.OutboxEventType;
import id.ac.ui.cs.advprog.bidmartauctionservice.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void enqueueBidPlaced(UUID bidId) {
        BidPlacedPayload payload = BidPlacedPayload.builder()
                .bidId(bidId)
                .build();
        enqueue(OutboxEventType.BID_PLACED, bidId, payload);
    }

    @Transactional
    public void enqueueAuctionEnded(UUID auctionId, AuctionStatus finalStatus) {
        AuctionEndedPayload payload = AuctionEndedPayload.builder()
                .auctionId(auctionId)
                .finalStatus(finalStatus)
                .build();
        enqueue(OutboxEventType.AUCTION_ENDED, auctionId, payload);
    }

    private void enqueue(OutboxEventType eventType, UUID aggregateId, Object payload) {
        OutboxEvent event = OutboxEvent.builder()
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payload(writePayload(payload))
                .published(false)
                .build();
        outboxEventRepository.save(event);
    }

    private String writePayload(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize outbox payload", ex);
        }
    }
}
