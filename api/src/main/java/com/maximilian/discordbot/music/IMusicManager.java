package com.maximilian.discordbot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.Map;

public interface IMusicManager {

    void loadAndPlay(TextChannel textChannel, String trackURL);

    void stop(Guild guild);

    void skip(TextChannel textChannel);

    IGuildMusicManager getMusicManager(Guild guild);

    Map<Long, IGuildMusicManager> getMusicManagers();

    AudioPlayerManager getAudioPlayerManager();

}
