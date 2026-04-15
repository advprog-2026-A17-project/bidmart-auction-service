package id.ac.ui.cs.advprog.bidmartauctionservice.scheduler;

import id.ac.ui.cs.advprog.bidmartauctionservice.client.WalletServiceClient;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.Auction;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.Bid;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.enums.AuctionStatus;
import id.ac.ui.cs.advprog.bidmartauctionservice.repository.AuctionRepository;
import id.ac.ui.cs.advprog.bidmartauctionservice.repository.BidRepository;
import id.ac.ui.cs.advprog.bidmartauctionservice.service.OutboxEventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Scheduled;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuctionLifecycleSchedulerTest {

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private OutboxEventService outboxEventService;

    @Mock
    private BidRepository bidRepository;

    @Mock
    private WalletServiceClient walletServiceClient;

    @InjectMocks
    private AuctionLifecycleScheduler scheduler;

    private Auction activeAuction;
    private Auction extendedAuction;
    private Auction draftAuction;
    private UUID auctionId1;
    private UUID auctionId2;

    @BeforeEach
    void setUp() {
        auctionId1 = UUID.randomUUID();
        auctionId2 = UUID.randomUUID();

        activeAuction = Auction.builder()
                .id(auctionId1)
                .listingId(UUID.randomUUID())
                .sellerId(UUID.randomUUID())
                .startingPrice(new BigDecimal("100.00"))
                .minimumIncrement(new BigDecimal("10.00"))
                .reservePrice(new BigDecimal("500.00"))
                .startTime(Instant.now().minusSeconds(7200))
                .endTime(Instant.now().minusSeconds(60))
                .status(AuctionStatus.ACTIVE)
                .currentHighestBid(new BigDecimal("600.00"))
                .build();

        extendedAuction = Auction.builder()
                .id(auctionId2)
                .listingId(UUID.randomUUID())
                .sellerId(UUID.randomUUID())
                .startingPrice(new BigDecimal("100.00"))
                .minimumIncrement(new BigDecimal("10.00"))
                .reservePrice(new BigDecimal("500.00"))
                .startTime(Instant.now().minusSeconds(7200))
                .endTime(Instant.now().minusSeconds(60))
                .status(AuctionStatus.EXTENDED)
                .currentHighestBid(new BigDecimal("400.00"))
                .build();

        draftAuction = Auction.builder()
                .id(UUID.randomUUID())
                .listingId(UUID.randomUUID())
                .sellerId(UUID.randomUUID())
                .startingPrice(new BigDecimal("100.00"))
                .minimumIncrement(new BigDecimal("10.00"))
                .reservePrice(new BigDecimal("500.00"))
                .startTime(Instant.now().minusSeconds(60))
                .endTime(Instant.now().plusSeconds(3600))
                .status(AuctionStatus.DRAFT)
                .build();
    }

    @Test
    void testCloseAuctions_AuctionWon() {
        when(auctionRepository.findByStatusAndStartTimeBefore(eq(AuctionStatus.DRAFT), any(Instant.class)))
                .thenReturn(java.util.Collections.emptyList());
        when(auctionRepository.findEndedAuctionsByMultipleStatuses(
                eq(Arrays.asList(AuctionStatus.ACTIVE, AuctionStatus.EXTENDED)),
                any(Instant.class)))
                .thenReturn(Arrays.asList(activeAuction));

        when(bidRepository.findFirstByAuctionIdOrderByBidAmountDesc(activeAuction.getId()))
                .thenReturn(java.util.Optional.of(Bid.builder()
                        .auction(activeAuction)
                        .bidderId(UUID.randomUUID())
                        .bidAmount(new BigDecimal("600.00"))
                        .build()));
        scheduler.closeExpiredAuctions();

        assert activeAuction.getStatus() == AuctionStatus.WON;
        verify(auctionRepository, times(2)).save(activeAuction);
        verify(walletServiceClient).convertFunds(any());
        verify(outboxEventService).enqueueAuctionEnded(activeAuction.getId(), AuctionStatus.WON);
    }

    @Test
    void testCloseAuctions_AuctionUnsold() {
        when(auctionRepository.findByStatusAndStartTimeBefore(eq(AuctionStatus.DRAFT), any(Instant.class)))
                .thenReturn(java.util.Collections.emptyList());
        when(auctionRepository.findEndedAuctionsByMultipleStatuses(
                eq(Arrays.asList(AuctionStatus.ACTIVE, AuctionStatus.EXTENDED)),
                any(Instant.class)))
                .thenReturn(Arrays.asList(extendedAuction));

        scheduler.closeExpiredAuctions();

        assert extendedAuction.getStatus() == AuctionStatus.UNSOLD;
        verify(auctionRepository, times(2)).save(extendedAuction);
        verify(walletServiceClient, org.mockito.Mockito.never()).convertFunds(any());
        verify(outboxEventService).enqueueAuctionEnded(extendedAuction.getId(), AuctionStatus.UNSOLD);
    }

    @Test
    void testCloseAuctions_ActivatesDraftAuctionsBeforeExpiryProcessing() {
        when(auctionRepository.findByStatusAndStartTimeBefore(eq(AuctionStatus.DRAFT), any(Instant.class)))
                .thenReturn(java.util.List.of(draftAuction));
        when(auctionRepository.findEndedAuctionsByMultipleStatuses(
                eq(Arrays.asList(AuctionStatus.ACTIVE, AuctionStatus.EXTENDED)),
                any(Instant.class)))
                .thenReturn(java.util.Collections.emptyList());

        scheduler.closeExpiredAuctions();

        assert draftAuction.getStatus() == AuctionStatus.ACTIVE;
        verify(auctionRepository).save(draftAuction);
    }

    @Test
    void testCloseExpiredAuctions_UsesConfigurableFixedDelay() throws NoSuchMethodException {
        Scheduled scheduled = AuctionLifecycleScheduler.class
                .getDeclaredMethod("closeExpiredAuctions")
                .getAnnotation(Scheduled.class);

        assertEquals("${auction.lifecycle.fixed-delay-ms:30000}", scheduled.fixedDelayString());
    }
}
