package net.sf.scrabble.core;

import java.util.AbstractSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * A sorted set of combos by means of a solution set for the current board
 * configuration.
 */
class ResultSet extends AbstractSet<Combo> {
	private TreeSet<Combo> set = new TreeSet<Combo>(new Comparator<Combo>() {
		public int compare(Combo a, Combo b) {
			int diff = b.getCredits() - a.getCredits();
			if (diff >= 0) {
				diff++;
			}
			return diff;
		}
	});
	private int maxSize;

	public ResultSet(int maxSize) {
		this.maxSize = maxSize;
	}

	@Override
	public boolean add(Combo c) {
		if (set.size() == maxSize) {
			Combo last = set.last();
			if (last.getCredits() < c.getCredits()) {
				set.remove(last);
			} else {
				return false;
			}
		}
		return set.add(c);
	}

	@Override
	public Iterator<Combo> iterator() {
		return set.iterator();
	}

	@Override
	public int size() {
		return set.size();
	}
}
