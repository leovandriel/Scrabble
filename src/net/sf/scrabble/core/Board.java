package net.sf.scrabble.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The scrabble board as a bidirection set of cells.
 */
public class Board {

	private static final int HOR = Combo.OrientationType.HORIZONTAL.ordinal();
	private static final int VER = Combo.OrientationType.VERTICAL.ordinal();
	private static final Cell BORDER_CELL = new Cell(0);
	private static final int MAX_DICTIONARY_CACHE_IN_REPORT = 30;

	private int boardWidth;
	private int boardHeight;

	private Cell[][][] cellMatrixArray;
	private Dictionary mainDictionary;
	private int maxNumberOfResults = 100;
	private Alphabet alphabet;
	private Scoring scoring;

	private class BoardIterator implements Iterator<Coord>, Iterable<Coord> {

		private Coord coord = new Coord(0, 0);
		private int width;
		private int height;

		BoardIterator() {
			this(boardWidth, boardHeight);
		}

		BoardIterator(int width, int height) {
			this.width = width;
			this.height = height;
		}

		public Iterator<Coord> iterator() {
			return this;
		}

		public boolean hasNext() {
			return coord.y < height;
		}

		public Coord next() {
			Coord result = new Coord(coord);
			coord.x++;
			if (coord.x >= width) {
				coord.x = 0;
				coord.y++;
			}
			return result;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	public Board(int width, int height, Dictionary dictionary, Alphabet alphabet, Scoring scoring) {
		this.alphabet = alphabet;
		this.scoring = scoring;
		boardWidth = width;
		boardHeight = height;
		mainDictionary = dictionary;
		cellMatrixArray = new Cell[2][][];
		cellMatrixArray[HOR] = new Cell[width][height];
		cellMatrixArray[VER] = new Cell[height][width];
		clear();
		for (Coord coord : new BoardIterator()) {
			int bonus = scoring.getBonusFor(coord);
			cellMatrixArray[HOR][coord.x][coord.y].bonus = bonus;
			cellMatrixArray[VER][coord.y][coord.x].bonus = bonus;
		}
	}

	public int getCode(int x, int y) {
		return alphabet.getTokenForValue(getCell(x, y, cellMatrixArray[HOR]).letter);
	}

	public int getBonus(int x, int y) {
		return getCell(x, y, cellMatrixArray[HOR]).bonus;
	}

	public void setCode(int x, int y, int code) {
		setValue(x, y, alphabet.getValueForToken(code));
	}

	private void setValue(int x, int y, int value) {
		getCell(x, y, cellMatrixArray[HOR]).letter = value;
		getCell(y, x, cellMatrixArray[VER]).letter = value;
	}

	public String info(Coord coord) {
		return "H: " + cellMatrixArray[HOR][coord.x][coord.y].toString() + "\nV: "
				+ cellMatrixArray[VER][coord.y][coord.x].toString();
	}

	public void clear() {
		for (Coord coord : new BoardIterator()) {
			cellMatrixArray[HOR][coord.x][coord.y] = new Cell(alphabet.getSize());
			cellMatrixArray[VER][coord.y][coord.x] = new Cell(alphabet.getSize());
		}
	}

	public Set<Combo> solve(String tokens, StringBuilder report) {
		ResultSet result = new ResultSet(maxNumberOfResults);
		alphabet.checkLegalTokens(tokens);
		report.append("----------------------------------------------------------------\n");
		report.append("solving: " + tokens + "\n");

		int[] freqArray = new int[alphabet.getSize() + 1];
		int jokerCount = alphabet.getFrequencyAndJoker(tokens, freqArray);
		String frequencyString = alphabet.frequencyToString(freqArray);
		report.append("frequency: " + frequencyString + "  jokers: " + jokerCount + "  total: "
				+ (freqArray[freqArray.length - 1] + jokerCount) + "\n");

		report.append("using: " + mainDictionary + "\n");
		Dictionary dictionary = mainDictionary.createFilteredDictionary(freqArray, jokerCount);
		Map<String, Dictionary> dictionaryCacheMap = new HashMap<String, Dictionary>();
		dictionaryCacheMap.put(frequencyString, dictionary);

		ResultSet verticalResultSet = new ResultSet(maxNumberOfResults);
		solveSingleOrientation(freqArray, jokerCount, dictionaryCacheMap, cellMatrixArray[VER], verticalResultSet);
		for (Combo combo : verticalResultSet) {
			result.add(combo.getMirror());
		}
		solveSingleOrientation(freqArray, jokerCount, dictionaryCacheMap, cellMatrixArray[HOR], result);

		report.append("dictionary-cache size: " + dictionaryCacheMap.size() + "\n");
		if (dictionaryCacheMap.size() < MAX_DICTIONARY_CACHE_IN_REPORT) {
			List<String> keyList = new ArrayList<String>(dictionaryCacheMap.keySet());
			Collections.sort(keyList, new Comparator<String>() {
				public int compare(String a, String b) {
					int lengthDiff = a.length() - b.length();
					if (lengthDiff != 0) {
						return lengthDiff;
					}
					return a.compareTo(b);
				}
			});
			for (String key : keyList) {
				report.append("  " + key + " = " + dictionaryCacheMap.get(key) + "\n");
			}
		}

		return result;
	}

	private void solveSingleOrientation(int[] freqArray, int jokerCount, Map<String, Dictionary> dictionaryCacheMap,
			Cell[][] cellMatrix, Set<Combo> outSet) {
		refreshCreditCache(cellMatrix);
		refreshFlags(cellMatrix);
		int tokenCount = freqArray[freqArray.length - 1] + jokerCount;
		refreshWordRange(tokenCount, cellMatrix);
		collectCombos(freqArray, jokerCount, dictionaryCacheMap, cellMatrix, outSet);
	}

	private void collectCombos(int[] freqArray, int jokerCount, Map<String, Dictionary> dictionaryCacheMap,
			Cell[][] cellMatrix, Set<Combo> outSet) {
		String defaultKey = alphabet.frequencyToString(freqArray);
		for (Coord coord : new BoardIterator(cellMatrix.length, cellMatrix[0].length)) {
			Cell start = cellMatrix[coord.x][coord.y];
			if (!start.isNoBegin && start.minWordLength > 0 && start.maxWordLength > 1) {
				int[] frequencySumArray = null;
				for (int l = 0; l < start.maxWordLength; l++) {
					Cell c = cellMatrix[coord.x + l][coord.y];
					if (c.letter != Alphabet.EMPTY_VALUE) {
						if (frequencySumArray == null) {
							frequencySumArray = Arrays.copyOf(freqArray, freqArray.length);
						}
						for (int i = 0; i < frequencySumArray.length; i++) {
							frequencySumArray[i] += c.beginOfWordWithFrequency[i];
						}
						l += c.beginOfWordWithFrequency[c.beginOfWordWithFrequency.length - 1] - 1;
					}
					if (l + 1 >= start.minWordLength && !c.isNoEnd) {
						Set<Word> candidateSet;
						if (frequencySumArray == null) {
							candidateSet = dictionaryCacheMap.get(defaultKey).get(l + 1);
						} else {
							String key = alphabet.frequencyToString(frequencySumArray);
							Dictionary dict = dictionaryCacheMap.get(key);
							if (dict == null) {
								dict = mainDictionary.createFilteredDictionary(frequencySumArray, jokerCount);
								dictionaryCacheMap.put(key, dict);
							}
							candidateSet = dict.get(l + 1);
						}
						if (candidateSet != null) {
							wordloop: for (Word word : candidateSet) {
								int empty = 0;
								for (int i = 0; i < word.length(); i++) {
									Cell d = cellMatrix[coord.x + i][coord.y];
									int e = word.valueArray[i];
									if (d.creditCacheArray[e] < 0) {
										continue wordloop;
									}
									if (d.letter == Alphabet.EMPTY_VALUE) {
										empty++;
									}
								}

								List<Cell> cellList = new LinkedList<Cell>();
								for (int i = 0; i < word.length(); i++) {
									cellList.add(cellMatrix[coord.x + i][coord.y]);
								}

								boolean usedAll = empty == freqArray[freqArray.length - 1] + jokerCount;
								int credit = scoring.getScore(word, usedAll, cellList);
								Combo combo = new Combo(word, new Coord(coord), credit);
								outSet.add(combo);
							}
						}
					}
				}
			}
		}
	}

	private void refreshCreditCache(Cell[][] cellMatrix) {
		for (Coord coord : new BoardIterator(cellMatrix.length, cellMatrix[0].length)) {
			Cell c = cellMatrix[coord.x][coord.y];
			if (c.letter == Alphabet.EMPTY_VALUE) {
				int[] pattern = getPattern(coord.x, coord.y, cellMatrix);
				if (pattern.length == 0) {
					throw new RuntimeException("Pattern has length 0 for cell: " + c);
				}
				if (pattern.length == 1) {
					if (pattern[0] != Alphabet.EMPTY_VALUE) {
						throw new RuntimeException("Single char pattern should be ANY: " + pattern[0]);
					}
					c.resetCreditCache(true);
				} else {
					c.resetCreditCache(false);
					mainDictionary.markAllowed(pattern, c.creditCacheArray, c.bonus, scoring);
				}
			} else if (c.letter == Alphabet.JOKER_VALUE) {
				// this means we're dealing with the first stone on the
				// board
				c.resetCreditCache(true);
			} else {
				c.resetCreditCache(false);
				c.creditCacheArray[c.letter] = 0;
			}
		}
	}

	private void refreshFlags(Cell[][] cellMatrix) {
		for (Coord coord : new BoardIterator(cellMatrix.length, cellMatrix[0].length)) {
			Cell c = cellMatrix[coord.x][coord.y];
			c.isNoBegin = getCell(coord.x - 1, coord.y, cellMatrix).letter != Alphabet.EMPTY_VALUE;
			c.isNoEnd = getCell(coord.x + 1, coord.y, cellMatrix).letter != Alphabet.EMPTY_VALUE;
			c.isYetConnected = getCell(coord.x, coord.y - 1, cellMatrix).letter != Alphabet.EMPTY_VALUE
					|| getCell(coord.x, coord.y + 1, cellMatrix).letter != Alphabet.EMPTY_VALUE
					|| getCell(coord.x, coord.y, cellMatrix).letter != Alphabet.EMPTY_VALUE;

			boolean isBeginOfWord = getCell(coord.x - 1, coord.y, cellMatrix).letter == Alphabet.EMPTY_VALUE
					&& c.letter != Alphabet.EMPTY_VALUE;
			if (isBeginOfWord && c.letter != Alphabet.JOKER_VALUE) {
				c.beginOfWordWithFrequency = getSequenceFrequency(coord.x, coord.y, cellMatrix);
			} else {
				c.beginOfWordWithFrequency = null;
			}
		}
	}

	private void refreshWordRange(int tokenCount, Cell[][] cellMatrix) {
		for (Coord coord : new BoardIterator(cellMatrix.length, cellMatrix[0].length)) {
			Cell c = cellMatrix[coord.x][coord.y];
			c.minWordLength = 0;
			c.maxWordLength = 0;
			if (!c.isNoBegin) {
				int maxWordLength = cellMatrix.length - coord.x;
				int empty = 0;
				boolean connected = false;
				for (int i = 0; i < maxWordLength; i++) {
					Cell d = cellMatrix[coord.x + i][coord.y];
					if (d.letter == Alphabet.EMPTY_VALUE) {
						empty++;
						if (empty > tokenCount) {
							break;
						}
					}
					if (!connected) {
						connected = d.isYetConnected;
					}
					if (connected && !d.isNoEnd && empty > 0) {
						c.maxWordLength = i + 1;
						if (c.minWordLength == 0) {
							c.minWordLength = i + 1;
						}
					}
				}
			}
		}
	}

	private int[] getPattern(int x, int y, Cell[][] cellMatrix) {
		ArrayList<Integer> array = new ArrayList<Integer>();
		for (int i = y - 1;; i--) {
			Cell c = getCell(x, i, cellMatrix);
			if (c.letter != Alphabet.EMPTY_VALUE) {
				array.add(Integer.valueOf(c.letter));
			} else {
				break;
			}
		}
		Collections.reverse(array);
		array.add(Integer.valueOf(Alphabet.EMPTY_VALUE));
		for (int i = y + 1;; i++) {
			Cell c = getCell(x, i, cellMatrix);
			if (c.letter != Alphabet.EMPTY_VALUE) {
				array.add(Integer.valueOf(c.letter));
			} else {
				break;
			}
		}
		int[] result = new int[array.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = array.get(i).intValue();
		}
		return result;
	}

	// in theory this needs to be done only once per cell per whole game
	private int[] getSequenceFrequency(int x, int y, Cell[][] cellMatrix) {
		int[] result = new int[alphabet.getSize() + 1];
		for (int i = x;; i++) {
			Cell c = getCell(i, y, cellMatrix);
			if (c.letter != Alphabet.EMPTY_VALUE) {
				result[c.letter]++;
			} else {
				result[result.length - 1] = i - x;
				break;
			}
		}
		return result;
	}

	protected static Cell getCell(int x, int y, Cell[][] cellMatrix) {
		if (x >= 0 && x < cellMatrix.length && y >= 0 && y < cellMatrix[x].length) {
			return cellMatrix[x][y];
		}
		return BORDER_CELL;
	}

	public Alphabet getAlphabet() {
		return alphabet;
	}

	public Scoring getScoring() {
		return scoring;
	}

	public Dictionary getDictionary() {
		return mainDictionary;
	}

	@Override
	public String toString() {
		return "Board(" + boardWidth + ", " + boardHeight + ")";
	}

}
