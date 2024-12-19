package com.skycatdev.autocut.export;

import org.apache.commons.lang3.math.Fraction;

import java.util.concurrent.TimeUnit;

public record Timecode(long frames, Fraction fps) {

    public static Timecode fromMillis(long millis, Fraction fps) {
        Fraction framesPerMilli = fps.divideBy(Fraction.getFraction(1000));
        Fraction frames = framesPerMilli.multiplyBy(Fraction.getFraction(millis));
        return new Timecode(frames.longValue(), fps);
    }

    public Timecode add(int amount, TimeUnit unit) {
        return add(unit.toMillis(amount));
    }

    public Timecode add(long millis) {
        Fraction addedFrames = fps.divideBy(Fraction.getFraction(1000)).multiplyBy(Fraction.getFraction(millis));
        return new Timecode(addedFrames.longValue() + frames(), fps());
    }

    public Timecode subtractFrames(long frames) {
        return new Timecode(frames() - frames, fps);
    }

    @Override
    public String toString() {
        Fraction secondsPerFrame = Fraction.getFraction(fps().getDenominator(), fps().getNumerator());
        Fraction millisPerFrame = secondsPerFrame.multiplyBy(Fraction.getFraction(1000));
        long millis = millisPerFrame.multiplyBy(Fraction.getFraction(frames())).longValue();
        long hours = millis / 3600000; // 1000 millis/sec * 60 sec/min * 60 min/hr;
        long minutes = (millis / 60000) % 60;
        long seconds = (millis / 1000) % 60;
        return String.format("%02d:%02d:%02d:%02d", hours, minutes, seconds, (long)(frames() % fps.doubleValue()));
    }

}
