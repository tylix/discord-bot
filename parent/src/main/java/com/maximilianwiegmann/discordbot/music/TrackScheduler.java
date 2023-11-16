package com.maximilianwiegmann.discordbot.music;

import com.maximilian.discordbot.music.AbstractTrackScheduler;
import com.maximilianwiegmann.discordbot.DiscordBot;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AbstractTrackScheduler {

    public TrackScheduler(AudioPlayer audioPlayer, long guildId) {
        super(audioPlayer, new LinkedBlockingQueue<>(), guildId);
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
        if (getQueue().isEmpty())
            DiscordBot.INSTANCE.getMusicManager().stop(DiscordBot.INSTANCE.getJda().getGuildById(getGuildId()));
    }
}
