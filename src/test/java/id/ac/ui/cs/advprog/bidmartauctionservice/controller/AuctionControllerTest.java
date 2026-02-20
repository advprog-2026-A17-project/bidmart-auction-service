package id.ac.ui.cs.advprog.bidmartauctionservice.controller;

import id.ac.ui.cs.advprog.bidmartauctionservice.model.Auction;
import id.ac.ui.cs.advprog.bidmartauctionservice.repository.AuctionRepository;
import id.ac.ui.cs.advprog.bidmartauctionservice.service.AuctionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuctionController.class)
class AuctionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuctionService auctionService;

    @MockitoBean
    private AuctionRepository auctionRepository;

    @Test
    void testGetAllAuctions() throws Exception {
        Auction auction = new Auction();
        auction.setItemName("Test Item");
        auction.setCurrentHighestBid(100.0);
        when(auctionRepository.findAll()).thenReturn(Arrays.asList(auction));

        mockMvc.perform(get("/api/auctions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].itemName").value("Test Item"))
                .andExpect(jsonPath("$[0].currentHighestBid").value(100.0));
    }

    @Test
    void testPlaceBidEndpoint() throws Exception {
        Auction auction = new Auction();
        auction.setCurrentHighestBid(150.0);
        when(auctionService.placeBid(anyLong(), anyDouble())).thenReturn(auction);

        mockMvc.perform(post("/api/auctions/1/bid")
                        .param("amount", "150.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentHighestBid").value(150.0));
    }

    @Test
    void testPlaceBid_BadRequest() throws Exception {
        when(auctionService.placeBid(anyLong(), anyDouble()))
                .thenThrow(new IllegalArgumentException("Bid too low"));

        mockMvc.perform(post("/api/auctions/1/bid")
                        .param("amount", "10.0"))
                .andExpect(status().isBadRequest());
    }
}