package com.maximilian.discordbot.data.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Data
@Getter
@Setter
public class MusicPlayerData {

    private MusicTrackData currentTrack;
    private long currentPosition;
    private List<MusicTrackData> queue;
    private boolean pause;
    private int volume;
    private String currentChannel;

}
