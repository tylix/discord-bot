package com.maximilianwiegmann.discordbot.data;

import com.maximilian.discordbot.data.GuildChannelData;
import com.maximilian.discordbot.data.IDataHandler;
import com.maximilianwiegmann.discordbot.DiscordBot;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.attribute.IPositionableChannel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class DataHandler implements IDataHandler {

    private final Map<Long, List<GuildChannelData>> channels = new HashMap<>();
    private final List<Guild> guilds = new ArrayList<>();

    @Override
    public Guild getGuild(String guildId) {
        return guilds.stream().filter(guild -> guild.getId().equals(guildId)).findFirst().orElse(null);
    }

    @Override
    public Guild getGuild(long guildId) {
        return getGuild(String.valueOf(guildId));
    }

    @Override
    public void addGuild(Guild guild) {
        guilds.add(guild);
    }

    @Override
    public void removeGuild(Guild guild) {
        guilds.removeIf(g -> g.getId().equals(guild.getId()));
    }

    @Override
    public List<GuildChannelData> getChannel(Guild guild) {
        if (!channels.containsKey(guild.getIdLong())) return new ArrayList<>();
        return channels.get(guild.getIdLong());
    }

    @Override
    public void loadChannels(Guild guild) {
        channels.put(guild.getIdLong(), guild.getChannels().stream().map(this::toChannelData).collect(Collectors.toList()));
    }

    @Override
    public void addChannel(Channel channel, Guild guild) {
        if (!channels.containsKey(guild.getIdLong())) return;
        channels.get(guild.getIdLong()).add(toChannelData(channel));
    }

    @Override
    public void removeChannel(Channel channel, Guild guild) {
        if (!channels.containsKey(guild.getIdLong())) return;
        channels.get(guild.getIdLong()).removeIf(data -> data.getId().equals(channel.getId()));
    }

    @Override
    public void changeChannel(Channel channel, Guild guild) {
        if (!channels.containsKey(guild.getIdLong())) return;
        GuildChannelData item = channels.get(guild.getIdLong()).stream().filter(data -> data.getId().equals(channel.getId())).findFirst().orElse(null);
        if (item == null) return;
        int index = channels.get(guild.getIdLong()).indexOf(item);
        item = toChannelData(channel);
        channels.get(guild.getIdLong()).set(index, item);
    }

    private GuildChannelData toChannelData(Channel channel) {
        return GuildChannelData.builder()
                .type(DiscordBot.INSTANCE.getAutoChannelHandler().isAutoChannel(channel.getIdLong()) ? -2 : DiscordBot.INSTANCE.getAutoChannelHandler().isChildChannel(channel.getIdLong()) ? -3 : channel.getType().getId())
                .name(channel.getName())
                .position(channel instanceof IPositionableChannel voiceChannel ? voiceChannel.getPositionRaw() : 0)
                .id(channel.getId())
                .build();
    }

}
