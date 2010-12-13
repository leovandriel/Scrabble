package net.sf.scrabble.local;

import net.sf.scrabble.core.Alphabet;

public abstract class LatinFactory implements ScrabbleFactory {
	public static final int DEFAULT_EMPTY_CODE = ' ';
	public static final int DEFAULT_JOKER_CODE = '_';

	public Alphabet createAlphabet() {
		Alphabet result = new Alphabet(DEFAULT_JOKER_CODE, DEFAULT_EMPTY_CODE);
		for (int i = 'A'; i <= 'Z'; i++) {
			result.addCodePoint(i);
		}
		return result;
	}
}
