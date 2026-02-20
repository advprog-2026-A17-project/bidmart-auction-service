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

    private Auction activeAuction;

    @BeforeEach
    void setUp() {
        activeAuction = new Auction(1L, "ITM-1", "Item A", 100.0, 10.0, 500.0,
                LocalDateTime.now(), LocalDateTime.now().plusHours(1), "ACTIVE");
    }

    @Test
    void testPlaceBid_Success() {
        when(auctionRepository.findByIdWithLock(1L)).thenReturn(Optional.of(activeAuction));
        when(auctionRepository.save(any(Auction.class))).thenReturn(activeAuction);

        Auction result = auctionService.placeBid(1L, 150.0);

        assertEquals(150.0, result.getCurrentHighestBid());
        verify(auctionRepository).save(activeAuction);
    }

    @Test
    void testPlaceBid_AuctionNotFound_ThrowsRuntimeException() {
        when(auctionRepository.findByIdWithLock(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> auctionService.placeBid(99L, 200.0),
                "Auction session not found");
    }

    @Test
    void testPlaceBid_InvalidStatus_ThrowsIllegalStateException() {
        activeAuction.setStatus("CLOSED");
        when(auctionRepository.findByIdWithLock(1L)).thenReturn(Optional.of(activeAuction));

        assertThrows(IllegalStateException.class, () -> auctionService.placeBid(1L, 200.0),
                "Auction is not accepting bids");
    }

    @Test
    void testPlaceBid_TooLowAmount_ThrowsIllegalArgumentException() {
        when(auctionRepository.findByIdWithLock(1L)).thenReturn(Optional.of(activeAuction));

        assertThrows(IllegalArgumentException.class, () -> auctionService.placeBid(1L, 105.0));
    }

    @Test
    void testPlaceBid_AntiSnipingExtension() {
        activeAuction.setEndTime(LocalDateTime.now().plusMinutes(1));
        when(auctionRepository.findByIdWithLock(1L)).thenReturn(Optional.of(activeAuction));
        when(auctionRepository.save(any(Auction.class))).thenAnswer(i -> i.getArguments()[0]);

        Auction result = auctionService.placeBid(1L, 200.0);

        assertEquals("EXTENDED", result.getStatus());
        assertTrue(result.getEndTime().isAfter(LocalDateTime.now().plusMinutes(2)));
    }
}