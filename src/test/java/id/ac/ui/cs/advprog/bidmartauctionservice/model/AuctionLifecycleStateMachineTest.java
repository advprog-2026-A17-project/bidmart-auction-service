package id.ac.ui.cs.advprog.bidmartauctionservice.model;

import id.ac.ui.cs.advprog.bidmartauctionservice.model.enums.AuctionStatus;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.lifecycle.AuctionLifecycleStateMachine;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuctionLifecycleStateMachineTest {

    @Test
    void testAllowsExpectedTransitions() {
        assertTrue(AuctionLifecycleStateMachine.canTransition(AuctionStatus.DRAFT, AuctionStatus.ACTIVE));
        assertTrue(AuctionLifecycleStateMachine.canTransition(AuctionStatus.ACTIVE, AuctionStatus.EXTENDED));
        assertTrue(AuctionLifecycleStateMachine.canTransition(AuctionStatus.ACTIVE, AuctionStatus.CLOSED));
        assertTrue(AuctionLifecycleStateMachine.canTransition(AuctionStatus.CLOSED, AuctionStatus.WON));
        assertTrue(AuctionLifecycleStateMachine.canTransition(AuctionStatus.CLOSED, AuctionStatus.UNSOLD));
    }

    @Test
    void testRejectsInvalidTransitions() {
        assertFalse(AuctionLifecycleStateMachine.canTransition(AuctionStatus.DRAFT, AuctionStatus.WON));
        assertFalse(AuctionLifecycleStateMachine.canTransition(AuctionStatus.WON, AuctionStatus.ACTIVE));
        assertFalse(AuctionLifecycleStateMachine.canTransition(AuctionStatus.UNSOLD, AuctionStatus.EXTENDED));
    }

    @Test
    void testEnforceTransitionThrowsOnInvalidPath() {
        assertThrows(IllegalStateException.class, () ->
                AuctionLifecycleStateMachine.enforceTransition(AuctionStatus.DRAFT, AuctionStatus.UNSOLD));
    }

    @Test
    void testTerminalStateRecognition() {
        assertTrue(AuctionLifecycleStateMachine.isTerminal(AuctionStatus.WON));
        assertTrue(AuctionLifecycleStateMachine.isTerminal(AuctionStatus.UNSOLD));
        assertFalse(AuctionLifecycleStateMachine.isTerminal(AuctionStatus.ACTIVE));
    }

    @Test
    void testBidPlacementPermissionByState() {
        assertTrue(AuctionLifecycleStateMachine.allowsBidPlacement(AuctionStatus.ACTIVE));
        assertTrue(AuctionLifecycleStateMachine.allowsBidPlacement(AuctionStatus.EXTENDED));
        assertFalse(AuctionLifecycleStateMachine.allowsBidPlacement(AuctionStatus.DRAFT));
        assertFalse(AuctionLifecycleStateMachine.allowsBidPlacement(AuctionStatus.CLOSED));
        assertFalse(AuctionLifecycleStateMachine.allowsBidPlacement(AuctionStatus.WON));
        assertFalse(AuctionLifecycleStateMachine.allowsBidPlacement(AuctionStatus.UNSOLD));
    }
}
