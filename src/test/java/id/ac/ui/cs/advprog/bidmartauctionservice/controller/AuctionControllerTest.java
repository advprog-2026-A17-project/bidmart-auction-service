package id.ac.ui.cs.advprog.bidmartauctionservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.bidmartauctionservice.client.AuthServiceClient;
import id.ac.ui.cs.advprog.bidmartauctionservice.dto.BidRequestDTO;
import id.ac.ui.cs.advprog.bidmartauctionservice.dto.CreateAuctionRequest;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.Auction;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.Bid;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.enums.AuctionStatus;
import id.ac.ui.cs.advprog.bidmartauctionservice.service.AuctionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuctionController.class)
class AuctionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuctionService auctionService;

    @MockitoBean
    private AuthServiceClient authServiceClient;

    @Test
    void testGetAuctions_WithPaginationAndFilters() throws Exception {
        Auction auction = Auction.builder()
                .id(UUID.randomUUID())
                .listingId(UUID.randomUUID())
                .currentHighestBid(new BigDecimal("100.00"))
                .sellerId(UUID.randomUUID())
                .startingPrice(new BigDecimal("50.00"))
                .minimumIncrement(new BigDecimal("10.00"))
                .reservePrice(new BigDecimal("200.00"))
                .startTime(Instant.now().minusSeconds(3600))
                .endTime(Instant.now().plusSeconds(3600))
                .status(AuctionStatus.ACTIVE)
                .build();
        Page<Auction> page = new PageImpl<>(List.of(auction), PageRequest.of(0, 10), 1);

        when(auctionService.searchAuctions(any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/auctions")
                        .param("status", "ACTIVE")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].currentHighestBid").value(100.0))
                .andExpect(jsonPath("$.items[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalItems").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void testGetAuctionById_Success() throws Exception {
        UUID auctionId = UUID.randomUUID();
        Auction auction = Auction.builder()
                .id(auctionId)
                .listingId(UUID.randomUUID())
                .sellerId(UUID.randomUUID())
                .startingPrice(new BigDecimal("100.00"))
                .minimumIncrement(new BigDecimal("10.00"))
                .reservePrice(new BigDecimal("500.00"))
                .startTime(Instant.now())
                .endTime(Instant.now().plusSeconds(3600))
                .status(AuctionStatus.ACTIVE)
                .build();

        when(auctionService.getAuctionById(auctionId)).thenReturn(Optional.of(auction));

        mockMvc.perform(get("/api/v1/auctions/{auctionId}", auctionId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(auctionId.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void testGetAuctionById_NotFound() throws Exception {
        UUID auctionId = UUID.randomUUID();
        when(auctionService.getAuctionById(auctionId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/auctions/{auctionId}", auctionId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("AUCTION_NOT_FOUND"))
                .andExpect(jsonPath("$.path").value("/api/v1/auctions/" + auctionId));
    }

    @Test
    void testGetAuctions_InvalidPageSizeReturnsValidationError() throws Exception {
        mockMvc.perform(get("/api/v1/auctions")
                        .param("size", "0")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.path").value("/api/v1/auctions"));
    }

    @Test
    void testPlaceBidEndpoint_Success() throws Exception {
        UUID auctionId = UUID.randomUUID();
        UUID bidderId = UUID.randomUUID();

        BidRequestDTO requestDTO = BidRequestDTO.builder()
                .bidderId(bidderId)
                .bidAmount(new BigDecimal("150.00"))
                .build();

        Auction auction = Auction.builder().id(auctionId).build();
        Bid bid = Bid.builder()
                .id(UUID.randomUUID())
                .auction(auction)
                .bidderId(bidderId)
                .bidAmount(new BigDecimal("150.00"))
                .bidTime(Instant.now())
                .build();

        when(authServiceClient.hasPermission("buyer@example.com", "bid:place")).thenReturn(true);
        when(auctionService.placeBid(eq(auctionId), any(BidRequestDTO.class))).thenReturn(bid);

        mockMvc.perform(post("/api/v1/auctions/{auctionId}/bids", auctionId)
                        .header("X-User-Email", "buyer@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bidAmount").value(150.0))
                .andExpect(jsonPath("$.bidderId").value(bidderId.toString()));
    }

    @Test
    void testPlaceBidEndpoint_ValidationFailed() throws Exception {
        UUID auctionId = UUID.randomUUID();

        BidRequestDTO requestDTO = BidRequestDTO.builder()
                .build();

        mockMvc.perform(post("/api/v1/auctions/{auctionId}/bids", auctionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testPlaceBidEndpoint_UsesIdentityHeaderWhenPayloadBidderMissing() throws Exception {
        UUID auctionId = UUID.randomUUID();
        UUID bidderId = UUID.randomUUID();

        BidRequestDTO requestDTO = BidRequestDTO.builder()
                .bidAmount(new BigDecimal("150.00"))
                .build();

        Auction auction = Auction.builder().id(auctionId).build();
        Bid bid = Bid.builder()
                .id(UUID.randomUUID())
                .auction(auction)
                .bidderId(bidderId)
                .bidAmount(new BigDecimal("150.00"))
                .bidTime(Instant.now())
                .build();

        when(auctionService.placeBid(eq(auctionId), any(BidRequestDTO.class))).thenReturn(bid);
        when(authServiceClient.hasPermission("buyer@example.com", "bid:place")).thenReturn(true);

        mockMvc.perform(post("/api/v1/auctions/{auctionId}/bids", auctionId)
                        .header("X-User-Id", bidderId.toString())
                        .header("X-User-Email", "buyer@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bidderId").value(bidderId.toString()));

        verify(auctionService).placeBid(eq(auctionId), argThat(dto -> bidderId.equals(dto.getBidderId())));
    }

    @Test
    void testPlaceBidEndpoint_BidderIdentityMismatchReturnsBadRequest() throws Exception {
        UUID auctionId = UUID.randomUUID();

        BidRequestDTO requestDTO = BidRequestDTO.builder()
                .bidderId(UUID.randomUUID())
                .bidAmount(new BigDecimal("150.00"))
                .build();

        mockMvc.perform(post("/api/v1/auctions/{auctionId}/bids", auctionId)
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Email", "buyer@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetBidHistory_Success() throws Exception {
        UUID auctionId = UUID.randomUUID();
        UUID bidderId1 = UUID.randomUUID();
        UUID bidderId2 = UUID.randomUUID();

        Instant now = Instant.now();
        Auction auction = Auction.builder().id(auctionId).build();

        List<Bid> bids = new ArrayList<>();
        bids.add(Bid.builder()
                .id(UUID.randomUUID())
                .auction(auction)
                .bidderId(bidderId2)
                .bidAmount(new BigDecimal("200.00"))
                .bidTime(now)
                .build());
        bids.add(Bid.builder()
                .id(UUID.randomUUID())
                .auction(auction)
                .bidderId(bidderId1)
                .bidAmount(new BigDecimal("150.00"))
                .bidTime(now.minusSeconds(60))
                .build());

        when(auctionService.getBidHistoryByAuctionId(auctionId)).thenReturn(bids);

        mockMvc.perform(get("/api/v1/auctions/{auctionId}/bids", auctionId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].bidAmount").value(200.0))
                .andExpect(jsonPath("$[1].bidAmount").value(150.0));
    }

    @Test
    void testCreateAuction_Success() throws Exception {
        UUID auctionId = UUID.randomUUID();
        UUID listingId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();

        CreateAuctionRequest requestDTO = CreateAuctionRequest.builder()
                .listingId(listingId)
                .sellerId(sellerId)
                .startingPrice(new BigDecimal("100.00"))
                .minimumIncrement(new BigDecimal("10.00"))
                .reservePrice(new BigDecimal("500.00"))
                .startTime(Instant.now())
                .endTime(Instant.now().plusSeconds(3600))
                .build();

        Auction auction = Auction.builder()
                .id(auctionId)
                .listingId(listingId)
                .sellerId(sellerId)
                .startingPrice(new BigDecimal("100.00"))
                .minimumIncrement(new BigDecimal("10.00"))
                .reservePrice(new BigDecimal("500.00"))
                .startTime(Instant.now())
                .endTime(Instant.now().plusSeconds(3600))
                .status(AuctionStatus.ACTIVE)
                .build();

        when(authServiceClient.hasPermission("seller@example.com", "auction:create")).thenReturn(true);
        when(auctionService.createAuction(any(CreateAuctionRequest.class))).thenReturn(auction);

        mockMvc.perform(post("/api/v1/auctions")
                        .header("X-User-Email", "seller@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.listingId").value(listingId.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void testPlaceBidEndpoint_ForbiddenWhenPermissionDenied() throws Exception {
        UUID auctionId = UUID.randomUUID();
        UUID bidderId = UUID.randomUUID();

        BidRequestDTO requestDTO = BidRequestDTO.builder()
                .bidderId(bidderId)
                .bidAmount(new BigDecimal("150.00"))
                .build();

        when(authServiceClient.hasPermission("buyer@example.com", "bid:place")).thenReturn(false);

        mockMvc.perform(post("/api/v1/auctions/{auctionId}/bids", auctionId)
                        .header("X-User-Email", "buyer@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateAuction_ForbiddenWhenPermissionDenied() throws Exception {
        CreateAuctionRequest requestDTO = CreateAuctionRequest.builder()
                .listingId(UUID.randomUUID())
                .sellerId(UUID.randomUUID())
                .startingPrice(new BigDecimal("100.00"))
                .minimumIncrement(new BigDecimal("10.00"))
                .reservePrice(new BigDecimal("500.00"))
                .startTime(Instant.now())
                .endTime(Instant.now().plusSeconds(3600))
                .build();

        when(authServiceClient.hasPermission("seller@example.com", "auction:create")).thenReturn(false);

        mockMvc.perform(post("/api/v1/auctions")
                        .header("X-User-Email", "seller@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isForbidden());
    }
}
