package com.maximilian.discordbot.data;

import com.maximilian.discordbot.data.member.GuildUserData;
import com.maximilian.discordbot.data.music.MusicPlayerData;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Data
@Getter
@Setter
public class GuildData {

    private String id;
    private String name;
    private Long created;
    private String iconUrl;
    private MusicPlayerData musicPlayerData;
    private List<GuildChannelData> channel;
    private List<GuildUserData> users;
    private int memberAmount;

    // TODO: ROLES, SETTINGS, USERS

}
