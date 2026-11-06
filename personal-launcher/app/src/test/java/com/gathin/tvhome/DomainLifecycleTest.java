package com.gathin.tvhome;

import org.junit.Test;
import static org.junit.Assert.*;

public class DomainLifecycleTest {
    @Test public void appReturnDoesNotRepeatReveal() {
        DomainLifecycle state = new DomainLifecycle(); state.resume();
        assertTrue(state.consumeReveal()); state.pause(); assertFalse(state.active());
        state.resume(); assertFalse(state.consumeReveal());
    }
    @Test public void wakeWhileAnotherAppOwnsScreenWaitsForHome() {
        DomainLifecycle state = new DomainLifecycle(); state.resume(); state.consumeReveal(); state.pause();
        state.screenOff(); state.screenOn(); assertFalse(state.active()); assertFalse(state.consumeReveal());
        state.resume(); assertTrue(state.consumeReveal()); assertFalse(state.consumeReveal());
    }
    @Test public void screenOffStopsAnimationAndWakeRevealsOnce() {
        DomainLifecycle state = new DomainLifecycle(); state.resume(); state.consumeReveal();
        state.screenOff(); assertFalse(state.active()); state.screenOn();
        assertTrue(state.active()); assertTrue(state.consumeReveal()); assertFalse(state.consumeReveal());
    }
}
