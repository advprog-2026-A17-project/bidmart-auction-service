package id.ac.ui.cs.advprog.bidmartauctionservice.controller;

import id.ac.ui.cs.advprog.bidmartauctionservice.client.AuthServiceClient;
import id.ac.ui.cs.advprog.bidmartauctionservice.dto.AuctionPageResponseDTO;
import id.ac.ui.cs.advprog.bidmartauctionservice.dto.AuctionResponseDTO;
import id.ac.ui.cs.advprog.bidmartauctionservice.dto.BidRequestDTO;
import id.ac.ui.cs.advprog.bidmartauctionservice.dto.BidResponseDTO;
import id.ac.ui.cs.advprog.bidmartauctionservice.dto.CreateAuctionRequest;
import id.ac.ui.cs.advprog.bidmartauctionservice.exception.AuctionNotFoundException;
import id.ac.ui.cs.advprog.bidmartauctionservice.exception.PermissionDeniedException;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.Auction;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.Bid;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.enums.AuctionStatus;
import id.ac.ui.cs.advprog.bidmartauctionservice.service.AuctionService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auctions")
@RequiredArgsConstructor
@Validated
public class AuctionController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "createdAt", "startTime", "endTime", "currentHighestBid", "reservePrice", "updatedAt"
    );

    private final AuctionService auctionService;
    private final AuthServiceClient authServiceClient;

    @GetMapping
    public ResponseEntity<AuctionPageResponseDTO> getAuctions(
            @RequestParam(value = "status", required = false) AuctionStatus status,
            @RequestParam(value = "sellerId", required = false) UUID sellerId,
            @RequestParam(value = "listingId", required = false) UUID listingId,
            @RequestParam(value = "page", defaultValue = "0") @Min(0) int page,
            @RequestParam(value = "size", defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "direction", defaultValue = "desc") String direction) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(parseSortDirection(direction), parseSortBy(sortBy)));
        Page<Auction> auctions = auctionService.searchAuctions(status, sellerId, listingId, pageRequest);
        return ResponseEntity.ok(AuctionPageResponseDTO.builder()
                .items(auctions.getContent().stream().map(this::toAuctionResponse).toList())
                .page(auctions.getNumber())
                .size(auctions.getSize())
                .totalItems(auctions.getTotalElements())
                .totalPages(auctions.getTotalPages())
                .build());
    }

    @GetMapping("/{auctionId}")
    public ResponseEntity<AuctionResponseDTO> getAuctionById(@PathVariable UUID auctionId) {
        Auction auction = auctionService.getAuctionById(auctionId)
                .orElseThrow(() -> new AuctionNotFoundException("Auction not found with ID: " + auctionId));
        return ResponseEntity.ok(toAuctionResponse(auction));
    }

    @PostMapping
    public ResponseEntity<AuctionResponseDTO> createAuction(
            @RequestHeader(value = "X-User-Email", required = false) String userEmailHeader,
            @Valid @RequestBody CreateAuctionRequest requestDTO) {
        enforcePermission(userEmailHeader, "auction:create");
        Auction auction = auctionService.createAuction(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(toAuctionResponse(auction));
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

    private String parseSortBy(String sortBy) {
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException("Unsupported sortBy value: " + sortBy);
        }
        return sortBy;
    }

    private Sort.Direction parseSortDirection(String direction) {
        if ("asc".equalsIgnoreCase(direction)) {
            return Sort.Direction.ASC;
        }
        if ("desc".equalsIgnoreCase(direction)) {
            return Sort.Direction.DESC;
        }
        throw new IllegalArgumentException("Unsupported direction value: " + direction);
    }

    private AuctionResponseDTO toAuctionResponse(Auction auction) {
        return AuctionResponseDTO.builder()
                .id(auction.getId())
                .listingId(auction.getListingId())
                .sellerId(auction.getSellerId())
                .startingPrice(auction.getStartingPrice())
                .minimumIncrement(auction.getMinimumIncrement())
                .reservePrice(auction.getReservePrice())
                .currentHighestBid(auction.getCurrentHighestBid())
                .startTime(auction.getStartTime())
                .endTime(auction.getEndTime())
                .status(auction.getStatus())
                .build();
    }
}
