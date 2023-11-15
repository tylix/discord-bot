package com.maximilianwiegmann.discordbot.music;

import com.maximilian.discordbot.music.AbstractTrackScheduler;
import com.maximilian.discordbot.music.AudioPlayerSendHandler;
import com.maximilian.discordbot.music.IGuildMusicManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public class GuildMusicManager implements IGuildMusicManager {

    private final AudioPlayer audioPlayer;
    private final AbstractTrackScheduler scheduler;
    private final AudioPlayerSendHandler sendHandler;

    public GuildMusicManager(AudioPlayerManager manager) {
        this.audioPlayer = manager.createPlayer();
        this.scheduler = new TrackScheduler(this.audioPlayer);
        this.audioPlayer.addListener(this.scheduler);
        this.sendHandler = new AudioPlayerSendHandler(this.audioPlayer);
    }
}
