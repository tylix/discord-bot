package com.maximilianwiegmann.discordbot.autochannel;

import com.maximilian.discordbot.JsonConfig;
import com.maximilian.discordbot.autochannel.IAutoChannelHandler;
import com.maximilianwiegmann.discordbot.DiscordBot;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

import java.io.File;
import java.text.MessageFormat;
import java.util.*;

@Getter
public class AutoChannelHandler implements IAutoChannelHandler {

    private final List<Long> autoChannel = new ArrayList<>();
    private final Map<Long, List<Long>> childChannel = new HashMap<>();
    private final JsonConfig tempChannelConfig = new JsonConfig(new File("tempchannel.json"));

    @Override
    public void loadAutoChannel() {
        System.out.println("Loading AutoChannel...");

        Long[] channel = DiscordBot.INSTANCE.getConfig().getOrDefaultSet("autoChannel", Long[].class, new Long[]{});
        for (Long l : channel) {
            autoChannel.add(l);

            Long[] tempChannel = tempChannelConfig.get(String.valueOf(l), Long[].class);
            if (tempChannel == null) tempChannel = new Long[]{};
            else {
                tempChannelConfig.set(String.valueOf(l), null);
                tempChannelConfig.saveConfig();
            }

            childChannel.put(l, new ArrayList<>(Arrays.asList(tempChannel)));

            for (Long loadedChannel : tempChannel) {
                VoiceChannel voiceChannel = DiscordBot.INSTANCE.getGuild().getVoiceChannelById(loadedChannel);
                if (voiceChannel == null) continue;
                if (voiceChannel.getMembers().isEmpty())
                    deleteChannel(voiceChannel);
            }
        }
        System.out.println(MessageFormat.format("{0} channel(s) has been successfully loaded.", autoChannel.size()));
    }

    @Override
    public void interactChannel(Member member, VoiceChannel channelJoined, VoiceChannel channelLeft, InteractType type) {
        System.out.println(member.getEffectiveName() + " " + channelJoined + " " + channelLeft + " " + type.name());

        switch (type) {
            case CONNECT:
                createChannel(channelJoined, member);
                break;
            case DISCONNECT:
                if (channelLeft.getMembers().isEmpty())
                    deleteChannel(channelLeft);
                break;
            case SWITCH:
                if (hasParent(channelLeft.getIdLong())
                        && isAutoChannel(channelJoined.getIdLong())
                        && getParent(channelLeft.getIdLong()) == channelJoined.getIdLong()
                        && channelLeft.getMembers().isEmpty()) {
                    DiscordBot.INSTANCE.getGuild().moveVoiceMember(member, channelLeft).complete();
                    return;
                }

                if (hasParent(channelLeft.getIdLong()) && channelLeft.getMembers().isEmpty())
                    deleteChannel(channelLeft);

                if (isAutoChannel(channelJoined.getIdLong())) createChannel(channelJoined, member);
                break;
        }
    }

    @Override
    public void deleteChannel(VoiceChannel child) {
        System.out.println("Deleting auto channel " + child.getName());

        Long parent = getParent(child.getIdLong());
        VoiceChannel parentChannel = DiscordBot.INSTANCE.getGuild().getVoiceChannelById(parent);

        if (parentChannel == null) return;

        childChannel.get(parent).removeIf(aLong -> aLong == child.getIdLong());
        child.delete().complete();

        for (int i = 0; i < childChannel.get(parent).size(); i++) {
            VoiceChannel channel = DiscordBot.INSTANCE.getGuild().getVoiceChannelById(childChannel.get(parent).get(i));
            if (channel == null) continue;

            channel.getManager().setName(parentChannel.getName() + " » " + DiscordBot.INSTANCE.intToRoman(i + 1)).complete();
        }
    }

    @Override
    public void createChannel(VoiceChannel parent, Member member) {
        System.out.println("Creating auto channel " + DiscordBot.INSTANCE.intToRoman(childChannel.get(parent.getIdLong()).size() + 1) + " " + parent.getName());

        VoiceChannel channel = DiscordBot.INSTANCE.getGuild().createVoiceChannel(parent.getName() + " » " + DiscordBot.INSTANCE.intToRoman(childChannel.get(parent.getIdLong()).size() + 1)).setNSFW(parent.isNSFW()).setParent(parent.getParentCategory()).setPosition(parent.getPosition()).setUserlimit(parent.getUserLimit()).complete();
        childChannel.get(parent.getIdLong()).add(channel.getIdLong());

        DiscordBot.INSTANCE.getGuild().moveVoiceMember(member, channel).complete();
    }

    @Override
    public void saveTempChannel() {
        childChannel.forEach((aLong, longs) -> {
            if (!longs.isEmpty()) tempChannelConfig.set(String.valueOf(aLong), longs);
        });
        tempChannelConfig.saveConfig();
    }
}
