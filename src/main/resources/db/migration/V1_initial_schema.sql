CREATE TABLE auctions (
                          id BIGSERIAL PRIMARY KEY,
                          item_id VARCHAR(255) NOT NULL,
                          item_name VARCHAR(255) NOT NULL,
                          current_highest_bid DOUBLE PRECISION DEFAULT 0.0,
                          min_increment DOUBLE PRECISION NOT NULL,
                          reserve_price DOUBLE PRECISION NOT NULL,
                          start_time TIMESTAMP NOT NULL,
                          end_time TIMESTAMP NOT NULL,
                          status VARCHAR(50) NOT NULL -- DRAFT, ACTIVE, EXTENDED, CLOSED
);

CREATE TABLE bids (
                      id BIGSERIAL PRIMARY KEY,
                      auction_id BIGINT REFERENCES auctions(id),
                      user_id VARCHAR(255) NOT NULL,
                      amount DOUBLE PRECISION NOT NULL,
                      bid_time TIMESTAMP NOT NULL
);