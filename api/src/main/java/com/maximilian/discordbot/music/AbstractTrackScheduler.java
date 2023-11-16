package com.maximilian.discordbot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.BlockingQueue;

@Getter
@RequiredArgsConstructor
public abstract class AbstractTrackScheduler extends AudioEventAdapter {

    private final AudioPlayer audioPlayer;
    private final BlockingQueue<AudioTrack> queue;
    private final long guildId;

    public abstract void queue(AudioTrack track);

    public abstract void nextTrack();

    @Override
    public abstract void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason);

}
