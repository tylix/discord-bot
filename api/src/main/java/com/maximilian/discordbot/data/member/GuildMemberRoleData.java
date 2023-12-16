package com.maximilian.discordbot.data.member;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Builder
@Data
@Getter
@Setter
public class GuildMemberRoleData {

    private String name;
    private String id;
    private int position;
    private int color;

}
