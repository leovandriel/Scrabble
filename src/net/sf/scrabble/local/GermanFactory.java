package net.sf.scrabble.local;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import net.sf.scrabble.core.Alphabet;
import net.sf.scrabble.core.Board;
import net.sf.scrabble.core.Coord;
import net.sf.scrabble.core.Dictionary;
import net.sf.scrabble.core.Scoring;

public class GermanFactory extends LatinFactory {
	public static final String DEFAULT_DICTIONARY_FILE = "de.dic";
	public static final String DEFAULT_DICTIONARY_ENCODING = "iso-8859-15";
	public static final int DEFAULT_WIDTH = 15;
	public static final int DEFAULT_HEIGHT = 15;
	private static final int DEFAULT_BONUS_FOR_USING_ALL = 50;
	private static final int[] DEFAULT_CREDIT_ARRAY = new int[] { 1, 3, 4, 1, 1, 4, 2, 2, 1, 6, 4, 2, 3, 1, 2, 4, 10,
			1, 1, 1, 1, 6, 3, 8, 10, 3, 6, 8, 6 };
	private final static int[][] DEFAULT_BONUS_MATRIX = new int[][] {
		{ 3, 0, 0,-2, 0, 0, 0, 3, 0, 0, 0,-2, 0, 0, 3 },
        { 0, 2, 0, 0, 0,-3, 0, 0, 0,-3, 0, 0, 0, 2, 0 },
		{ 0, 0, 2, 0, 0, 0,-2, 0,-2, 0, 0, 0, 2, 0, 0 },
        {-2, 0, 0, 2, 0, 0, 0,-2, 0, 0, 0, 2, 0, 0,-2 },
		{ 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0 },
        { 0,-3, 0, 0, 0,-3, 0, 0, 0,-3, 0, 0, 0,-3, 0 },
		{ 0, 0,-2, 0, 0, 0,-2, 0,-2, 0, 0, 0,-2, 0, 0 },
        { 3, 0, 0,-2, 0, 0, 0, 2, 0, 0, 0,-2, 0, 0, 3 },
		{ 0, 0,-2, 0, 0, 0,-2, 0,-2, 0, 0, 0,-2, 0, 0 },
        { 0,-3, 0, 0, 0,-3, 0, 0, 0,-3, 0, 0, 0,-3, 0 },
		{ 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0 },
        {-2, 0, 0, 2, 0, 0, 0,-2, 0, 0, 0, 2, 0, 0,-2 },
		{ 0, 0, 2, 0, 0, 0,-2, 0,-2, 0, 0, 0, 2, 0, 0 },
        { 0, 2, 0, 0, 0,-3, 0, 0, 0,-3, 0, 0, 0, 2, 0 },
		{ 3, 0, 0,-2, 0, 0, 0, 3, 0, 0, 0,-2, 0, 0, 3 } };

	public Scoring createScoring() {
		Scoring result = new Scoring();
		for (int i = 0; i < DEFAULT_CREDIT_ARRAY.length; i++) {
			int credit = DEFAULT_CREDIT_ARRAY[i];
			if (credit != Scoring.DEFAULT_CREDIT) {
				result.addCredit(i, credit);
			}
		}
		for (int x = 0; x < DEFAULT_BONUS_MATRIX.length; x++) {
			for (int y = 0; y < DEFAULT_BONUS_MATRIX[x].length; y++) {
				int bonus = DEFAULT_BONUS_MATRIX[x][y];
				if (bonus != Scoring.DEFAULT_BONUS) {
					result.addBonus(new Coord(x, y), bonus);
				}
			}
		}
		result.setBonusForUsingAll(DEFAULT_BONUS_FOR_USING_ALL);
		return result;
	}

	public Dictionary createDictionary(Alphabet alphabet, Scoring scoring) {
		try {
			InputStream stream = getClass().getResourceAsStream(DEFAULT_DICTIONARY_FILE);
			Reader reader = new BufferedReader(new InputStreamReader(stream, DEFAULT_DICTIONARY_ENCODING));
			return Dictionary.readFrom(reader, alphabet, scoring);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

    @Override
    public Alphabet createAlphabet() {
        Alphabet alphabet = super.createAlphabet();
        alphabet.addCodePoint('Ä');
        alphabet.addCodePoint('Ö');
        alphabet.addCodePoint('Ü');
        return alphabet;
    }

    public Board createBoard() {
		Alphabet alphabet = createAlphabet();
		Scoring scoring = createScoring();
		Dictionary dictionary = createDictionary(alphabet, scoring);
		return new Board(DEFAULT_WIDTH, DEFAULT_HEIGHT, dictionary, alphabet, scoring);
	}
}
