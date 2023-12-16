package com.maximilian.discordbot.data.member;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.OnlineStatus;

import java.util.List;

@Builder
@Data
@Getter
@Setter
public class GuildUserData {

    private String id;
    private String nickname;
    private String name;
    private String avatar;
    private String currentChannel;
    private OnlineStatus onlineStatus;
    private long joined;
    private long boosting;
    private boolean owner;
    private boolean muted;
    private boolean deafened;
    private boolean streaming;
    private List<GuildUserActivityData> activityData;
    private List<GuildMemberRoleData> roles;

}
