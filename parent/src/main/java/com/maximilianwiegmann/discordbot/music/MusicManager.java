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
    public void loadAndPlay(TextChannel textChannel, String trackURL, long voiceChannel) {
        loadAndPlay(textChannel, textChannel.getGuild(), trackURL, voiceChannel);
    }

    @Override
    public void loadAndPlay(TextChannel textChannel, Guild guild, String trackURL, long voiceChannel) {

        IGuildMusicManager musicManager = getMusicManager(guild);

        if (!guild.getAudioManager().isConnected())
            guild.getAudioManager().openAudioConnection(guild.getVoiceChannelById(voiceChannel));

        guild.getAudioManager().setSelfDeafened(true);
        guild.getAudioManager().setSelfMuted(false);

        this.audioPlayerManager.loadItemOrdered(musicManager, trackURL, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                musicManager.getScheduler().queue(audioTrack);

                if (textChannel != null)
                    textChannel.sendMessage("Adding to queue **`" + audioTrack.getInfo().title + "`** by **`" + audioTrack.getInfo().author + "`**").queue();

            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                List<AudioTrack> tracks = audioPlaylist.getTracks();

                if (!tracks.isEmpty()) {
                    AudioTrack track = tracks.get(0);
                    musicManager.getScheduler().queue(track);
                    if (tracks.size() > 1 && !audioPlaylist.isSearchResult()) {
                        for (int i = 1; i < tracks.size(); i++)
                            musicManager.getScheduler().queue(tracks.get(i));
                        if (textChannel != null)
                            textChannel.sendMessage("Adding **`" + tracks.size() + " Tracks`** to queue").queue();
                    }
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
    public void stop(Guild guild) {
        IGuildMusicManager musicManager = getMusicManager(guild);
        musicManager.getScheduler().getAudioPlayer().stopTrack();
        guild.getAudioManager().closeAudioConnection();
    }

    @Override
    public void skip(TextChannel textChannel) {
        IGuildMusicManager musicManager = getMusicManager(textChannel.getGuild());
        musicManager.getScheduler().nextTrack();

        AudioTrack track = musicManager.getScheduler().getAudioPlayer().getPlayingTrack();

        textChannel.sendMessage("Now playing **`" + track.getInfo().title + "`** by **`" + track.getInfo().author + "`**").queue();
    }

    @Override
    public IGuildMusicManager getMusicManager(Guild guild) {
        return this.musicManagers.computeIfAbsent(guild.getIdLong(), (guildId) -> {
            IGuildMusicManager guildMusicManager = new GuildMusicManager(this.audioPlayerManager, guildId);
            guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());
            return guildMusicManager;
        });
    }
}
