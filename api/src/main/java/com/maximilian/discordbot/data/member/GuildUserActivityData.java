package com.maximilian.discordbot.data.member;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Activity;

@Builder
@Data
@Getter
@Setter
public class GuildUserActivityData {

    private String name;
    private String state;
    private String url;
    private String emoji;
    private String details;
    private String image;
    private long timestamp;
    private Activity.ActivityType activityType;
}
