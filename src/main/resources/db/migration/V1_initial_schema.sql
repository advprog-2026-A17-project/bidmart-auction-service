CREATE TABLE auctions (
    id UUID PRIMARY KEY,
    listing_id UUID NOT NULL,
    seller_id UUID NOT NULL,
    starting_price NUMERIC(19, 4) NOT NULL,
    reserve_price NUMERIC(19, 4) NOT NULL,
    current_highest_bid NUMERIC(19, 4),
    minimum_increment NUMERIC(19, 4) NOT NULL,
    status VARCHAR(50) NOT NULL,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE bids (
    id UUID PRIMARY KEY,
    auction_id UUID NOT NULL REFERENCES auctions(id),
    bidder_id UUID NOT NULL,
    bid_amount NUMERIC(19, 4) NOT NULL,
    bid_time TIMESTAMP WITH TIME ZONE NOT NULL
);