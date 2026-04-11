package id.ac.ui.cs.advprog.bidmartauctionservice.service;

import id.ac.ui.cs.advprog.bidmartauctionservice.client.WalletServiceClient;
import id.ac.ui.cs.advprog.bidmartauctionservice.dto.BidRequestDTO;
import id.ac.ui.cs.advprog.bidmartauctionservice.dto.CreateAuctionRequest;
import id.ac.ui.cs.advprog.bidmartauctionservice.dto.wallet.HoldFundsRequest;
import id.ac.ui.cs.advprog.bidmartauctionservice.dto.wallet.ReleaseFundsRequest;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.Auction;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.Bid;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.enums.AuctionStatus;
import id.ac.ui.cs.advprog.bidmartauctionservice.repository.AuctionRepository;
import id.ac.ui.cs.advprog.bidmartauctionservice.repository.BidRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Duration;
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

    @Override
    @Transactional
    public Bid placeBid(UUID auctionId, BidRequestDTO requestDTO) {
        Instant now = Instant.now();

        Auction auction = auctionRepository.findByIdWithPessimisticWriteLock(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("Auction not found with ID: " + auctionId));

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
        Optional<Bid> previousHighestBid = bidRepository.findFirstByAuctionIdOrderByBidAmountDesc(auctionId);

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

        // Anti-sniping: extend 2 minutes if bid is placed near end time
        Duration remainingTime = Duration.between(now, auction.getEndTime());
        if (remainingTime.toMinutes() < 2) {
            auction.setEndTime(now.plus(Duration.ofMinutes(2)));
            auction.setStatus(AuctionStatus.EXTENDED);
        }

        auction.setCurrentHighestBid(requestDTO.getBidAmount());
        auctionRepository.save(auction);

        Bid bid = Bid.builder()
                .auction(auction)
                .bidderId(requestDTO.getBidderId())
                .bidAmount(requestDTO.getBidAmount())
                .bidTime(now)
                .build();

        return bidRepository.save(bid);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Auction> getAllAuctions() {
        return auctionRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Bid> getBidHistoryByAuctionId(UUID auctionId) {
        return bidRepository.findByAuctionIdOrderByBidTimeDesc(auctionId);
    }

    @Override
    @Transactional
    public Auction createAuction(CreateAuctionRequest requestDTO) {
        Auction auction = Auction.builder()
                .listingId(requestDTO.getListingId())
                .sellerId(requestDTO.getSellerId())
                .startingPrice(requestDTO.getStartingPrice())
                .minimumIncrement(requestDTO.getMinimumIncrement())
                .reservePrice(requestDTO.getReservePrice())
                .startTime(requestDTO.getStartTime())
                .endTime(requestDTO.getEndTime())
                .status(AuctionStatus.ACTIVE)
                .build();

        return auctionRepository.save(auction);
    }
}