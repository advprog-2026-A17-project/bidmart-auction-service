package id.ac.ui.cs.advprog.bidmartauctionservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.OutboxEvent;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.enums.AuctionStatus;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.enums.OutboxEventType;
import id.ac.ui.cs.advprog.bidmartauctionservice.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OutboxEventServiceTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @InjectMocks
    private OutboxEventService outboxEventService;

    @BeforeEach
    void setUp() {
        outboxEventService = new OutboxEventService(outboxEventRepository, new ObjectMapper());
    }

    @Test
    void enqueueBidPlaced_PersistsOutboxEvent() {
        UUID bidId = UUID.randomUUID();

        outboxEventService.enqueueBidPlaced(bidId);

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(captor.capture());
        OutboxEvent savedEvent = captor.getValue();

        assertEquals(OutboxEventType.BID_PLACED, savedEvent.getEventType());
        assertEquals(bidId, savedEvent.getAggregateId());
        assertFalse(savedEvent.isPublished());
    }

    @Test
    void enqueueAuctionEnded_PersistsOutboxEvent() {
        UUID auctionId = UUID.randomUUID();

        outboxEventService.enqueueAuctionEnded(auctionId, AuctionStatus.WON);

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(captor.capture());
        OutboxEvent savedEvent = captor.getValue();

        assertEquals(OutboxEventType.AUCTION_ENDED, savedEvent.getEventType());
        assertEquals(auctionId, savedEvent.getAggregateId());
        assertFalse(savedEvent.isPublished());
    }
}
