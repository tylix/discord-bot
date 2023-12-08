package com.maximilianwiegmann.discordbot.music;

import com.maximilian.discordbot.music.AbstractTrackScheduler;
import com.maximilianwiegmann.discordbot.DiscordBot;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AbstractTrackScheduler {

    public TrackScheduler(AudioPlayer audioPlayer, long guildId) {
        super(audioPlayer, new LinkedBlockingQueue<>(), new ArrayList<>(), guildId);
    }

    @Override
    public void queue(AudioTrack track) {
        if (!this.getAudioPlayer().startTrack(track, true))
            this.getQueue().offer(track);
        else
            getHistory().add(track);
    }

    @Override
    public void nextTrack() {
        if (getQueue().isEmpty()) {
            DiscordBot.INSTANCE.getMusicManager().stop(DiscordBot.INSTANCE.getDataHandler().getGuild(getGuildId()));
            return;
        }
        AudioTrack track = this.getQueue().poll();
        this.getAudioPlayer().startTrack(track, false);
        getHistory().add(track);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext)
            nextTrack();
    }
}
