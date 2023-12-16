package com.maximilianwiegmann.discordbot.autochannel;

import com.google.gson.reflect.TypeToken;
import com.iwebpp.crypto.TweetNaclFast;
import com.maximilian.discordbot.JsonConfig;
import com.maximilian.discordbot.autochannel.IAutoChannelHandler;
import com.maximilianwiegmann.discordbot.DiscordBot;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

import java.io.File;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.*;

@Getter
public class AutoChannelHandler implements IAutoChannelHandler {

    private final Map<Long, List<Long>> autoChannel = new HashMap<>();
    private final Map<Long, Map<Long, List<Long>>> childChannel = new HashMap<>();
    private final JsonConfig tempChannelConfig = new JsonConfig(new File("tempchannel.json"));

    private final JsonConfig config = DiscordBot.INSTANCE.getConfig();

    @Override
    public void loadAutoChannel(Guild guild) {
        System.out.println("Loading AutoChannel...");

        autoChannel.putIfAbsent(guild.getIdLong(), new ArrayList<>());
        childChannel.putIfAbsent(guild.getIdLong(), new HashMap<>());

        Type type = new TypeToken<HashMap<Long, Long[]>>(){}.getType();

        Long[] channel = config.getOrDefaultSet("autoChannel." + guild.getId(), Long[].class, new Long[]{});
        Map<Long, Long[]> tempGuildChannel = tempChannelConfig.get(guild.getId(), type);
        for (Long l : channel) {
            autoChannel.get(guild.getIdLong()).add(l);

            if (tempGuildChannel == null) {
                childChannel.get(guild.getIdLong()).put(l, new ArrayList<>());
                continue;
            }

            Long[] tempChannel = tempGuildChannel.get(l);
            if (tempChannel == null) tempChannel = new Long[]{};
            else tempChannelConfig.set(guild.getId(), null);

            childChannel.get(guild.getIdLong()).put(l, new ArrayList<>(Arrays.asList(tempChannel)));

            for (Long loadedChannel : tempChannel) {
                VoiceChannel voiceChannel = guild.getVoiceChannelById(loadedChannel);
                if (voiceChannel == null) continue;
                if (voiceChannel.getMembers().isEmpty())
                    deleteChannel(guild, voiceChannel);
            }
        }
        tempChannelConfig.saveConfig();
        System.out.println(MessageFormat.format("{0} channel(s) has been successfully loaded.", autoChannel.size()));
    }

    @Override
    public void interactChannel(Guild guild, Member member, VoiceChannel channelJoined, VoiceChannel channelLeft, InteractType type) {
        System.out.println(member.getEffectiveName() + " " + channelJoined + " " + channelLeft + " " + type.name());

        switch (type) {
            case CONNECT:
                createChannel(guild, channelJoined, member);
                break;
            case DISCONNECT:
                if (channelLeft.getMembers().isEmpty() || channelLeft.getMembers().size() == 1 && channelLeft.getMembers().get(0).getUser().isBot())
                    deleteChannel(guild, channelLeft);
                break;
            case SWITCH:
                if (hasParent(guild, channelLeft.getIdLong())
                        && isAutoChannel(guild, channelJoined.getIdLong())
                        && getParent(guild, channelLeft.getIdLong()) == channelJoined.getIdLong()
                        && channelLeft.getMembers().isEmpty()) {
                    DiscordBot.INSTANCE.getGuild().moveVoiceMember(member, channelLeft).complete();
                    return;
                }

                if (hasParent(guild, channelLeft.getIdLong()) && channelLeft.getMembers().isEmpty() || channelLeft.getMembers().size() == 1 && channelLeft.getMembers().get(0).getUser().isBot())
                    deleteChannel(guild, channelLeft);

                if (isAutoChannel(guild, channelJoined.getIdLong())) createChannel(guild, channelJoined, member);
                break;
        }
    }

    @Override
    public void deleteChannel(Guild guild, VoiceChannel child) {
        System.out.println("Deleting auto channel " + child.getName());

        Long parent = getParent(guild, child.getIdLong());
        VoiceChannel parentChannel = guild.getVoiceChannelById(parent);

        if (parentChannel == null) return;

        childChannel.get(guild.getIdLong()).get(parent).removeIf(aLong -> aLong == child.getIdLong());
        child.delete().complete();

        for (int i = 0; i < childChannel.get(guild.getIdLong()).get(parent).size(); i++) {
            VoiceChannel channel = guild.getVoiceChannelById(childChannel.get(guild.getIdLong()).get(parent).get(i));
            if (channel == null) continue;

            channel.getManager().setName(parentChannel.getName() + " » " + DiscordBot.INSTANCE.intToRoman(i + 1)).complete();
        }
    }

    @Override
    public void createChannel(Guild guild, VoiceChannel parent, Member member) {
        System.out.println("Creating auto channel " + DiscordBot.INSTANCE.intToRoman(childChannel.get(guild.getIdLong()).get(parent.getIdLong()).size() + 1) + " " + parent.getName());

        System.out.println(parent.getPositionRaw());
        VoiceChannel channel = guild
                .createVoiceChannel(parent.getName() + " » " + DiscordBot.INSTANCE.intToRoman(childChannel.get(guild.getIdLong()).get(parent.getIdLong()).size() + 1))
                .setNSFW(parent.isNSFW())
                .setParent(parent.getParentCategory())
                .setPosition(parent.getPositionRaw())
                .setUserlimit(parent.getUserLimit())
                .complete();
        childChannel.get(guild.getIdLong()).get(parent.getIdLong()).add(channel.getIdLong());
        for (PermissionOverride rolePermissionOverride : parent.getPermissionContainer().getRolePermissionOverrides()) {
            channel.getPermissionContainer().getManager().putPermissionOverride(rolePermissionOverride.getPermissionHolder(), rolePermissionOverride.getAllowed(), rolePermissionOverride.getDenied()).queue();
        }

        guild.moveVoiceMember(member, channel).complete();
    }

    @Override
    public void saveTempChannel() {
        /*childChannel.forEach((aLong, longs) -> {
            if (!longs.isEmpty()) tempChannelConfig.set(String.valueOf(aLong), longs);
        });*/
        childChannel.forEach((aLong, longListMap) -> {
            if (!longListMap.keySet().isEmpty()) tempChannelConfig.set(String.valueOf(aLong), longListMap);
        });
        tempChannelConfig.saveConfig();
    }

    @Override
    public void removeAutoChannel(Guild guild, long id) {
        if (!autoChannel.containsKey(guild.getIdLong())) return;
        autoChannel.get(guild.getIdLong()).remove(id);

        Long[] channel = config.getOrDefaultSet("autoChannel." + guild.getId(), Long[].class, new Long[]{});
        List<Long> channelList = new ArrayList<>(Arrays.asList(channel));
        channelList.remove(id);
        config.set("autoChannel." + guild.getId(), channelList);
        config.saveConfig();

        DiscordBot.INSTANCE.getDataHandler().changeChannel(guild.getGuildChannelById(id), guild);
    }

    @Override
    public void addAutoChannel(Guild guild, long id) {
        if (!autoChannel.containsKey(guild.getIdLong())) return;
        autoChannel.get(guild.getIdLong()).add(id);

        Long[] channel = config.getOrDefaultSet("autoChannel." + guild.getId(), Long[].class, new Long[]{});
        List<Long> channelList = new ArrayList<>(Arrays.asList(channel));
        channelList.add(id);
        config.set("autoChannel." + guild.getId(), channelList);
        config.saveConfig();

        childChannel.get(guild.getIdLong()).put(id, new ArrayList<>());

        DiscordBot.INSTANCE.getDataHandler().changeChannel(guild.getGuildChannelById(id), guild);
    }
}
