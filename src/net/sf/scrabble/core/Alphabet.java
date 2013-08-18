package net.sf.scrabble.core;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The allowed characters in the alphabet.
 */
public class Alphabet {
	public static final int EMPTY_VALUE = -1;
	public static final int JOKER_VALUE = -2;
	private List<Integer> codeList = new LinkedList<Integer>();
	private Map<Integer, Integer> codeMap = new HashMap<Integer, Integer>();
	private int jokerCode;
	private int emptyCode;

	public Alphabet(int jokerCode, int emptyCode) {
		this.jokerCode = jokerCode;
		this.emptyCode = emptyCode;
	}

	public void addCodePoint(int code) {
		Integer boxed = Integer.valueOf(code);
		if (codeList.contains(boxed)) {
			throw new RuntimeException("Code already in alphabet: " + code);
		}
		codeMap.put(boxed, Integer.valueOf(codeList.size()));
		codeList.add(boxed);
	}

	public int getSize() {
		return codeList.size();
	}

	public int getValueForCode(int code) {
		return codeMap.get(Integer.valueOf(code)).intValue();
	}

	public int getValueForToken(int code) {
		if (code == jokerCode) {
			return JOKER_VALUE;
		}
		if (code == emptyCode) {
			return EMPTY_VALUE;
		}
		return getValueForCode(code);
	}

	public int getCodeForValue(int value) {
		return codeList.get(value).intValue();
	}

	public int getTokenForValue(int value) {
		switch (value) {
		case JOKER_VALUE:
			return jokerCode;
		case EMPTY_VALUE:
			return emptyCode;
		default:
			return getCodeForValue(value);
		}
	}

	public String frequencyToString(int[] frequency) {
		StringBuilder builder = new StringBuilder(frequency[getSize()]);
		for (int i = 0; i < getSize(); i++) {
			for (int j = 0; j < frequency[i]; j++) {
				builder.appendCodePoint(getCodeForValue(i));
			}
		}
		return builder.toString();
	}

	public String sequenceToString(int[] sequence) {
		StringBuilder builder = new StringBuilder(sequence.length);
		for (int i = 0; i < sequence.length; i++) {
			builder.appendCodePoint(getCodeForValue(sequence[i]));
		}
		return builder.toString();
	}

	public void checkLegalTokens(String tokens) {
		for (int i = 0; i < tokens.length(); i++) {
			getValueForToken(tokens.codePointAt(i));
		}
	}

	public int getFrequencyAndJoker(String tokens, int[] freqArray) {
		int jokers = 0;
		for (int i = 0; i < tokens.length(); i++) {
			int code = tokens.codePointAt(i);
			if (code == jokerCode) {
				jokers++;
			} else {
				freqArray[getValueForCode(code)]++;
			}
		}
		freqArray[getSize()] = tokens.length() - jokers;
		return jokers;
	}

	public String toString(Word word) {
		int[] valueArray = word.valueArray;
		StringBuilder builder = new StringBuilder(valueArray.length);
		for (int i = 0; i < valueArray.length; i++) {
			builder.appendCodePoint(getCodeForValue(valueArray[i]));
		}
		return builder.toString();
	}
}
