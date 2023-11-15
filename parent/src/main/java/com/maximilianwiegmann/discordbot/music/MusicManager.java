package com.maximilianwiegmann.discordbot.music;

import com.maximilian.discordbot.music.IGuildMusicManager;
import com.maximilian.discordbot.music.IMusicManager;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class MusicManager implements IMusicManager {

    private final Map<Long, IGuildMusicManager> musicManagers = new HashMap<>();
    private final AudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();

    public MusicManager() {
        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager);
    }

    @Override
    public void loadAndPlay(TextChannel textChannel, String trackURL) {
        IGuildMusicManager musicManager = getMusicManager(textChannel.getGuild());

        System.out.println(trackURL);

        this.audioPlayerManager.loadItemOrdered(musicManager, trackURL, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                musicManager.getScheduler().queue(audioTrack);

                textChannel.sendMessage("Adding to queue **`" + audioTrack.getInfo().title + "`** by **`" + audioTrack.getInfo().author + "`**").queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                List<AudioTrack> tracks = audioPlaylist.getTracks();

                if (!tracks.isEmpty()) {
                    AudioTrack track = tracks.get(0);
                    musicManager.getScheduler().queue(track);
                    textChannel.sendMessage("Adding to queue **`" + track.getInfo().title + "`** by **`" + track.getInfo().author + "`**").queue();
                }
            }

            @Override
            public void noMatches() {

            }

            @Override
            public void loadFailed(FriendlyException e) {

            }
        });
    }

    @Override
    public void stop(TextChannel textChannel) {
        IGuildMusicManager musicManager = getMusicManager(textChannel.getGuild());
        musicManager.getScheduler().getAudioPlayer().stopTrack();
        textChannel.getGuild().getAudioManager().closeAudioConnection();
    }

    @Override
    public void skip(TextChannel textChannel) {
        IGuildMusicManager musicManager = getMusicManager(textChannel.getGuild());
        musicManager.getScheduler().nextTrack();
    }

    @Override
    public IGuildMusicManager getMusicManager(Guild guild) {
        return this.musicManagers.computeIfAbsent(guild.getIdLong(), (guildId) -> {
            IGuildMusicManager guildMusicManager = new GuildMusicManager(this.audioPlayerManager);
            guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());
            return guildMusicManager;
        });
    }
}
