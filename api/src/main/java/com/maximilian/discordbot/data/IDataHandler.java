package com.maximilian.discordbot.data;

import com.maximilian.discordbot.data.member.GuildUserData;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
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

    List<GuildUserData> getUser(Guild guild);

    void loadUsers(Guild guild);

    void addMember(Guild guild, Member member);

    void updateMember(Guild guild, Member member);

    void removeMember(Guild guild, Member member);


}
