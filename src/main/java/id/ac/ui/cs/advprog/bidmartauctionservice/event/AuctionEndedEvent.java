package id.ac.ui.cs.advprog.bidmartauctionservice.event;

import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.Auction;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.enums.AuctionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
@AllArgsConstructor
public class AuctionEndedEvent extends ApplicationEvent {
    private final Auction auction;
    private final AuctionStatus finalStatus;

    public AuctionEndedEvent(Object source, Auction auction, AuctionStatus finalStatus) {
        super(source);
        this.auction = auction;
        this.finalStatus = finalStatus;
    }
}
