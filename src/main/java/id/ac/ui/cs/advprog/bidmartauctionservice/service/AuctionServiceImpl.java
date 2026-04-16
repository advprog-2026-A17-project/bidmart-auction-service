package id.ac.ui.cs.advprog.bidmartauctionservice.service;

import id.ac.ui.cs.advprog.bidmartauctionservice.client.CatalogueServiceClient;
import id.ac.ui.cs.advprog.bidmartauctionservice.client.WalletServiceClient;
import id.ac.ui.cs.advprog.bidmartauctionservice.dto.BidRequestDTO;
import id.ac.ui.cs.advprog.bidmartauctionservice.dto.CreateAuctionRequest;
import id.ac.ui.cs.advprog.bidmartauctionservice.dto.catalogue.ListingSummaryResponse;
import id.ac.ui.cs.advprog.bidmartauctionservice.dto.wallet.HoldFundsRequest;
import id.ac.ui.cs.advprog.bidmartauctionservice.dto.wallet.ReleaseFundsRequest;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.Auction;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.Bid;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.enums.AuctionStatus;
import id.ac.ui.cs.advprog.bidmartauctionservice.repository.AuctionRepository;
import id.ac.ui.cs.advprog.bidmartauctionservice.repository.BidRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import id.ac.ui.cs.advprog.bidmartauctionservice.service.policy.AntiSnipingPolicy;
import id.ac.ui.cs.advprog.bidmartauctionservice.service.policy.WinningBidSelector;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuctionServiceImpl implements AuctionService {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final WalletServiceClient walletServiceClient;
    private final CatalogueServiceClient catalogueServiceClient;
    private final OutboxEventService outboxEventService;
    private final AntiSnipingPolicy antiSnipingPolicy;
    private final WinningBidSelector winningBidSelector;

    @Override
    @Transactional
    public Bid placeBid(UUID auctionId, BidRequestDTO requestDTO) {
        Instant now = Instant.now();

        Auction auction = auctionRepository.findByIdWithPessimisticWriteLock(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("Auction not found with ID: " + auctionId));
        requireActiveListing(auction.getListingId());

        if (auction.getStatus() != AuctionStatus.ACTIVE && auction.getStatus() != AuctionStatus.EXTENDED) {
            throw new IllegalStateException("Bids can only be placed on ACTIVE or EXTENDED auctions.");
        }
        if (now.isAfter(auction.getEndTime())) {
            throw new IllegalStateException("Auction has already ended.");
        }

        BigDecimal requiredMinimumBid;
        if (auction.getCurrentHighestBid() == null) {
            requiredMinimumBid = auction.getStartingPrice();
        } else {
            requiredMinimumBid = auction.getCurrentHighestBid().add(auction.getMinimumIncrement());
        }

        if (requestDTO.getBidAmount().compareTo(requiredMinimumBid) < 0) {
            throw new IllegalArgumentException("Bid amount must be at least " + requiredMinimumBid);
        }

        // Get previous highest bid if exists
        Optional<Bid> previousHighestBid = winningBidSelector.findWinningBid(auctionId);

        // Hold funds for new bidder from wallet service
        HoldFundsRequest holdRequest = HoldFundsRequest.builder()
                .userId(requestDTO.getBidderId())
                .amount(requestDTO.getBidAmount())
                .description("Bid placed on auction " + auctionId)
                .build();
        walletServiceClient.holdFunds(holdRequest);

        // Release funds for previous bidder if there was one
        if (previousHighestBid.isPresent()) {
            ReleaseFundsRequest releaseRequest = ReleaseFundsRequest.builder()
                    .userId(previousHighestBid.get().getBidderId())
                    .amount(previousHighestBid.get().getBidAmount())
                    .description("Outbid on auction " + auctionId)
                    .build();
            walletServiceClient.releaseFunds(releaseRequest);
        }

        antiSnipingPolicy.applyForBid(auction, now);

        auction.setCurrentHighestBid(requestDTO.getBidAmount());
        auctionRepository.save(auction);

        Bid bid = Bid.builder()
                .auction(auction)
                .bidderId(requestDTO.getBidderId())
                .bidAmount(requestDTO.getBidAmount())
                .bidTime(now)
                .build();

        Bid savedBid = bidRepository.save(bid);
        outboxEventService.enqueueBidPlaced(savedBid.getId());
        return savedBid;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Auction> getAllAuctions() {
        return auctionRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Auction> searchAuctions(AuctionStatus status, UUID sellerId, UUID listingId, Pageable pageable) {
        Specification<Auction> specification = (root, query, cb) -> cb.conjunction();

        if (status != null) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (sellerId != null) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("sellerId"), sellerId));
        }
        if (listingId != null) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("listingId"), listingId));
        }

        return auctionRepository.findAll(specification, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Auction> getAuctionById(UUID auctionId) {
        return auctionRepository.findById(auctionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Bid> getBidHistoryByAuctionId(UUID auctionId) {
        return bidRepository.findByAuctionIdOrderByBidTimeDesc(auctionId);
    }

    @Override
    @Transactional
    public Auction createAuction(CreateAuctionRequest requestDTO) {
        ListingSummaryResponse listing = requireActiveListing(requestDTO.getListingId());
        if (!requestDTO.getSellerId().toString().equals(listing.getSellerId())) {
            throw new IllegalArgumentException("Listing seller does not match auction seller");
        }

        if (!requestDTO.getEndTime().isAfter(requestDTO.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        if (requestDTO.getReservePrice().compareTo(requestDTO.getStartingPrice()) < 0) {
            throw new IllegalArgumentException("Reserve price must be greater than or equal to starting price");
        }
        if (requestDTO.getStartingPrice().compareTo(BigDecimal.ZERO) <= 0 ||
            requestDTO.getMinimumIncrement().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Starting price and minimum increment must be greater than zero");
        }

        AuctionStatus initialStatus = requestDTO.getStartTime().isAfter(Instant.now())
                ? AuctionStatus.DRAFT
                : AuctionStatus.ACTIVE;

        Auction auction = Auction.builder()
                .listingId(requestDTO.getListingId())
                .sellerId(requestDTO.getSellerId())
                .startingPrice(requestDTO.getStartingPrice())
                .minimumIncrement(requestDTO.getMinimumIncrement())
                .reservePrice(requestDTO.getReservePrice())
                .startTime(requestDTO.getStartTime())
                .endTime(requestDTO.getEndTime())
                .status(initialStatus)
                .build();

        return auctionRepository.save(auction);
    }

    private ListingSummaryResponse requireActiveListing(UUID listingId) {
        ListingSummaryResponse listing = catalogueServiceClient.getListing(listingId);
        if (listing.getStatus() == null || !"ACTIVE".equalsIgnoreCase(listing.getStatus())) {
            throw new IllegalStateException("Listing is not active");
        }
        return listing;
    }
}
