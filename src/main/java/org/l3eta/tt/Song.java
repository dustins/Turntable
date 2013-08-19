package org.l3eta.tt;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import org.joda.time.Duration;
import org.joda.time.LocalDateTime;
import org.l3eta.tt.util.Message;

public class Song {
	private static Room room;
	private User user;
	private String id;
	private Integer up, down, snag;
    private Duration duration;
    private LocalDateTime startTime;


	// Meta / Useless
	private String artist, genre, name, album;

	public Song(Message message, boolean isPlaylistSong) {
		if(!isPlaylistSong)
			this.user = room.getUsers().getByID(message.getString("djid"));
        if (message.has("starttime")) {
            Double startTime = message.getDouble("starttime") * 1000d;
            this.startTime = new LocalDateTime(startTime.longValue());
        }
		this.id = message.getString("_id");
		message = message.getSubObject("metadata");
		this.name = message.getString("song");
		this.artist = message.getString("artist");
		this.album = message.getString("album");
		this.genre = message.getString("genre");
        this.duration = new Duration(message.getInt("length")*1000);
		snag = 0;
		up = 0;
		down = 0;
	}

	public String getID() {
		return id;
	}

	public String getUserID() {
		return user.getID();
	}

	public String getName() {
		return name;
	}

    public Optional<LocalDateTime> getStartTime() {
        return Optional.fromNullable(startTime);
    }

    public Duration getDuration() {
        return duration;
    }

    public String getGenre() {
		return genre;
	}

	public String getAlbum() {
		return album;
	}

	public String getArtist() {
		return artist;
	}

	public Integer getUpVotes() {
		return up;
	}

	public Integer getDownVotes() {
		return down;
	}

	public Integer getSnags() {
		return snag;
	}

	public User getUser() {
		return user;
	}

	// Bot functions
	protected void setUp(int up) {
		this.up = up;
	}

	protected void setDown(int down) {
		this.down = down;
	}

	protected void setSnag(int snag) {
		this.snag = snag;
	}

	protected static void setRoom(Room room) {
		Song.room = room;
	}
	
	protected void addSnag() {
		snag++;
	}

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Song other = (Song) obj;
        return Objects.equal(this.getID(), other.getID());
    }
}
