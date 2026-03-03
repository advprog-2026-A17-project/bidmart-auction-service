package id.ac.ui.cs.advprog.bidmartauctionservice.service;

import id.ac.ui.cs.advprog.bidmartauctionservice.dto.BidRequestDTO;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.Bid;

import java.util.UUID;

public interface AuctionService {
    Bid placeBid(UUID auctionId, BidRequestDTO requestDTO);
}