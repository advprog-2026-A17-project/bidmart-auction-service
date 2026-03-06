package id.ac.ui.cs.advprog.bidmartauctionservice.service;

import id.ac.ui.cs.advprog.bidmartauctionservice.model.Auction;
import id.ac.ui.cs.advprog.bidmartauctionservice.repository.AuctionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuctionServiceImpl implements AuctionService {

    private final AuctionRepository auctionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Auction> getAllAuctions() {
        return auctionRepository.findAll();
    }

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
}