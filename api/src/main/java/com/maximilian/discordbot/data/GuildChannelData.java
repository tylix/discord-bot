package com.maximilian.discordbot.data;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Builder
@Data
@Getter
@Setter
public class GuildChannelData {

    private String name;
    private String id;
    private int type;
    private int position;


}
