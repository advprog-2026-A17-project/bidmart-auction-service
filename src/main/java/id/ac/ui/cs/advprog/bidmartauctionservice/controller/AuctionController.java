package id.ac.ui.cs.advprog.bidmartauctionservice.controller;

import id.ac.ui.cs.advprog.bidmartauctionservice.model.Auction;
import id.ac.ui.cs.advprog.bidmartauctionservice.service.AuctionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auctions")
@RequiredArgsConstructor
public class AuctionController {
    private final AuctionService auctionService;

    @PostMapping("/{id}/bid")
    public ResponseEntity<Auction> bid(@PathVariable Long id, @RequestParam Double amount) {
        return ResponseEntity.ok(auctionService.placeBid(id, amount));
    }
}