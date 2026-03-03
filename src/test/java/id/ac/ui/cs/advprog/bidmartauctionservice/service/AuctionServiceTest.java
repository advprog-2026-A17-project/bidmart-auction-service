package id.ac.ui.cs.advprog.bidmartauctionservice.service;

import id.ac.ui.cs.advprog.bidmartauctionservice.dto.BidRequestDTO;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.Auction;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.Bid;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.enums.AuctionStatus;
import id.ac.ui.cs.advprog.bidmartauctionservice.repository.AuctionRepository;
import id.ac.ui.cs.advprog.bidmartauctionservice.repository.BidRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuctionServiceTest {

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private BidRepository bidRepository;

    @InjectMocks
    private AuctionServiceImpl auctionService;

    private Auction activeAuction;
    private UUID auctionId;
    private BidRequestDTO validBidRequest;

    @BeforeEach
    void setUp() {
        auctionId = UUID.randomUUID();
        activeAuction = Auction.builder()
                .id(auctionId)
                .listingId(UUID.randomUUID())
                .sellerId(UUID.randomUUID())
                .startingPrice(new BigDecimal("100.00"))
                .minimumIncrement(new BigDecimal("10.00"))
                .reservePrice(new BigDecimal("500.00"))
                .startTime(Instant.now().minusSeconds(3600))
                .endTime(Instant.now().plusSeconds(3600))
                .status(AuctionStatus.ACTIVE)
                .build();

        validBidRequest = BidRequestDTO.builder()
                .bidderId(UUID.randomUUID())
                .bidAmount(new BigDecimal("150.00"))
                .build();
    }

    @Test
    void testPlaceBid_Success() {
        when(auctionRepository.findByIdWithPessimisticWriteLock(auctionId)).thenReturn(Optional.of(activeAuction));

        Bid savedBid = Bid.builder()
                .id(UUID.randomUUID())
                .auction(activeAuction)
                .bidderId(validBidRequest.getBidderId())
                .bidAmount(validBidRequest.getBidAmount())
                .bidTime(Instant.now())
                .build();

        when(bidRepository.save(any(Bid.class))).thenReturn(savedBid);

        Bid result = auctionService.placeBid(auctionId, validBidRequest);

        assertEquals(new BigDecimal("150.00"), result.getAuction().getCurrentHighestBid());
        verify(auctionRepository).save(activeAuction);
        verify(bidRepository).save(any(Bid.class));
    }

    @Test
    void testPlaceBid_AuctionNotFound_ThrowsIllegalArgumentException() {
        when(auctionRepository.findByIdWithPessimisticWriteLock(auctionId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> auctionService.placeBid(auctionId, validBidRequest));
    }

    @Test
    void testPlaceBid_InvalidStatus_ThrowsIllegalStateException() {
        activeAuction.setStatus(AuctionStatus.CLOSED);
        when(auctionRepository.findByIdWithPessimisticWriteLock(auctionId)).thenReturn(Optional.of(activeAuction));

        assertThrows(IllegalStateException.class, () -> auctionService.placeBid(auctionId, validBidRequest));
    }

    @Test
    void testPlaceBid_TooLowAmount_ThrowsIllegalArgumentException() {
        activeAuction.setCurrentHighestBid(new BigDecimal("150.00"));
        validBidRequest.setBidAmount(new BigDecimal("155.00"));

        when(auctionRepository.findByIdWithPessimisticWriteLock(auctionId)).thenReturn(Optional.of(activeAuction));

        assertThrows(IllegalArgumentException.class, () -> auctionService.placeBid(auctionId, validBidRequest));
    }

    @Test
    void testPlaceBid_AntiSnipingExtension() {
        activeAuction.setEndTime(Instant.now().plusSeconds(30));
        when(auctionRepository.findByIdWithPessimisticWriteLock(auctionId)).thenReturn(Optional.of(activeAuction));

        Bid savedBid = Bid.builder().auction(activeAuction).build();
        when(bidRepository.save(any(Bid.class))).thenReturn(savedBid);

        auctionService.placeBid(auctionId, validBidRequest);

        assertEquals(AuctionStatus.EXTENDED, activeAuction.getStatus());
        Duration remaining = Duration.between(Instant.now(), activeAuction.getEndTime());
        assertTrue(remaining.toMinutes() >= 1);
    }
}