package com.example.trafficlightsim;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

/** Streams and loops the supplied MP3 on a background thread. */
final class MusicPlayer {
    private static final String MUSIC_RESOURCE = "/com/example/trafficlightsim/assets/asus-elevator-music.mp3";
    private volatile boolean playing = true;
    private volatile SourceDataLine activeLine;

    void start() {
        Thread thread = new Thread(this::playLoop, "traffic-simulation-music");
        thread.setDaemon(true);
        thread.start();
    }

    void stop() {
        playing = false;
        SourceDataLine line = activeLine;
        if (line != null) {
            line.stop();
            line.close();
        }
    }

    private void playLoop() {
        while (playing) {
            try (InputStream resource = MusicPlayer.class.getResourceAsStream(MUSIC_RESOURCE)) {
                if (resource == null) {
                    System.err.println("Music file was not found: " + MUSIC_RESOURCE);
                    return;
                }
                // MP3SPI can inspect metadata and decode the stream through Java Sound.
                try (BufferedInputStream buffered = new BufferedInputStream(resource, 64 * 1024);
                     AudioInputStream mp3 = AudioSystem.getAudioInputStream(buffered)) {
                    AudioFormat source = mp3.getFormat();
                    float sampleRate = source.getSampleRate() > 0 ? source.getSampleRate() : 44100;
                    int channels = Math.max(1, source.getChannels());
                    AudioFormat pcm = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                            sampleRate, 16, channels, channels * 2, sampleRate, false);
                    try (AudioInputStream decoded = AudioSystem.getAudioInputStream(pcm, mp3)) {
                        DataLine.Info info = new DataLine.Info(SourceDataLine.class, pcm);
                        try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info)) {
                            activeLine = line;
                            line.open(pcm);
                            line.start();
                            byte[] buffer = new byte[8192];
                            int count;
                            while (playing && (count = decoded.read(buffer, 0, buffer.length)) != -1) {
                                line.write(buffer, 0, count);
                            }
                            if (playing) line.drain();
                            activeLine = null;
                        }
                    }
                }
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
                System.err.println("Could not play the simulation music: " + ex.getMessage());
                return;
            }
        }
    }
}
