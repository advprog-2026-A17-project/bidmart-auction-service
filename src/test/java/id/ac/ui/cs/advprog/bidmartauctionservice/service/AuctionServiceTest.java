package id.ac.ui.cs.advprog.bidmartauctionservice.service;

import id.ac.ui.cs.advprog.bidmartauctionservice.client.CatalogueServiceClient;
import id.ac.ui.cs.advprog.bidmartauctionservice.client.WalletServiceClient;
import id.ac.ui.cs.advprog.bidmartauctionservice.dto.BidRequestDTO;
import id.ac.ui.cs.advprog.bidmartauctionservice.dto.CreateAuctionRequest;
import id.ac.ui.cs.advprog.bidmartauctionservice.dto.catalogue.ListingSummaryResponse;
import id.ac.ui.cs.advprog.bidmartauctionservice.dto.wallet.HoldFundsRequest;
import id.ac.ui.cs.advprog.bidmartauctionservice.dto.wallet.ReleaseFundsRequest;
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
import id.ac.ui.cs.advprog.bidmartauctionservice.service.policy.AntiSnipingPolicy;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuctionServiceTest {

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private BidRepository bidRepository;

    @Mock
    private WalletServiceClient walletServiceClient;

    @Mock
    private CatalogueServiceClient catalogueServiceClient;

    @Mock
    private OutboxEventService outboxEventService;

    @Mock
    private AntiSnipingPolicy antiSnipingPolicy;

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
        when(catalogueServiceClient.getListing(activeAuction.getListingId())).thenReturn(ListingSummaryResponse.builder()
                .id(activeAuction.getListingId().toString())
                .sellerId(activeAuction.getSellerId().toString())
                .status("ACTIVE")
                .build());

        Bid savedBid = Bid.builder()
                .id(UUID.randomUUID())
                .auction(activeAuction)
                .bidderId(validBidRequest.getBidderId())
                .bidAmount(validBidRequest.getBidAmount())
                .bidTime(Instant.now())
                .build();

        when(bidRepository.save(any(Bid.class))).thenReturn(savedBid);
        when(bidRepository.findFirstByAuctionIdOrderByBidAmountDescBidTimeAsc(auctionId)).thenReturn(Optional.empty());
        doNothing().when(walletServiceClient).holdFunds(any(HoldFundsRequest.class));

        Bid result = auctionService.placeBid(auctionId, validBidRequest);

        assertEquals(new BigDecimal("150.00"), result.getAuction().getCurrentHighestBid());
        verify(auctionRepository).save(activeAuction);
        verify(bidRepository).save(any(Bid.class));
        verify(outboxEventService).enqueueBidPlaced(savedBid.getId());
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
        when(catalogueServiceClient.getListing(activeAuction.getListingId())).thenReturn(ListingSummaryResponse.builder()
                .id(activeAuction.getListingId().toString())
                .sellerId(activeAuction.getSellerId().toString())
                .status("ACTIVE")
                .build());

        assertThrows(IllegalStateException.class, () -> auctionService.placeBid(auctionId, validBidRequest));
    }

    @Test
    void testPlaceBid_TooLowAmount_ThrowsIllegalArgumentException() {
        activeAuction.setCurrentHighestBid(new BigDecimal("150.00"));
        validBidRequest.setBidAmount(new BigDecimal("155.00"));

        when(auctionRepository.findByIdWithPessimisticWriteLock(auctionId)).thenReturn(Optional.of(activeAuction));
        when(catalogueServiceClient.getListing(activeAuction.getListingId())).thenReturn(ListingSummaryResponse.builder()
                .id(activeAuction.getListingId().toString())
                .sellerId(activeAuction.getSellerId().toString())
                .status("ACTIVE")
                .build());

        assertThrows(IllegalArgumentException.class, () -> auctionService.placeBid(auctionId, validBidRequest));
    }

    @Test
    void testPlaceBid_AntiSnipingExtension() {
        activeAuction.setEndTime(Instant.now().plusSeconds(30));
        when(auctionRepository.findByIdWithPessimisticWriteLock(auctionId)).thenReturn(Optional.of(activeAuction));
        when(catalogueServiceClient.getListing(activeAuction.getListingId())).thenReturn(ListingSummaryResponse.builder()
                .id(activeAuction.getListingId().toString())
                .sellerId(activeAuction.getSellerId().toString())
                .status("ACTIVE")
                .build());

        Bid savedBid = Bid.builder().auction(activeAuction).build();
        when(bidRepository.save(any(Bid.class))).thenReturn(savedBid);
        when(bidRepository.findFirstByAuctionIdOrderByBidAmountDescBidTimeAsc(auctionId)).thenReturn(Optional.empty());
        doNothing().when(walletServiceClient).holdFunds(any(HoldFundsRequest.class));

        auctionService.placeBid(auctionId, validBidRequest);

        verify(antiSnipingPolicy).applyForBid(any(Auction.class), any(Instant.class));
    }

    @Test
    void testPlaceBid_WithWalletHold_Success() {
        when(auctionRepository.findByIdWithPessimisticWriteLock(auctionId)).thenReturn(Optional.of(activeAuction));
        when(catalogueServiceClient.getListing(activeAuction.getListingId())).thenReturn(ListingSummaryResponse.builder()
                .id(activeAuction.getListingId().toString())
                .sellerId(activeAuction.getSellerId().toString())
                .status("ACTIVE")
                .build());

        Bid savedBid = Bid.builder()
                .id(UUID.randomUUID())
                .auction(activeAuction)
                .bidderId(validBidRequest.getBidderId())
                .bidAmount(validBidRequest.getBidAmount())
                .bidTime(Instant.now())
                .build();

        when(bidRepository.save(any(Bid.class))).thenReturn(savedBid);
        when(bidRepository.findFirstByAuctionIdOrderByBidAmountDescBidTimeAsc(auctionId)).thenReturn(Optional.empty());
        doNothing().when(walletServiceClient).holdFunds(any(HoldFundsRequest.class));

        Bid result = auctionService.placeBid(auctionId, validBidRequest);

        assertEquals(new BigDecimal("150.00"), result.getBidAmount());
        verify(walletServiceClient).holdFunds(argThat(req ->
                req.getUserId().equals(validBidRequest.getBidderId()) &&
                req.getAmount().equals(validBidRequest.getBidAmount())
        ));
    }

    @Test
    void testPlaceBid_WithWalletHoldFailure_ThrowsException() {
        when(auctionRepository.findByIdWithPessimisticWriteLock(auctionId)).thenReturn(Optional.of(activeAuction));
        when(catalogueServiceClient.getListing(activeAuction.getListingId())).thenReturn(ListingSummaryResponse.builder()
                .id(activeAuction.getListingId().toString())
                .sellerId(activeAuction.getSellerId().toString())
                .status("ACTIVE")
                .build());
        doThrow(new RuntimeException("Wallet service unavailable"))
                .when(walletServiceClient).holdFunds(any(HoldFundsRequest.class));

        assertThrows(RuntimeException.class, () -> auctionService.placeBid(auctionId, validBidRequest));
    }

    @Test
    void testPlaceBid_WithPriorBidder_ReleasePreviousBidderFunds() {
        UUID previousBidderId = UUID.randomUUID();
        BigDecimal previousBidAmount = new BigDecimal("120.00");
        activeAuction.setCurrentHighestBid(previousBidAmount);

        when(auctionRepository.findByIdWithPessimisticWriteLock(auctionId)).thenReturn(Optional.of(activeAuction));
        when(catalogueServiceClient.getListing(activeAuction.getListingId())).thenReturn(ListingSummaryResponse.builder()
                .id(activeAuction.getListingId().toString())
                .sellerId(activeAuction.getSellerId().toString())
                .status("ACTIVE")
                .build());

        Bid savedBid = Bid.builder()
                .id(UUID.randomUUID())
                .auction(activeAuction)
                .bidderId(validBidRequest.getBidderId())
                .bidAmount(validBidRequest.getBidAmount())
                .bidTime(Instant.now())
                .build();

        when(bidRepository.save(any(Bid.class))).thenReturn(savedBid);
        when(bidRepository.findFirstByAuctionIdOrderByBidAmountDescBidTimeAsc(auctionId))
                .thenReturn(Optional.of(Bid.builder()
                        .bidderId(previousBidderId)
                        .bidAmount(previousBidAmount)
                        .build()));

        doNothing().when(walletServiceClient).holdFunds(any(HoldFundsRequest.class));
        doNothing().when(walletServiceClient).releaseFunds(any(ReleaseFundsRequest.class));

        auctionService.placeBid(auctionId, validBidRequest);

        verify(walletServiceClient).releaseFunds(argThat(req ->
                req.getUserId().equals(previousBidderId) &&
                req.getAmount().equals(previousBidAmount)
        ));
    }

    @Test
    void testCreateAuction_EndTimeBeforeStartTime_ThrowsIllegalArgumentException() {
        CreateAuctionRequest request = CreateAuctionRequest.builder()
                .listingId(UUID.randomUUID())
                .sellerId(UUID.randomUUID())
                .startingPrice(new BigDecimal("100.00"))
                .minimumIncrement(new BigDecimal("10.00"))
                .reservePrice(new BigDecimal("150.00"))
                .startTime(Instant.now().plusSeconds(3600))
                .endTime(Instant.now().plusSeconds(1800))
                .build();

        when(catalogueServiceClient.getListing(request.getListingId())).thenReturn(ListingSummaryResponse.builder()
                .id(request.getListingId().toString())
                .sellerId(request.getSellerId().toString())
                .status("ACTIVE")
                .build());

        assertThrows(IllegalArgumentException.class, () -> auctionService.createAuction(request));
        verify(auctionRepository, never()).save(any(Auction.class));
    }

    @Test
    void testCreateAuction_ReserveBelowStarting_ThrowsIllegalArgumentException() {
        CreateAuctionRequest request = CreateAuctionRequest.builder()
                .listingId(UUID.randomUUID())
                .sellerId(UUID.randomUUID())
                .startingPrice(new BigDecimal("200.00"))
                .minimumIncrement(new BigDecimal("10.00"))
                .reservePrice(new BigDecimal("150.00"))
                .startTime(Instant.now().plusSeconds(300))
                .endTime(Instant.now().plusSeconds(3600))
                .build();

        when(catalogueServiceClient.getListing(request.getListingId())).thenReturn(ListingSummaryResponse.builder()
                .id(request.getListingId().toString())
                .sellerId(request.getSellerId().toString())
                .status("ACTIVE")
                .build());

        assertThrows(IllegalArgumentException.class, () -> auctionService.createAuction(request));
        verify(auctionRepository, never()).save(any(Auction.class));
    }

    @Test
    void testCreateAuction_FutureStartTime_DefaultsToDraftStatus() {
        CreateAuctionRequest request = CreateAuctionRequest.builder()
                .listingId(UUID.randomUUID())
                .sellerId(UUID.randomUUID())
                .startingPrice(new BigDecimal("200.00"))
                .minimumIncrement(new BigDecimal("10.00"))
                .reservePrice(new BigDecimal("250.00"))
                .startTime(Instant.now().plusSeconds(600))
                .endTime(Instant.now().plusSeconds(3600))
                .build();

        when(catalogueServiceClient.getListing(request.getListingId())).thenReturn(ListingSummaryResponse.builder()
                .id(request.getListingId().toString())
                .sellerId(request.getSellerId().toString())
                .status("ACTIVE")
                .build());
        when(auctionRepository.save(any(Auction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Auction saved = auctionService.createAuction(request);

        assertEquals(AuctionStatus.DRAFT, saved.getStatus());
        verify(auctionRepository).save(any(Auction.class));
    }

    @Test
    void testCreateAuction_CurrentStartTime_DefaultsToActiveStatus() {
        CreateAuctionRequest request = CreateAuctionRequest.builder()
                .listingId(UUID.randomUUID())
                .sellerId(UUID.randomUUID())
                .startingPrice(new BigDecimal("200.00"))
                .minimumIncrement(new BigDecimal("10.00"))
                .reservePrice(new BigDecimal("250.00"))
                .startTime(Instant.now().minusSeconds(10))
                .endTime(Instant.now().plusSeconds(3600))
                .build();

        when(catalogueServiceClient.getListing(request.getListingId())).thenReturn(ListingSummaryResponse.builder()
                .id(request.getListingId().toString())
                .sellerId(request.getSellerId().toString())
                .status("ACTIVE")
                .build());
        when(auctionRepository.save(any(Auction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Auction saved = auctionService.createAuction(request);

        assertEquals(AuctionStatus.ACTIVE, saved.getStatus());
        verify(auctionRepository).save(any(Auction.class));
    }

    @Test
    void testPlaceBid_ListingNotActive_ThrowsIllegalStateException() {
        when(auctionRepository.findByIdWithPessimisticWriteLock(auctionId)).thenReturn(Optional.of(activeAuction));
        when(catalogueServiceClient.getListing(activeAuction.getListingId())).thenReturn(ListingSummaryResponse.builder()
                .id(activeAuction.getListingId().toString())
                .sellerId(activeAuction.getSellerId().toString())
                .status("CANCELLED")
                .build());

        assertThrows(IllegalStateException.class, () -> auctionService.placeBid(auctionId, validBidRequest));
    }

    @Test
    void testCreateAuction_SellerMismatch_ThrowsIllegalArgumentException() {
        CreateAuctionRequest request = CreateAuctionRequest.builder()
                .listingId(UUID.randomUUID())
                .sellerId(UUID.randomUUID())
                .startingPrice(new BigDecimal("200.00"))
                .minimumIncrement(new BigDecimal("10.00"))
                .reservePrice(new BigDecimal("250.00"))
                .startTime(Instant.now().minusSeconds(10))
                .endTime(Instant.now().plusSeconds(3600))
                .build();

        when(catalogueServiceClient.getListing(request.getListingId())).thenReturn(ListingSummaryResponse.builder()
                .id(request.getListingId().toString())
                .sellerId(UUID.randomUUID().toString())
                .status("ACTIVE")
                .build());

        assertThrows(IllegalArgumentException.class, () -> auctionService.createAuction(request));
        verify(auctionRepository, never()).save(any(Auction.class));
    }
}
