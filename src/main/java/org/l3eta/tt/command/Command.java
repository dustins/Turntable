package org.l3eta.tt.command;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang.ArrayUtils;
import org.l3eta.tt.Bot;
import org.l3eta.tt.User;
import org.l3eta.tt.event.ChatEvent.ChatType;
import org.l3eta.tt.user.Rank;

import java.util.Arrays;
import java.util.Set;

public abstract class Command {
	private String name;
	private Set<Rank> rank = Sets.newHashSet(Rank.USER);
    protected Bot bot;

	public Command(String name) {
		this.name = name;
	}

	public void setRank(Rank...ranks) {
		this.rank.clear();
        this.rank.addAll(Lists.newArrayList(ranks));
	}

	public Set<Rank> getRank() {
		return rank;
	}

	public void setBot(Bot bot) {
		this.bot = bot;
	}

	public String getName() {
		return name;
	}

	public boolean canExecute(User user) {
        return this.getRank().contains(user.getRank());
	}

	public void load() {
	}

	public void unload() {
	}

	public abstract void execute(User user, String[] args, ChatType type);
}
