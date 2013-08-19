package org.l3eta.tt;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mongodb.BasicDBList;
import org.apache.commons.lang.ArrayUtils;
import org.l3eta.tt.user.Rank;
import org.l3eta.tt.util.Message;

import java.util.*;

public final class Room {
	public Users users;
	private HashMap<String, User> userList;
    public SongLog songLog;
    private LinkedList<Song> songList;
	private Set<User> djs;
    private String creatorID;
    private List<String> moderatorIDs;
	private Bot bot;
	private Song currentSong;
	private String id;
	private String name = "room.name";

	public Room(Bot bot) {
		this.bot = bot;
		users = new Users();
		userList = new HashMap<String, User>();
        songLog = new SongLog();
        songList = new LinkedList<Song>();
		djs = Sets.newHashSet();
        moderatorIDs = Lists.newArrayList();
	}

	public void setData(RoomData data) {
        creatorID = data.creator.getID();
        moderatorIDs = Arrays.asList(data.getMods());
		users.addUsers(data.getUsers());
        for (String userID : data.getDjs()) {
            this.djs.add(this.users.getByID(userID));
        }

        Song[] songs = data.getSongs();
        if (data.getSong() != null && data.getSong().equals(songs[songs.length-1])) {
            songs = (Song[])ArrayUtils.remove(songs, songs.length - 1);
        }
        songLog.addSongs(songs);

		if (data.getDjCount() > 0) {
			for (String d : data.getDjs()) {
				addDj(getUsers().getByID(d));
			}
		}

		setCurrentSong(data.getSong());
    }

	protected void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the DJs list.
	 * 
	 * @return User array of the dj's.
	 */
	public User[] getDjs() {
		return djs.toArray(new User[0]);
	}

	public void addDj(User user) {
		djs.add(user);
		user.setDj(true);
	}

	public void remDj(User user) {
		djs.remove(user);
		user.setDj(false);
	}
	
	/**
	 * @return Room Name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the current song.
	 * 
	 * @return The room's current song.
	 */
	public Song getCurrentSong() {
		return currentSong;
	}

	public class Users {
		/**
		 * Gets all the users to an Array of user
		 * 
		 * @return Array of Users
		 */
		public User[] getList() {
			return userList.values().toArray(new User[0]);
		}

		/**
		 * Adds User to the room.
		 * 
		 * @param user
		 *            The User to add.
		 */
		public void addUser(User user) {
			String id = user.getID();
			if (!userList.containsKey(id)) {
			    userList.put(id, user);
            }

            user = userList.get(id);
            bot.getDatabase().getUserRank(id);

            if (creatorID != null && creatorID.equals(user.getID())) {
                user.setRank(Rank.OWNER);
            }
            else if (moderatorIDs != null && moderatorIDs.contains(user.getID())) {
                user.setRank(Rank.MOD);
            }
		}

		/**
		 * Removes User from the room.
		 * 
		 * @param user
		 *            User to remove.
		 */
		public void removeUser(User user) {
			if (userList.containsKey(user.getID())) {
				userList.remove(user.getID());
			}
		}

		/**
		 * Gets a user by their name.
		 * 
		 * @param name
		 *            Name of the user.
		 * @return The User
		 */
		public User getByName(String name) {
			for(String key : userList.keySet()) {
				User user = userList.get(key);
				if(user != null && user.getName().equals(name)) {
					return user;
				}				
			}
			if (name.startsWith("@"))
				return getByName(name.substring(1));
			return null;
		}

		/**
		 * Gets a user by there UserID
		 * 
		 * @param line
		 *            Line containing there UserID
		 * @return The User
		 */
		public User getByID(Message message) {
			try {
				return getByID(message.getString("userid"));
			} catch (Exception ex) {
			}
			return getByID(message.toString());
		}

		/**
		 * Gets a user by there UserID
		 * 
		 * @param userid
		 *            Their UserID
		 * @return The User
		 */
		public User getByID(String userid) {
            for (User user : userList.values()) {
                if (user.getID().equals(userid)) {
                    return user;
                }
            }

			return null;
		}

		/**
		 * Checks if a user is in the room.
		 * 
		 * @param userid
		 *            ID of the user to find
		 * @return if in the room returns true, Otherwise false
		 */
		public boolean inRoom(String userid) {
			return getByID(userid) != null;
		}

		public void addUsers(BasicDBList list) {
			addUsers(list.toArray(new User[0]));
		}

		public void addUsers(User[] users) {
			for (User user : users) {
				addUser(user);
			}
		}
	}

    public class SongLog {
        public ImmutableList<Song> getList() {
            return new ImmutableList.Builder<Song>().addAll(songList).build();
        }

        public void addSong(Song song) {
            songList.addFirst(song);

            if (songList.size() > 40) {
                songList.removeLast();
            }
        }

        public void addSongs(Song[] songs) {
            for (Song song : songs) {
                this.addSong(song);
            }
        }
    }

	protected void setCurrentSong(Song song) {
        if (song != null) {
            songLog.addSong(song);
        }

		this.currentSong = song;
	}

	public Users getUsers() {
		return users;
	}

    public SongLog getSongLog() {
        return songLog;
    }

	public String getID() {
		return id;
	}

	public void saveUser(User user) {
		bot.getDatabase().saveUser(user);
	}

	// TODO RoomData
	public static class RoomData {
		private String roomid, name, name_lower, shortcut, privacy, currentDj;
		private String[] mods, djs;
		private double created;
		private boolean djFull;
		private int downvotes, pointLimit, maxDjs, upvotes, listeners, djCount, maxUsers;
		private ArrayList<User> users;
		private User creator;
		private Song song;
		private String chatServer;
		private int chatPort;
        private List<Song> songs;

		public RoomData(Message json) {
			this(json, false);
		}

		public RoomData(Message json, boolean isDGR) {
			// TODO add in sticker placements
			users = new ArrayList<User>();
            songs = Lists.newArrayList();

			if (!isDGR) {
				if (json.getBoolean("success")) {
					Message to;
					for (Message user : json.getMessageList("users")) {
						users.add(new User(user));
					}
					to = new Message(json.get("room"));
					name = to.getString("name");
					name_lower = to.getString("name_lower");
					shortcut = to.getString("shortcut");
					roomid = to.getString("roomid");
					created = to.getDouble("created");
					to = to.getSubObject("metadata");
					creator = new User(to.getSubObject("creator"));
					try {
						song = new Song(to.getSubObject("current_song"), false);
					} catch (Exception ex) {
						song = null;
					}
					djFull = to.getBoolean("dj_full");
					downvotes = to.getInt("downvotes");
					privacy = to.getString("privacy");
					upvotes = to.getInt("upvotes");
					if (to.has("djthreshold"))
						pointLimit = to.getInt("djthreshold");
					mods = to.getStringList("moderator_id");
					djs = to.getStringList("djs");
					maxDjs = to.getInt("max_djs");
					currentDj = to.getString("current_dj");
					listeners = to.getInt("listeners");
					djCount = to.getInt("djcount");
					maxUsers = to.getInt("max_size");
					// votelog = temp.get("votelog");
					// stickerPlacements = temp.get("sticker_placements");
                    for (Message song : to.getMessageList("songlog")) {
                        songs.add(new Song(song, false));
                    }

				}
			} else {
				Message to = new Message(json.get("room"));
				BasicDBList tl = to.getList("chatserver");
				chatServer = tl.get(0).toString();
				chatPort = Integer.parseInt(tl.get(1).toString());

				name = to.getString("name");
				name_lower = to.getString("name_lower");
				shortcut = to.getString("shortcut");
				roomid = to.getString("roomid");
				created = to.getDouble("created");
				to = to.getSubObject("metadata");
				try {
					song = new Song(to.getSubObject("current_song"), false);
				} catch (Exception ex) {
					song = null;
				}
				djFull = to.getBoolean("dj_full");
				downvotes = to.getInt("downvotes");
				privacy = to.getString("privacy");
				upvotes = to.getInt("upvotes");
				if (to.has("djthreshold"))
					pointLimit = to.getInt("djthreshold");
				mods = to.getStringList("moderator_id");
				djs = to.getStringList("djs");
				maxDjs = to.getInt("max_djs");
				currentDj = to.getString("current_dj");
				listeners = to.getInt("listeners");
				djCount = to.getInt("djcount");
				maxUsers = to.getInt("max_size");
				// votelog = temp.get("votelog");
                // stickerPlacements = temp.get("sticker_placements");
                for (Message song : to.getMessageList("songlog")) {
                    songs.add(new Song(song, false));
                }

                for (Message user : json.getMessageList("users")) {
					User u = new User(user);
					users.add(u);
				}

			}

		}

		/**
		 * Returns a list of Moderators in the room
		 * 
		 * @return List of Moderators
		 */
		public String[] getMods() {
			return mods;
		}

		public String[] getDjs() {
			return djs;
		}

		public String getName() {
			return name;
		}

		public String getNameLower() {
			return name_lower;
		}

		public String getID() {
			return roomid;
		}

		public String getShortcut() {
			return shortcut;
		}

		public String getPrivacy() {
			return privacy;
		}

		public String getCurrentDj() {
			return currentDj;
		}

		public String getChatServer() {
			return chatServer;
		}

		public double getCreated() {
			// TODO change this to a timestamp;
			return created;
		}

		public int getChatPort() {
			return chatPort;
		}

		public int getUpVotes() {
			return upvotes;
		}

		public int getMaxUsers() {
			return maxUsers;
		}

		public int getDownVotes() {
			return downvotes;
		}

		public int getPointLimit() {
			return pointLimit;
		}

		public int getDjCount() {
			return djCount;
		}

		public int getListeners() {
			return listeners;
		}

		public int getMaxDjs() {
			return maxDjs;
		}

		public boolean isDeckFull() {
			return djFull;
		}

        public User getCreator() {
			return creator;
		}

		public User[] getUsers() {
			return users.toArray(new User[0]);
		}

		public Song getSong() {
			return song;
		}

        public Song[] getSongs() {
            return songs.toArray(new Song[]{});
        }
	}
}
