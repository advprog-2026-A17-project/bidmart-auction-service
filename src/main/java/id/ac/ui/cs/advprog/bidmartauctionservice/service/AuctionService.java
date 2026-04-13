package id.ac.ui.cs.advprog.bidmartauctionservice.service;

import id.ac.ui.cs.advprog.bidmartauctionservice.dto.BidRequestDTO;
import id.ac.ui.cs.advprog.bidmartauctionservice.dto.CreateAuctionRequest;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.Auction;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.Bid;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.enums.AuctionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuctionService {
    Bid placeBid(UUID auctionId, BidRequestDTO requestDTO);
    List<Auction> getAllAuctions();
    Page<Auction> searchAuctions(AuctionStatus status, UUID sellerId, UUID listingId, Pageable pageable);
    Optional<Auction> getAuctionById(UUID auctionId);
    List<Bid> getBidHistoryByAuctionId(UUID auctionId);
    Auction createAuction(CreateAuctionRequest requestDTO);
}
