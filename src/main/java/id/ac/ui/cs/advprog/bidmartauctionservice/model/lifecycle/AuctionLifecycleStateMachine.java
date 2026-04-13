package id.ac.ui.cs.advprog.bidmartauctionservice.model.lifecycle;

import id.ac.ui.cs.advprog.bidmartauctionservice.model.enums.AuctionStatus;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class AuctionLifecycleStateMachine {
    private static final Map<AuctionStatus, Set<AuctionStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(AuctionStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(AuctionStatus.DRAFT, EnumSet.of(AuctionStatus.ACTIVE));
        ALLOWED_TRANSITIONS.put(AuctionStatus.ACTIVE, EnumSet.of(AuctionStatus.EXTENDED, AuctionStatus.CLOSED));
        ALLOWED_TRANSITIONS.put(AuctionStatus.EXTENDED, EnumSet.of(AuctionStatus.CLOSED));
        ALLOWED_TRANSITIONS.put(AuctionStatus.CLOSED, EnumSet.of(AuctionStatus.WON, AuctionStatus.UNSOLD));
        ALLOWED_TRANSITIONS.put(AuctionStatus.WON, EnumSet.noneOf(AuctionStatus.class));
        ALLOWED_TRANSITIONS.put(AuctionStatus.UNSOLD, EnumSet.noneOf(AuctionStatus.class));
    }

    private AuctionLifecycleStateMachine() {
    }

    public static boolean canTransition(AuctionStatus from, AuctionStatus to) {
        return ALLOWED_TRANSITIONS.getOrDefault(from, EnumSet.noneOf(AuctionStatus.class)).contains(to);
    }

    public static void enforceTransition(AuctionStatus from, AuctionStatus to) {
        if (!canTransition(from, to)) {
            throw new IllegalStateException("Invalid auction status transition: " + from + " -> " + to);
        }
    }

    public static boolean isTerminal(AuctionStatus status) {
        return status == AuctionStatus.WON || status == AuctionStatus.UNSOLD;
    }
}
