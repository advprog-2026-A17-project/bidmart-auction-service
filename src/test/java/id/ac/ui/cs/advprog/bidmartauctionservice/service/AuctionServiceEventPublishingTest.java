package id.ac.ui.cs.advprog.bidmartauctionservice.service;

import id.ac.ui.cs.advprog.bidmartauctionservice.client.CatalogueServiceClient;
import id.ac.ui.cs.advprog.bidmartauctionservice.client.WalletServiceClient;
import id.ac.ui.cs.advprog.bidmartauctionservice.dto.BidRequestDTO;
import id.ac.ui.cs.advprog.bidmartauctionservice.dto.catalogue.ListingSummaryResponse;
import id.ac.ui.cs.advprog.bidmartauctionservice.dto.wallet.HoldFundsRequest;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.Auction;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.Bid;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.enums.AuctionStatus;
import id.ac.ui.cs.advprog.bidmartauctionservice.repository.AuctionRepository;
import id.ac.ui.cs.advprog.bidmartauctionservice.repository.BidRepository;
import id.ac.ui.cs.advprog.bidmartauctionservice.service.policy.AntiSnipingPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuctionServiceEventPublishingTest {

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
    private UUID newBidderId;

    @BeforeEach
    void setUp() {
        auctionId = UUID.randomUUID();
        newBidderId = UUID.randomUUID();

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
    }

    @Test
    void testPlaceBid_EnqueuesBidPlacedOutboxEvent() {
        BidRequestDTO requestDTO = BidRequestDTO.builder()
                .bidderId(newBidderId)
                .bidAmount(new BigDecimal("200.00"))
                .build();

        when(auctionRepository.findByIdWithPessimisticWriteLock(auctionId))
                .thenReturn(Optional.of(activeAuction));
        when(catalogueServiceClient.getListing(activeAuction.getListingId())).thenReturn(ListingSummaryResponse.builder()
                .id(activeAuction.getListingId().toString())
                .sellerId(activeAuction.getSellerId().toString())
                .status("ACTIVE")
                .build());

        Bid savedBid = Bid.builder()
                .id(UUID.randomUUID())
                .auction(activeAuction)
                .bidderId(newBidderId)
                .bidAmount(new BigDecimal("200.00"))
                .bidTime(Instant.now())
                .build();

        when(bidRepository.save(any(Bid.class))).thenReturn(savedBid);
        when(bidRepository.findFirstByAuctionIdOrderByBidAmountDescBidTimeAsc(auctionId))
                .thenReturn(Optional.empty());
        doNothing().when(walletServiceClient).holdFunds(any(HoldFundsRequest.class));

        auctionService.placeBid(auctionId, requestDTO);

        verify(outboxEventService).enqueueBidPlaced(savedBid.getId());
    }
}
