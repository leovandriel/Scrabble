package net.sf.scrabble.local;

import net.sf.scrabble.core.Board;

/**
 * A provider of custom scrabble stuff like the alphabet, scoring, board.
 */
public interface ScrabbleFactory {
	public Board createBoard();
}
