package net.sf.scrabble.core;

/**
 * A single spot on the board, accomanied with some caches that keep track of
 * candidate letters and score.
 */
class Cell {
	public int[] creditCacheArray;
	public int letter = Alphabet.EMPTY_VALUE;
	public int bonus = 0;
	// caching
	public boolean isNoBegin;
	public boolean isNoEnd;
	public boolean isYetConnected;
	public int minWordLength;
	public int maxWordLength;
	public int[] beginOfWordWithFrequency;

	public Cell(int alphabetSize) {
		creditCacheArray = new int[alphabetSize];
	}

	public void resetCreditCache(boolean allowed) {
		int credit = allowed ? 0 : -1;
		for (int i = 0; i < creditCacheArray.length; i++) {
			creditCacheArray[i] = credit;
		}
	}

	@Override
	public String toString() {
		return super.toString();
	}

	public String toString(Alphabet alphabet) {
		StringBuilder builder = new StringBuilder();
		builder.append("Cell(letter=" + letter);
		builder.append("  no-begin=" + (isNoBegin ? "T" : "F"));
		builder.append("  no-end=" + (isNoEnd ? "T" : "F"));
		if (minWordLength > 0) {
			builder.append("  min=" + minWordLength);
		}
		if (maxWordLength > 0) {
			builder.append("  max=" + maxWordLength);
		}
		builder.append("  connect=" + (isYetConnected ? "T" : "F"));
		builder.append("  allowed=");
		for (int i = 0; i < creditCacheArray.length; i++) {
			if (creditCacheArray[i] >= 0) {
				builder.appendCodePoint(alphabet.getCodeForValue(i));
			}
		}
		if (beginOfWordWithFrequency != null) {
			builder.append("  begin=" + alphabet.frequencyToString(beginOfWordWithFrequency));
		}
		builder.append(")");
		return builder.toString();
	}
}
