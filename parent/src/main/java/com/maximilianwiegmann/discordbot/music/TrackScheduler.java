package com.maximilianwiegmann.discordbot.music;

import com.maximilian.discordbot.music.AbstractTrackScheduler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AbstractTrackScheduler {

    public TrackScheduler(AudioPlayer audioPlayer) {
        super(audioPlayer, new LinkedBlockingQueue<>());
    }

    @Override
    public void queue(AudioTrack track) {
        if (!this.getAudioPlayer().startTrack(track, true))
            this.getQueue().offer(track);
    }

    @Override
    public void nextTrack() {
        this.getAudioPlayer().startTrack(this.getQueue().poll(), false);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext)
            nextTrack();
    }
}
