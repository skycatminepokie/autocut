package com.skycatdev.autocut;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Recorder {
    protected String title;
    protected String recordingPath; // As opposed to the EDL output
    protected String recordingName;
    protected int fps;
    protected ArrayList<Clip> clips = new ArrayList<>();

    public Recorder(String title, String recordingPath, String recordingName, int fps) {
        this.title = title;
        this.recordingPath = recordingPath;
        this.recordingName = recordingName;
        this.fps = fps;
    }

    public File toEdl() throws IOException { // TODO: Better throwing of errors
        File edl = new File(recordingPath + ".edl");
        assert !edl.exists() : "EDL already exists, and error handling has not been set up yet!"; // TODO
        edl.createNewFile();

        try (PrintWriter pw = new PrintWriter(edl)) {
            pw.println("TITLE: " + title);
            pw.println("FCM: NON-DROP FRAME");
            pw.println();
            Timecode timelineIn = new Timecode(0, 0, 0, 0, fps);
            Timecode timelineOut;
            for (int i = 0; i < clips.size(); i++) {
                // Clip header
                Clip clip = clips.get(i);
                timelineOut = timelineIn.add(clip.duration());
                pw.printf("%03d  AX       V     C        %s %s %s %s\n", i + 1, clip.in().asString(), clip.out().asString(), timelineIn.asString(), timelineOut.asString());
                timelineIn = timelineOut;

                pw.println("* FROM CLIP NAME: " + recordingName);

                pw.println();
            }
        }
        return edl; // TODO: Warn when there's over 999 clips
    }
}
