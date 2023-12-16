package com.maximilianwiegmann.discordbot.data;

import com.maximilian.discordbot.data.GuildChannelData;
import com.maximilian.discordbot.data.member.GuildMemberRoleData;
import com.maximilian.discordbot.data.member.GuildUserActivityData;
import com.maximilian.discordbot.data.member.GuildUserData;
import com.maximilian.discordbot.data.IDataHandler;
import com.maximilianwiegmann.discordbot.DiscordBot;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.attribute.IAgeRestrictedChannel;
import net.dv8tion.jda.api.entities.channel.attribute.IPositionableChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.internal.entities.channel.mixin.attribute.ITopicChannelMixin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class DataHandler implements IDataHandler {

    private final Map<Long, List<GuildChannelData>> channels = new HashMap<>();
    private final Map<Long, List<GuildUserData>> users = new HashMap<>();
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
        channels.put(guild.getIdLong(), guild.getChannels().stream().map(guildChannel -> toChannelData(guild, guildChannel)).collect(Collectors.toList()));
    }

    @Override
    public void addChannel(Channel channel, Guild guild) {
        if (!channels.containsKey(guild.getIdLong())) return;
        channels.get(guild.getIdLong()).add(toChannelData(guild, channel));
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
        item = toChannelData(guild, channel);
        channels.get(guild.getIdLong()).set(index, item);
    }

    @Override
    public List<GuildUserData> getUser(Guild guild) {
        if (!users.containsKey(guild.getIdLong())) return new ArrayList<>();
        return users.get(guild.getIdLong());
    }


    @Override
    public void loadUsers(Guild guild) {
        users.putIfAbsent(guild.getIdLong(), new ArrayList<>());
        guild.loadMembers(member -> {
            users.get(guild.getIdLong()).add(toUserData(member));
        });
    }

    @Override
    public void addMember(Guild guild, Member member) {
        if (!users.containsKey(guild.getIdLong())) return;
        users.get(guild.getIdLong()).add(toUserData(member));
    }

    @Override
    public void updateMember(Guild guild, Member member) {
        if (!users.containsKey(guild.getIdLong())) return;
        GuildUserData item = users.get(guild.getIdLong()).stream().filter(data -> data.getId().equals(member.getId())).findFirst().orElse(null);
        if (item == null) return;
        int index = users.get(guild.getIdLong()).indexOf(item);
        item = toUserData(member);
        users.get(guild.getIdLong()).set(index, item);
    }

    @Override
    public void removeMember(Guild guild, Member member) {
        if (!users.containsKey(guild.getIdLong())) return;
        users.get(guild.getIdLong()).removeIf(data -> data.getId().equals(member.getId()));
    }

    private GuildChannelData toChannelData(Guild guild, Channel channel) {
        return GuildChannelData.builder()
                .type(DiscordBot.INSTANCE.getAutoChannelHandler().isAutoChannel(guild, channel.getIdLong()) ? -2 : DiscordBot.INSTANCE.getAutoChannelHandler().isChildChannel(guild, channel.getIdLong()) ? -3 : channel.getType().getId())
                .name(channel.getName())
                .position(channel instanceof IPositionableChannel voiceChannel ? voiceChannel.getPosition() : -1)
                .id(channel.getId())
                .topic(channel instanceof ITopicChannelMixin<?> topicChannel ? topicChannel.getTopic() : null)
                .nsfw(channel instanceof IAgeRestrictedChannel ageRestricted && ageRestricted.isNSFW())
                .maxMember(channel instanceof VoiceChannel voiceChannel ? voiceChannel.getUserLimit() : -1)
                .member(channel instanceof VoiceChannel voiceChannel ? voiceChannel.getMembers().size() : -1)
                .build();
    }

    private GuildUserData toUserData(Member member) {
        return GuildUserData.builder()
                .nickname(member.getEffectiveName())
                .name(member.getUser().getName())
                .avatar(member.getUser().getAvatarUrl())
                .id(member.getId())
                .onlineStatus(member.getOnlineStatus())
                .currentChannel(member.getVoiceState().inAudioChannel() ? member.getVoiceState().getChannel().getId() : null)
                .activityData(
                        member.getActivities().stream().map(activity -> {
                                    if (activity == null || activity.asRichPresence() == null) return null;
                                    return GuildUserActivityData.builder()
                                            .activityType(activity.getType())
                                            .url(activity.getUrl())
                                            .details(activity.asRichPresence().getDetails())
                                            .state(activity.asRichPresence().getState())
                                            .image(activity.asRichPresence().getSmallImage() == null ? null : activity.asRichPresence().getSmallImage().getUrl())
                                            .emoji(activity.asRichPresence().getEmoji() != null ? activity.asRichPresence().getEmoji().asUnicode().getFormatted() : null)
                                            .timestamp(activity.getTimestamps() == null ? 0 : activity.getTimestamps().getStartTime().toEpochMilli())
                                            .name(activity.getName())
                                            .build();
                                }
                        ).collect(Collectors.toList())
                )
                .owner(member.isOwner())
                .boosting(member.isBoosting() ? member.getTimeBoosted().toInstant().toEpochMilli() : -1)
                .muted(member.getVoiceState().isSelfMuted())
                .deafened(member.getVoiceState().isSelfDeafened())
                .streaming(member.getVoiceState().isStream())
                .joined(member.getTimeJoined().toInstant().toEpochMilli())
                .roles(member.getRoles().stream().map(role -> GuildMemberRoleData.builder()
                        .name(role.getName())
                        .id(role.getId())
                        .color(role.getColorRaw())
                        .position(role.getPosition())
                        .build()).collect(Collectors.toList()))
                .build();
    }

}
