package id.ac.ui.cs.advprog.bidmartauctionservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.bidmartauctionservice.dto.BidRequestDTO;
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
import java.util.Arrays;
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
}