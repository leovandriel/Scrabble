package net.sf.scrabble.core;

import java.util.ArrayList;
import java.util.List;

/**
 * A word (combination of letters) on the board, specified by word, the
 * position, and the orientation. In combination with the board it is played
 * onto, this credits are also present.
 */
public class Combo {

	public enum OrientationType {
		HORIZONTAL, VERTICAL
	}

	protected Word word;
	protected Coord coord;
	protected int credits;
	protected OrientationType orientation = OrientationType.HORIZONTAL;

	public class Assigment {
		public int x;
		public int y;
		public int c;

		private Assigment(int x, int y, int c) {
			this.x = x;
			this.y = y;
			this.c = c;
		}
	}

	public List<Assigment> getAssignmentList(Alphabet alphabet) {
		List<Assigment> result = new ArrayList<Assigment>();
		int dx = orientation == OrientationType.HORIZONTAL ? 1 : 0;
		int dy = 1 - dx;
		int[] array = word.valueArray;
		int x = coord.x;
		int y = coord.y;
		for (int i = 0; i < array.length; i++) {
			result.add(new Assigment(x + i * dx, y + i * dy, alphabet.getCodeForValue(array[i])));
		}
		return result;
	}

	public int getCredits() {
		return credits;
	}

	public String getString(Alphabet alphabet) {
		return alphabet.toString(word);
	}

	public Coord getCoord() {
		return coord;
	}

	public boolean isHorizontal() {
		return orientation == OrientationType.HORIZONTAL;
	}

	protected Combo(Word word, Coord coord, int credits) {
		this(word, coord, credits, OrientationType.HORIZONTAL);
	}

	protected Combo(Word word, Coord coord, int credits, OrientationType orientation) {
		this.word = word;
		this.coord = coord;
		this.credits = credits;
		this.orientation = orientation;
	}

	protected Combo getMirror() {
		Coord c = new Coord(coord.y, coord.x);
		OrientationType o = orientation == OrientationType.HORIZONTAL ? OrientationType.VERTICAL
				: OrientationType.HORIZONTAL;
		return new Combo(word, c, credits, o);
	}

	@Override
	public String toString() {
		return "Combo(" + word + ", " + coord + ", " + orientation + ", " + credits + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((coord == null) ? 0 : coord.hashCode());
		result = prime * result + ((orientation == null) ? 0 : orientation.hashCode());
		result = prime * result + ((word == null) ? 0 : word.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Combo)) {
			return false;
		}
		Combo other = (Combo) obj;
		if (coord == null) {
			if (other.coord != null) {
				return false;
			}
		} else if (!coord.equals(other.coord)) {
			return false;
		}
		if (orientation == null) {
			if (other.orientation != null) {
				return false;
			}
		} else if (!orientation.equals(other.orientation)) {
			return false;
		}
		if (word == null) {
			if (other.word != null) {
				return false;
			}
		} else if (!word.equals(other.word)) {
			return false;
		}
		return true;
	}
}
