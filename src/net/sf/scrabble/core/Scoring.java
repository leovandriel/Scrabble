package net.sf.scrabble.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scoring {
	public static int DEFAULT_CREDIT = 1;
	public static int DEFAULT_BONUS = 0;
	private Map<Integer, Integer> creditMap = new HashMap<Integer, Integer>();
	private Map<Coord, Integer> bonusMap = new HashMap<Coord, Integer>();
	private int bonusForUsingAll;
	
	public void addCredit(int value, int credit) {
		creditMap.put(Integer.valueOf(value), Integer.valueOf(credit));
	}

	public void addBonus(Coord coord, int bonus) {
		bonusMap.put(new Coord(coord), Integer.valueOf(bonus));
	}

	public int getScore(Word word, boolean usedAll, List<Cell> cellList) {
		int credit = word.plainCredit;
		if (usedAll) {
			credit += bonusForUsingAll;
		}
		int i = 0;
		for (Cell d : cellList) {
			if (d.letter == Alphabet.EMPTY_VALUE && d.bonus < 0) {
				credit += (-d.bonus - 1) * getCreditFor(word.valueArray[i]);
			}
            i++;
		}
		for (Cell d : cellList) {
			if (d.letter == Alphabet.EMPTY_VALUE && d.bonus > 0) {
				credit *= d.bonus;
			}
		}
		return credit;
	}

	public int getScore(Word word, int value, int bonus) {
		int wordCredit = word.plainCredit;
		if (bonus < 0) {
			wordCredit += (-bonus - 1) * getCreditFor(value);
		} else if (bonus > 0) {
			wordCredit *= bonus;
		}
		return wordCredit;
	}

	public int getBonusFor(Coord coord) {
		Integer boxed = bonusMap.get(coord);
		if (boxed == null) {
			return DEFAULT_BONUS;
		}
		return boxed.intValue();
	}

	public int getCreditFor(int value) {
		Integer boxed = creditMap.get(Integer.valueOf(value));
		if (boxed == null) {
			return DEFAULT_CREDIT;
		}
		return boxed.intValue();
	}

	public int getBonusForUsingAll() {
		return bonusForUsingAll;
	}

	public void setBonusForUsingAll(int bonusForUsingAll) {
		this.bonusForUsingAll = bonusForUsingAll;
	}
}
