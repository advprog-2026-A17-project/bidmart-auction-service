package id.ac.ui.cs.advprog.bidmartauctionservice.scheduler;

import id.ac.ui.cs.advprog.bidmartauctionservice.event.AuctionEndedEvent;
import id.ac.ui.cs.advprog.bidmartauctionservice.event.BidPlacedEvent;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.Auction;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.Bid;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.OutboxEvent;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.enums.AuctionStatus;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.enums.OutboxEventType;
import id.ac.ui.cs.advprog.bidmartauctionservice.repository.AuctionRepository;
import id.ac.ui.cs.advprog.bidmartauctionservice.repository.BidRepository;
import id.ac.ui.cs.advprog.bidmartauctionservice.repository.OutboxEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxEventPublisherSchedulerTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private BidRepository bidRepository;

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OutboxEventPublisherScheduler scheduler;

    @Test
    void publishPendingEvents_PublishesBidPlacedEventAndMarksPublished() {
        UUID bidId = UUID.randomUUID();
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .id(UUID.randomUUID())
                .aggregateId(bidId)
                .eventType(OutboxEventType.BID_PLACED)
                .payload("{\"bidId\":\"" + bidId + "\"}")
                .published(false)
                .createdAt(Instant.now())
                .build();

        Bid bid = Bid.builder()
                .id(bidId)
                .auction(Auction.builder().id(UUID.randomUUID()).build())
                .bidderId(UUID.randomUUID())
                .bidAmount(new BigDecimal("120.00"))
                .bidTime(Instant.now())
                .build();

        when(outboxEventRepository.findTop50ByPublishedFalseOrderByCreatedAtAsc()).thenReturn(List.of(outboxEvent));
        when(bidRepository.findById(bidId)).thenReturn(Optional.of(bid));

        scheduler.publishPendingEvents();

        verify(eventPublisher).publishEvent(any(BidPlacedEvent.class));
        verify(outboxEventRepository).save(outboxEvent);
    }

    @Test
    void publishPendingEvents_PublishesAuctionEndedEventAndMarksPublished() {
        UUID auctionId = UUID.randomUUID();
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .id(UUID.randomUUID())
                .aggregateId(auctionId)
                .eventType(OutboxEventType.AUCTION_ENDED)
                .payload("{\"auctionId\":\"" + auctionId + "\",\"finalStatus\":\"WON\"}")
                .published(false)
                .createdAt(Instant.now())
                .build();

        Auction auction = Auction.builder()
                .id(auctionId)
                .listingId(UUID.randomUUID())
                .sellerId(UUID.randomUUID())
                .startingPrice(new BigDecimal("100.00"))
                .minimumIncrement(new BigDecimal("10.00"))
                .reservePrice(new BigDecimal("150.00"))
                .startTime(Instant.now().minusSeconds(3600))
                .endTime(Instant.now().minusSeconds(10))
                .status(AuctionStatus.WON)
                .build();

        when(outboxEventRepository.findTop50ByPublishedFalseOrderByCreatedAtAsc()).thenReturn(List.of(outboxEvent));
        when(auctionRepository.findById(auctionId)).thenReturn(Optional.of(auction));

        scheduler.publishPendingEvents();

        verify(eventPublisher).publishEvent(any(AuctionEndedEvent.class));
        verify(outboxEventRepository).save(outboxEvent);
    }
}
