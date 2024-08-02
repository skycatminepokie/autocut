package com.skycatdev.autocut;

import java.util.ArrayList;

public class Recorder {
    protected ArrayList<Clip> clips = new ArrayList<>();
    protected ArrayList<RecordingEvent> events = new ArrayList<>();

    public Recorder() {
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
