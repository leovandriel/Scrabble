package net.sf.scrabble.local;

import net.sf.scrabble.core.Alphabet;
import net.sf.scrabble.core.Board;
import net.sf.scrabble.core.Dictionary;
import net.sf.scrabble.core.Scoring;

/**
 * A provider of custom scrabble stuff like the alphabet, scoring, board.
 */
public interface ScrabbleFactory {
	public Alphabet createAlphabet();

	public Scoring createScoring();

	public Board createBoard();

	public Dictionary createDictionary();
}
