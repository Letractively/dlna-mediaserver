package de.sosd.mediaserver.domain.db;

public enum ClassNameWcType {

    // @XmlEnumValue("object.item")
    OBJECT_ITEM("object.item"),
    // @XmlEnumValue("object.item.imageItem")
    OBJECT_ITEM_IMAGE_ITEM("object.item.imageItem"),
    // @XmlEnumValue("object.item.imageItem.photo")
    OBJECT_ITEM_IMAGE_ITEM_PHOTO("object.item.imageItem.photo"),
    // @XmlEnumValue("object.item.audioItem")
    OBJECT_ITEM_AUDIO_ITEM("object.item.audioItem"),
    // @XmlEnumValue("object.item.audioItem.musicTrack")
    OBJECT_ITEM_AUDIO_ITEM_MUSIC_TRACK("object.item.audioItem.musicTrack"),
    // @XmlEnumValue("object.item.audioItem.audioBroadcast")
    OBJECT_ITEM_AUDIO_ITEM_AUDIO_BROADCAST(
            "object.item.audioItem.audioBroadcast"),
    // @XmlEnumValue("object.item.audioItem.audioBook")
    OBJECT_ITEM_AUDIO_ITEM_AUDIO_BOOK("object.item.audioItem.audioBook"),
    // @XmlEnumValue("object.item.videoItem")
    OBJECT_ITEM_VIDEO_ITEM("object.item.videoItem"),
    // @XmlEnumValue("object.item.videoItem.movie")
    OBJECT_ITEM_VIDEO_ITEM_MOVIE("object.item.videoItem.movie"),
    // @XmlEnumValue("object.item.videoItem.videoBroadcast")
    OBJECT_ITEM_VIDEO_ITEM_VIDEO_BROADCAST(
            "object.item.videoItem.videoBroadcast"),
    // @XmlEnumValue("object.item.videoItem.musicVideoClip")
    OBJECT_ITEM_VIDEO_ITEM_MUSIC_VIDEO_CLIP(
            "object.item.videoItem.musicVideoClip"),
    // @XmlEnumValue("object.item.playlistItem")
    OBJECT_ITEM_PLAYLIST_ITEM("object.item.playlistItem"),
    // @XmlEnumValue("object.item.textItem")
    OBJECT_ITEM_TEXT_ITEM("object.item.textItem"),
    // @XmlEnumValue("object.item.bookmarkItem")
    OBJECT_ITEM_BOOKMARK_ITEM("object.item.bookmarkItem"),
    // @XmlEnumValue("object.item.epgItem")
    OBJECT_ITEM_EPG_ITEM("object.item.epgItem"),
    // @XmlEnumValue("object.item.epgItem.audioProgram")
    OBJECT_ITEM_EPG_ITEM_AUDIO_PROGRAM("object.item.epgItem.audioProgram"),
    // @XmlEnumValue("object.item.epgItem.videoProgram")
    OBJECT_ITEM_EPG_ITEM_VIDEO_PROGRAM("object.item.epgItem.videoProgram"),
    // @XmlEnumValue("object.container.person")
    OBJECT_CONTAINER_PERSON("object.container.person"),
    // @XmlEnumValue("object.container.person.musicArtist")
    OBJECT_CONTAINER_PERSON_MUSIC_ARTIST("object.container.person.musicArtist"),
    // @XmlEnumValue("object.container.playlistContainer")
    OBJECT_CONTAINER_PLAYLIST_CONTAINER("object.container.playlistContainer"),
    // @XmlEnumValue("object.container.album")
    OBJECT_CONTAINER_ALBUM("object.container.album"),
    // @XmlEnumValue("object.container.album.musicAlbum")
    OBJECT_CONTAINER_ALBUM_MUSIC_ALBUM("object.container.album.musicAlbum"),
    // @XmlEnumValue("object.container.album.photoAlbum")
    OBJECT_CONTAINER_ALBUM_PHOTO_ALBUM("object.container.album.photoAlbum"),
    // @XmlEnumValue("object.container.genre")
    OBJECT_CONTAINER_GENRE("object.container.genre"),
    // @XmlEnumValue("object.container.genre.musicGenre")
    OBJECT_CONTAINER_GENRE_MUSIC_GENRE("object.container.genre.musicGenre"),
    // @XmlEnumValue("object.container.genre.movieGenre")
    OBJECT_CONTAINER_GENRE_MOVIE_GENRE("object.container.genre.movieGenre"),
    // @XmlEnumValue("object.container.channelGroup")
    OBJECT_CONTAINER_CHANNEL_GROUP("object.container.channelGroup"),
    // @XmlEnumValue("object.container.channelGroup.audioChannelGroup")
    OBJECT_CONTAINER_CHANNEL_GROUP_AUDIO_CHANNEL_GROUP(
            "object.container.channelGroup.audioChannelGroup"),
    // @XmlEnumValue("object.container.channelGroup.videoChannelGroup")
    OBJECT_CONTAINER_CHANNEL_GROUP_VIDEO_CHANNEL_GROUP(
            "object.container.channelGroup.videoChannelGroup"),
    // @XmlEnumValue("object.container.epgContainer")
    OBJECT_CONTAINER_EPG_CONTAINER("object.container.epgContainer"),
    // @XmlEnumValue("object.container.storageSystem")
    OBJECT_CONTAINER_STORAGE_SYSTEM("object.container.storageSystem"),
    // @XmlEnumValue("object.container.storageVolume")
    OBJECT_CONTAINER_STORAGE_VOLUME("object.container.storageVolume"),
    // @XmlEnumValue("object.container.storageFolder")
    OBJECT_CONTAINER_STORAGE_FOLDER("object.container.storageFolder"),
    // @XmlEnumValue("object.container.bookmarkFolder")
    OBJECT_CONTAINER_BOOKMARK_FOLDER("object.container.bookmarkFolder"),
    OBJECT_CONTAINER_VIDEO_PHOTO_ALBUM("object.container.album.videoAlbum");
    private final String value;

    ClassNameWcType(final String v) {
        this.value = v;
    }

    public String value() {
        return this.value;
    }

    public static ClassNameWcType fromValue(final String v) {
        for (final ClassNameWcType c : ClassNameWcType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
