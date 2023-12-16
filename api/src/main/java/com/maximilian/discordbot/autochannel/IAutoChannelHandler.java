package com.maximilian.discordbot.autochannel;

import com.maximilian.discordbot.JsonConfig;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public interface IAutoChannelHandler {

    void loadAutoChannel(Guild guild);

    void interactChannel(Guild guild, Member member, VoiceChannel channelJoined, VoiceChannel channelLeft, InteractType type);

    void createChannel(Guild guild, VoiceChannel parent, Member member);

    void deleteChannel(Guild guild, VoiceChannel child);

    void saveTempChannel();

    void removeAutoChannel(Guild guild, long id);

    void addAutoChannel(Guild guild, long id);

    Map<Long, List<Long>> getAutoChannel();

    Map<Long, Map<Long, List<Long>>> getChildChannel();

    default boolean isAutoChannel(Guild guild, long id) {
        return getAutoChannel().get(guild.getIdLong()).stream().anyMatch(aLong -> aLong == id);
    }

    default boolean isChildChannel(Guild guild, long id) {
        return getChildChannel().get(guild.getIdLong()).entrySet().stream().anyMatch(longListEntry -> longListEntry.getValue().contains(id));
        /*if (!getChildChannel().containsKey(id)) return false;
        return getChildChannel().get(id).stream().anyMatch(aLong -> aLong == id);*/
    }

    default Long getParent(Guild guild, long childId) {
        return getChildChannel().get(guild.getIdLong()).entrySet().stream()
                .filter(entry -> entry.getValue().contains(childId))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(0L);
    }

    default boolean hasParent(Guild guild, long id) {
        return getParent(guild, id) != 0L;
    }

    JsonConfig getTempChannelConfig();

    enum InteractType {
        CONNECT,
        SWITCH,
        DISCONNECT
    }

}
