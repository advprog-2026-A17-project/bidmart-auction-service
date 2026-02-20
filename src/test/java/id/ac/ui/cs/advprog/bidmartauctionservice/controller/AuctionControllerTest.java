package id.ac.ui.cs.advprog.bidmartauctionservice.controller;

import id.ac.ui.cs.advprog.bidmartauctionservice.model.Auction;
import id.ac.ui.cs.advprog.bidmartauctionservice.service.AuctionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuctionController.class)
class AuctionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuctionService auctionService;

    @Test
    void testPostBidEndpoint() throws Exception {
        Auction auction = new Auction();
        auction.setId(1L);
        auction.setCurrentHighestBid(200.0);

        when(auctionService.placeBid(1L, 200.0)).thenReturn(auction);

        mockMvc.perform(post("/api/auctions/1/bid")
                        .param("amount", "200.0"))
                .andExpect(status().isOk());
    }
}