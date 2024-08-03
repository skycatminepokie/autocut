package com.skycatdev.autocut;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacv.*;

import java.io.*;
import java.util.ArrayList;
import java.util.stream.Stream;

public class Recorder {
    protected ArrayList<Clip> clips = new ArrayList<>();
    protected ArrayList<RecordingEvent> events = new ArrayList<>();

    public Recorder() {
    }

    public void onRecordingEnded(String outputPath) {
        FFmpegLogCallback.set();
        File recording = new File(outputPath);
        File export = recording.toPath().resolveSibling("cut" + recording.getName()).toFile();
        String ffmpeg = Loader.load(org.bytedeco.ffmpeg.ffmpeg.class);
        ProcessBuilder pb = new ProcessBuilder(ffmpeg, "-f", "concat", "-safe", "0", "-i", "C:\\Users\\USER\\Videos\\Recordings\\texxt.txt", "-c", "copy", export.getAbsolutePath());
        try {
            pb.inheritIO().start().waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return;
    }
}
