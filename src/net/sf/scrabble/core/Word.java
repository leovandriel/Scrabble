package net.sf.scrabble.core;

import java.util.Arrays;

/**
 * A dictionary word, including letter frequency and credit cache.
 */
class Word {
	public int[] freqArray;
	public int[] valueArray;
	public int plainCredit;

	private Word(int[] valueArray, int range, int credit) {
		this.valueArray = valueArray;
		plainCredit = credit;
		freqArray = new int[range + 1];
		for (int i = 0; i < valueArray.length; i++) {
			freqArray[valueArray[i]]++;
		}
		freqArray[range] = valueArray.length;
	}

	public int length() {
		return valueArray.length;
	}

	public int valueAt(int i) {
		return valueArray[i];
	}

	/**
	 * Tests whether this word can be composed out of as set of tokens.
	 */
	public boolean canBeMadeOutOf(int[] tokenArray, int jokerCount) {
		for (int i = 0; i < freqArray.length; i++) {
			if (freqArray[i] > tokenArray[i]) {
				jokerCount -= freqArray[i] - tokenArray[i];
				if (jokerCount < 0) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Tests whether this word contains a given sequence.
	 */
	public boolean contains(int[] sequence) {
		loop: for (int i = 0; i < valueArray.length; i++) {
			for (int j = 0; j < sequence.length; j++) {
				if (valueArray[i + j] != sequence[j]) {
					continue loop;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * Tests whether this word matches the provided pattern.
	 */
	public boolean matches(int[] sequence) {
		for (int i = 0; i < valueArray.length; i++) {
			if (valueArray[i] != sequence[i] && sequence[i] != Alphabet.EMPTY_VALUE) {
				return false;
			}
		}
		return true;
	}

	public static Word createFrom(String sequence, Alphabet alphabet, Scoring scoring) {
		int[] valueArray = new int[sequence.length()];
		int credits = 0;
		for (int i = 0; i < valueArray.length; i++) {
			int value = alphabet.getValueForCode(sequence.codePointAt(i));
			valueArray[i] = value;
			credits += scoring.getCreditFor(value);
		}
		return new Word(valueArray, alphabet.getSize(), credits);
	}

	public static String toString(int[] array) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < array.length; i++) {
			builder.append(array[i]);
			builder.append(' ');
		}
		return builder.toString();
	}

	@Override
	public String toString() {
		return "Word(" + toString(valueArray) + "#:" + valueArray.length + ")";
	}

	@Override
	public int hashCode() {
		return 31 + Arrays.hashCode(valueArray);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Word)) {
			return false;
		}
		Word other = (Word) obj;
		if (!Arrays.equals(valueArray, other.valueArray)) {
			return false;
		}
		return true;
	}
}
