package com.skycatdev.autocut;

public record Timecode(int hours, int minutes, int seconds, int frames, int fps) { // TODO: WARN: Potential problems with very long videos, need to investigate
    public Timecode {
        assert minutes < 60 && minutes >= 0; // TODO: Error handling
        assert seconds < 60 && seconds >= 0; // TODO: Error handling
        assert frames < fps && frames >= 0; // TODO: Error handling
        // assert fps > 0; // implied
    }

    public Timecode(long frames, int fps) {
        this(frames / fps, (int) (frames % fps), fps);
    }

    public Timecode(long seconds, int frames, int fps) {
        this(seconds / 60, (int) (seconds % 60), frames, fps);
    }

    public Timecode(long minutes, int seconds, int frames, int fps) {
        this((int) minutes / 60, (int) minutes % 60, seconds, frames, fps);
    }

    public Timecode add(Timecode other) {
        assert other.fps() == fps(); // TODO: Error handling
        return new Timecode(toFrames() + other.toFrames(), fps);
    }

    public String asString() {
        return String.format("%02d:%02d:%02d:%02d", hours(), minutes(), seconds(), frames());
    }

    public Timecode subtract(Timecode other) {
        assert other.fps() == fps(); // TODO: Error handling
        return new Timecode(toFrames() - other.toFrames(), fps());
    }

    public long toFrames() {
        return toSecondsFloored() * fps + frames;
    }

    public long toMinutesFloored() {
        return hours() * 60L + minutes();
    }

    public long toSecondsFloored() {
        return toMinutesFloored() * 60 + seconds();
    }

    @Override
    public String toString() {
        return String.format("%02d:%02d:%02d:%02d @ %d fps", hours(), minutes(), seconds(), frames(), fps());
    }
}
