package com.gathin.tvhome;

/** A wake is queued while another app owns the screen, then consumed once Home resumes. */
final class DomainLifecycle {
    private boolean foreground, screenOn = true, reveal = true;
    void resume() { foreground = true; }
    void pause() { foreground = false; }
    void screenOff() { screenOn = false; reveal = true; }
    void screenOn() { screenOn = true; }
    boolean active() { return foreground && screenOn; }
    boolean consumeReveal() {
        if (!active() || !reveal) return false;
        reveal = false;
        return true;
    }
}
