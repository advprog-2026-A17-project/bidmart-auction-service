package id.ac.ui.cs.advprog.bidmartauctionservice.service;

import id.ac.ui.cs.advprog.bidmartauctionservice.model.Auction;
import id.ac.ui.cs.advprog.bidmartauctionservice.repository.AuctionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuctionServiceTest {
    @Mock
    private AuctionRepository auctionRepository;

    @InjectMocks
    private AuctionService auctionService;

    private Auction auction;

    @BeforeEach
    void setUp() {
        auction = new Auction();
        auction.setId(1L);
        auction.setCurrentHighestBid(100.0);
        auction.setMinIncrement(10.0);
        auction.setEndTime(LocalDateTime.now().plusMinutes(10));
        auction.setStatus("ACTIVE");
    }

    @Test
    void testPlaceBidSuccess() {
        when(auctionRepository.findByIdWithLock(1L)).thenReturn(Optional.of(auction));
        when(auctionRepository.save(any(Auction.class))).thenReturn(auction);

        Auction result = auctionService.placeBid(1L, 120.0);

        assertEquals(120.0, result.getCurrentHighestBid());
        verify(auctionRepository, times(1)).save(auction);
    }

    @Test
    void testPlaceBidTooLowThrowsException() {
        when(auctionRepository.findByIdWithLock(1L)).thenReturn(Optional.of(auction));

        assertThrows(IllegalArgumentException.class, () -> {
            auctionService.placeBid(1L, 105.0);
        });
    }

    @Test
    void testAntiSnipingExtension() {
        auction.setEndTime(LocalDateTime.now().plusMinutes(1));
        when(auctionRepository.findByIdWithLock(1L)).thenReturn(Optional.of(auction));
        when(auctionRepository.save(any(Auction.class))).thenReturn(auction);

        LocalDateTime originalEndTime = auction.getEndTime();
        Auction result = auctionService.placeBid(1L, 200.0);

        assertTrue(result.getEndTime().isAfter(originalEndTime));
        assertEquals("EXTENDED", result.getStatus());
    }
}