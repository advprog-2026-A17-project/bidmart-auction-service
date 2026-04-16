package id.ac.ui.cs.advprog.bidmartauctionservice.service.policy;

import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.Bid;
import id.ac.ui.cs.advprog.bidmartauctionservice.repository.BidRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RepositoryWinningBidSelector implements WinningBidSelector {

    private final BidRepository bidRepository;

    @Override
    public Optional<Bid> findWinningBid(UUID auctionId) {
        return bidRepository.findFirstByAuctionIdOrderByBidAmountDescBidTimeAsc(auctionId);
    }
}
