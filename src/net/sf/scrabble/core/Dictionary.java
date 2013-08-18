package net.sf.scrabble.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * A collection of words that can be scrabbled, including some routines to load
 * from file.
 */
public class Dictionary {
	private final static Map<Integer, Integer> replaceMap = new HashMap<Integer, Integer>();
	static {
		replaceMap.put(Integer.valueOf('à'), Integer.valueOf('A'));
		replaceMap.put(Integer.valueOf('â'), Integer.valueOf('A'));
		replaceMap.put(Integer.valueOf('ë'), Integer.valueOf('E'));
		replaceMap.put(Integer.valueOf('é'), Integer.valueOf('E'));
		replaceMap.put(Integer.valueOf('è'), Integer.valueOf('E'));
		replaceMap.put(Integer.valueOf('ê'), Integer.valueOf('E'));
		replaceMap.put(Integer.valueOf('ï'), Integer.valueOf('I'));
		replaceMap.put(Integer.valueOf('î'), Integer.valueOf('I'));
		replaceMap.put(Integer.valueOf('ó'), Integer.valueOf('O'));
		replaceMap.put(Integer.valueOf('ô'), Integer.valueOf('O'));
		replaceMap.put(Integer.valueOf('û'), Integer.valueOf('U'));
		replaceMap.put(Integer.valueOf('ç'), Integer.valueOf('C'));
		replaceMap.put(Integer.valueOf('ñ'), Integer.valueOf('N'));
		List<Entry<Integer, Integer>> temp = new LinkedList<Entry<Integer, Integer>>(replaceMap.entrySet());
		for (Entry<Integer, Integer> entry : temp) {
			String s = new String(new int[] { entry.getKey().intValue() }, 0, 1);
			replaceMap.put(Integer.valueOf(s.toUpperCase().codePointAt(0)), entry.getValue());
		}
		for (int i = 'A'; i <= 'Z'; i++) {
			replaceMap.put(Integer.valueOf(i - 'A' + 'a'), Integer.valueOf(i));
		}
	}
	protected Map<Integer, Set<Word>> wordSetMap = new HashMap<Integer, Set<Word>>();

	public void add(Word word) {
		Set<Word> set = getOrCreate(word.length());
		set.add(word);
	}

	protected Set<Word> getOrCreate(int length) {
		Set<Word> result = wordSetMap.get(Integer.valueOf(length));
		if (result == null) {
			result = new HashSet<Word>();
			wordSetMap.put(Integer.valueOf(length), result);
		}
		return result;
	}

	protected Set<Word> get(int length) {
		return wordSetMap.get(Integer.valueOf(length));
	}

	protected Set<Integer> getLengthSet() {
		return wordSetMap.keySet();
	}

	protected Dictionary createFilteredDictionary(int[] sequence) {
		Dictionary result = new Dictionary();
		for (Entry<Integer, Set<Word>> entry : wordSetMap.entrySet()) {
			if (entry.getKey().intValue() > sequence.length) {
				Set<Word> set = entry.getValue();
				for (Word word : set) {
					if (word.contains(sequence)) {
						result.add(word);
					}
				}
			}
		}
		return result;
	}

	protected Dictionary createFilteredDictionary(int[] freqArray, int jokerCount) {
		Dictionary result = new Dictionary();
		for (Entry<Integer, Set<Word>> entry : wordSetMap.entrySet()) {
			if (entry.getKey().intValue() <= freqArray[freqArray.length - 1] + jokerCount) {
				Set<Word> set = entry.getValue();
				for (Word word : set) {
					if (word.canBeMadeOutOf(freqArray, jokerCount)) {
						result.add(word);
					}
				}
			}
		}
		return result;
	}

	protected void markAllowed(int[] pattern, int[] outCredit, int bonus, Scoring scoring) {
		int index = -1;
		for (int i = 0; i < pattern.length; i++) {
			if (pattern[i] == Alphabet.EMPTY_VALUE) {
				if (index >= 0) {
					throw new RuntimeException("Pattern contains multiple ANY chars: " + Word.toString(pattern));
				}
				index = i;
			}
		}
		if (index < 0) {
			throw new RuntimeException("Pattern does not contain the ANY char: " + Word.toString(pattern));
		}
		Set<Word> set = wordSetMap.get(Integer.valueOf(pattern.length));
		if (set != null) {
			for (Word word : set) {
				if (word.matches(pattern)) {
					int value = word.valueArray[index];
					outCredit[value] = scoring.getScore(word, value, bonus);
				}
			}
		}
	}

	public static Dictionary readFrom(Reader r, Alphabet alphabet, Scoring scoring) throws IOException {
		Dictionary result = new Dictionary();
		BufferedReader reader;
		if (r instanceof BufferedReader) {
			reader = (BufferedReader) r;
		} else {
			reader = new BufferedReader(r);
		}
		for (String line = reader.readLine(); line != null; line = reader.readLine()) {
			char[] array = line.toCharArray();
			for (int i = 0; i < array.length; i++) {
				Integer boxed = replaceMap.get(Integer.valueOf(array[i]));
				if (boxed != null) {
					array[i] = (char) boxed.intValue();
				}
			}
			result.add(Word.createFrom(new String(array), alphabet, scoring));
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Dictionary(");
		int sum = 0;
		for (Entry<Integer, Set<Word>> entry : wordSetMap.entrySet()) {
			builder.append(entry.getKey() + ":" + entry.getValue().size() + " ");
			sum += entry.getValue().size();
		}
		builder.append("#:" + sum);
		builder.append(")");
		return builder.toString();
	}
}
