package com.skycatdev.autocut;

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
        /*
        assert recording.exists(); // TODO: Error handling
        InputStream inputStream; // TODO: Put in try-with-resources
        try {
            inputStream = new FileInputStream(recording);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(recording); FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(export, grabber.getImageWidth(), grabber.getImageHeight(), grabber.getAudioChannels())) {
            export.createNewFile();
            grabber.start();
            recorder.start(grabber.getFormatContext());
            var frame = grabber.grabFrame(true, true, true, false);
            while (frame != null) {
                recorder.record(frame);
                frame = grabber.grabFrame(true, true, true, false);
            }
            recorder.stop();
            grabber.stop();
        } catch (FrameGrabber.Exception e) {
            throw new RuntimeException(e);
        } catch (FrameRecorder.Exception e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        */
        // TODO: This magically doubles the video length, but not the audio length :sigh:
        String inputFile = outputPath;

        try {
            FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFile);
            grabber.start();
            int audioChannels = grabber.getAudioChannels();
            boolean hasAudio = audioChannels > 0;

            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(recording.toPath().resolveSibling("cut" + recording.getName()).toString(), grabber.getImageWidth(), grabber.getImageHeight(), audioChannels);
            recorder.start();

            Frame frame;
            while ((frame = grabber.grabFrame(hasAudio, true, true, false)) != null) {
                recorder.record(frame);
            }
            recorder.stop();
            grabber.stop();
        } catch (FFmpegFrameGrabber.Exception e) {
            throw new RuntimeException(e);
        } catch (FFmpegFrameRecorder.Exception e) {
            throw new RuntimeException(e);
        }

    }

    /*
     public File exportEdl() throws IOException { // TODO: Better throwing of errors
         File edl = new File(recordingPath + ".edl");
         assert !edl.exists() : "EDL already exists, and error handling has not been set up yet!"; // TODO
         edl.createNewFile();

         try (PrintWriter pw = new PrintWriter(edl)) {
             pw.println("TITLE: " + title);
             pw.println("FCM: NON-DROP FRAME");
             pw.println();
             long timelineIn = 0;
             long timelineOut;
             for (int i = 0; i < clips.size(); i++) {
                 // Clip header
                 Clip clip = clips.get(i);
                 timelineOut = timelineIn + clip.duration();
                 pw.printf("%03d  AX       V     C        %s %s %s %s\n", i + 1, clip.in().asString(), clip.out().asString(), timelineIn.asString(), timelineOut.asString());
                 timelineIn = timelineOut;

                 pw.println("* FROM CLIP NAME: " + recordingName);

                 pw.println();
             }
         }
         return edl; // TODO: Warn when there's over 999 clips
     }
    */
}
