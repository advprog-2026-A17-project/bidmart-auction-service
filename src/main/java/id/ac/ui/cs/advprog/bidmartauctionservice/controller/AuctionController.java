package id.ac.ui.cs.advprog.bidmartauctionservice.controller;

import id.ac.ui.cs.advprog.bidmartauctionservice.client.AuthServiceClient;
import id.ac.ui.cs.advprog.bidmartauctionservice.dto.BidRequestDTO;
import id.ac.ui.cs.advprog.bidmartauctionservice.dto.BidResponseDTO;
import id.ac.ui.cs.advprog.bidmartauctionservice.dto.CreateAuctionRequest;
import id.ac.ui.cs.advprog.bidmartauctionservice.exception.PermissionDeniedException;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.Auction;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.Bid;
import id.ac.ui.cs.advprog.bidmartauctionservice.service.AuctionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auctions")
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionService auctionService;
    private final AuthServiceClient authServiceClient;

    @GetMapping
    public ResponseEntity<List<Auction>> getAllAuctions() {
        return ResponseEntity.ok(auctionService.getAllAuctions());
    }

    @PostMapping
    public ResponseEntity<Auction> createAuction(
            @RequestHeader(value = "X-User-Email", required = false) String userEmailHeader,
            @Valid @RequestBody CreateAuctionRequest requestDTO) {
        enforcePermission(userEmailHeader, "auction:create");
        Auction auction = auctionService.createAuction(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(auction);
    }

    @PostMapping("/{auctionId}/bids")
    public ResponseEntity<BidResponseDTO> placeBid(
            @PathVariable UUID auctionId,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmailHeader,
            @Valid @RequestBody BidRequestDTO requestDTO) {
        requestDTO.setBidderId(resolveBidderId(userIdHeader, requestDTO.getBidderId()));
        enforcePermission(userEmailHeader, "bid:place");

        Bid bid = auctionService.placeBid(auctionId, requestDTO);

        BidResponseDTO responseDTO = BidResponseDTO.builder()
                .id(bid.getId())
                .auctionId(bid.getAuction().getId())
                .bidderId(bid.getBidderId())
                .bidAmount(bid.getBidAmount())
                .bidTime(bid.getBidTime())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @GetMapping("/{auctionId}/bids")
    public ResponseEntity<List<BidResponseDTO>> getBidHistory(@PathVariable UUID auctionId) {
        List<Bid> bids = auctionService.getBidHistoryByAuctionId(auctionId);

        List<BidResponseDTO> responseDTOs = bids.stream()
                .map(bid -> BidResponseDTO.builder()
                        .id(bid.getId())
                        .auctionId(bid.getAuction().getId())
                        .bidderId(bid.getBidderId())
                        .bidAmount(bid.getBidAmount())
                        .bidTime(bid.getBidTime())
                        .build())
                .toList();

        return ResponseEntity.ok(responseDTOs);
    }

    private UUID resolveBidderId(String userIdHeader, UUID payloadBidderId) {
        UUID headerBidderId = null;
        if (userIdHeader != null && !userIdHeader.isBlank()) {
            try {
                headerBidderId = UUID.fromString(userIdHeader);
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Invalid X-User-Id header");
            }
        }

        if (headerBidderId != null && payloadBidderId != null && !headerBidderId.equals(payloadBidderId)) {
            throw new IllegalArgumentException("Bidder identity mismatch between payload and header");
        }

        UUID effectiveBidderId = headerBidderId != null ? headerBidderId : payloadBidderId;
        if (effectiveBidderId == null) {
            throw new IllegalArgumentException("Bidder identity is required");
        }
        return effectiveBidderId;
    }

    private void enforcePermission(String userEmailHeader, String permission) {
        if (userEmailHeader == null || userEmailHeader.isBlank()) {
            throw new IllegalArgumentException("X-User-Email header is required");
        }
        boolean allowed = authServiceClient.hasPermission(userEmailHeader, permission);
        if (!allowed) {
            throw new PermissionDeniedException("Permission denied: " + permission);
        }
    }
}
