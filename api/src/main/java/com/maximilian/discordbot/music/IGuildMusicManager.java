package com.maximilian.discordbot.music;

public interface IGuildMusicManager {

    AudioPlayerSendHandler getSendHandler();


    AbstractTrackScheduler getScheduler();

}
