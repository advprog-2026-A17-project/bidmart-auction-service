package id.ac.ui.cs.advprog.bidmartauctionservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    @Test
    void testGetAllAuctions() throws Exception {
        Auction auction = Auction.builder()
                .id(UUID.randomUUID())
                .listingId(UUID.randomUUID())
                .currentHighestBid(new BigDecimal("100.00"))
                .status(AuctionStatus.ACTIVE)
                .build();

        when(auctionService.getAllAuctions()).thenReturn(Arrays.asList(auction));

        mockMvc.perform(get("/api/v1/auctions")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].currentHighestBid").value(100.0))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
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

        when(auctionService.placeBid(eq(auctionId), any(BidRequestDTO.class))).thenReturn(bid);

        mockMvc.perform(post("/api/v1/auctions/{auctionId}/bids", auctionId)
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
                .bidderId(UUID.randomUUID())
                .build();

        mockMvc.perform(post("/api/v1/auctions/{auctionId}/bids", auctionId)
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

        when(auctionService.createAuction(any(CreateAuctionRequest.class))).thenReturn(auction);

        mockMvc.perform(post("/api/v1/auctions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.listingId").value(listingId.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }
}