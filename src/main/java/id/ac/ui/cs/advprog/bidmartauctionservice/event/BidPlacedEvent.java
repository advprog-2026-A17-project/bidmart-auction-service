package id.ac.ui.cs.advprog.bidmartauctionservice.event;

import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.Bid;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class BidPlacedEvent extends ApplicationEvent {
    private final Bid bid;

    public BidPlacedEvent(Object source, Bid bid) {
        super(source);
        this.bid = bid;
    }
}
