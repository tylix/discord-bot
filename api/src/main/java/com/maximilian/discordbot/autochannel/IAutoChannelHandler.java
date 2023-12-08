package com.maximilian.discordbot.autochannel;

import com.maximilian.discordbot.JsonConfig;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public interface IAutoChannelHandler {

    void loadAutoChannel();

    void interactChannel(Member member, VoiceChannel channelJoined, VoiceChannel channelLeft, InteractType type);

    void createChannel(VoiceChannel parent, Member member);

    void deleteChannel(VoiceChannel child);

    void saveTempChannel();

    List<Long> getAutoChannel();

    Map<Long, List<Long>> getChildChannel();

    default boolean isAutoChannel(long id) {
        return getAutoChannel().stream().anyMatch(aLong -> aLong == id);
    }

    default boolean isChildChannel(long id) {
        return getChildChannel().entrySet().stream().anyMatch(longListEntry -> longListEntry.getValue().stream().anyMatch(aLong -> aLong == id));
        /*if (!getChildChannel().containsKey(id)) return false;
        return getChildChannel().get(id).stream().anyMatch(aLong -> aLong == id);*/
    }

    default Long getParent(long childId) {
        return getChildChannel().entrySet().stream()
                .filter(entry -> entry.getValue().contains(childId))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(0L);
    }

    default boolean hasParent(long id) {
        return getParent(id) != 0L;
    }

    JsonConfig getTempChannelConfig();

    enum InteractType {
        CONNECT,
        SWITCH,
        DISCONNECT
    }

}
