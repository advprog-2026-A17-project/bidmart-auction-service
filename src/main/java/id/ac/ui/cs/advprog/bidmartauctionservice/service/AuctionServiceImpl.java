package id.ac.ui.cs.advprog.bidmartauctionservice.service;

<<<<<<< Updated upstream
import id.ac.ui.cs.advprog.bidmartauctionservice.dto.BidRequestDTO;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.Auction;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.Bid;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.enums.AuctionStatus;
import id.ac.ui.cs.advprog.bidmartauctionservice.repository.AuctionRepository;
import id.ac.ui.cs.advprog.bidmartauctionservice.repository.BidRepository;
=======
import id.ac.ui.cs.advprog.bidmartauctionservice.model.Auction;
import id.ac.ui.cs.advprog.bidmartauctionservice.repository.AuctionRepository;
>>>>>>> Stashed changes
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

<<<<<<< Updated upstream
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
=======
import java.time.LocalDateTime;
import java.util.List;
>>>>>>> Stashed changes

@Service
@RequiredArgsConstructor
public class AuctionServiceImpl implements AuctionService {

    private final AuctionRepository auctionRepository;
<<<<<<< Updated upstream
    private final BidRepository bidRepository;

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
=======
>>>>>>> Stashed changes

    @Override
    @Transactional(readOnly = true)
    public List<Auction> getAllAuctions() {
        return auctionRepository.findAll();
    }
<<<<<<< Updated upstream
=======

    @Override
    @Transactional
    public Auction placeBid(Long auctionId, Double amount) {
        Auction auction = auctionRepository.findByIdWithLock(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction session not found"));

        if (!"ACTIVE".equals(auction.getStatus()) && !"EXTENDED".equals(auction.getStatus())) {
            throw new IllegalStateException("Auction is not accepting bids");
        }

        if (amount < (auction.getCurrentHighestBid() + auction.getMinIncrement())) {
            throw new IllegalArgumentException("Bid amount is too low");
        }

        // Anti-sniping: extend 2 minutes if bid is placed near end time
        if (auction.getEndTime().isBefore(LocalDateTime.now().plusMinutes(2))) {
            auction.setEndTime(auction.getEndTime().plusMinutes(2));
            auction.setStatus("EXTENDED");
        }

        auction.setCurrentHighestBid(amount);
        return auctionRepository.save(auction);
    }
>>>>>>> Stashed changes
}