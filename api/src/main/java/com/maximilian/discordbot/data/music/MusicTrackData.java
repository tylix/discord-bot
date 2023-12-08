package com.maximilian.discordbot.data.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Builder
@Data
@Getter
@Setter
public class MusicTrackData {

    private String title;
    private String author;
    private long length;
    private String identifier;
    private boolean stream;
    private String uri;

    public static MusicTrackData fromAudioTrackInfo(AudioTrackInfo trackInfo) {
        return MusicTrackData.builder().author(trackInfo.author).length(trackInfo.length).title(trackInfo.title).identifier(trackInfo.identifier).stream(trackInfo.isStream).uri(trackInfo.uri).build();
    }

}
