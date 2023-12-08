package com.maximilian.discordbot.data;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.Channel;

import java.util.List;

public interface IDataHandler {

    Guild getGuild(String guildId);

    Guild getGuild(long guildId);

    List<Guild> getGuilds();

    void addGuild(Guild guild);

    void removeGuild(Guild guild);

    List<GuildChannelData> getChannel(Guild guild);

    void loadChannels(Guild guild);

    void addChannel(Channel channel, Guild guild);

    void removeChannel(Channel channel, Guild guild);

    void changeChannel(Channel channel, Guild guild);

}
