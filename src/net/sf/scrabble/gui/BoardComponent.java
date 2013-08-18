package net.sf.scrabble.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import net.sf.scrabble.core.Alphabet;
import net.sf.scrabble.core.Board;
import net.sf.scrabble.core.Combo;
import net.sf.scrabble.core.Coord;
import net.sf.scrabble.core.Scoring;
import net.sf.scrabble.core.Combo.Assigment;

public class BoardComponent extends JPanel {
	private static final long serialVersionUID = 1L;
	private final static int BOARD_WIDTH = 15;
	private final static int BOARD_HEIGHT = 15;
	private final static Color TRIPPLE_LETTER = Color.getHSBColor(.55f, .80f, .80f);
	private final static Color DOUBLE_LETTER = Color.getHSBColor(.00f, .00f, .70f);
	private final static Color DEFAULT = Color.getHSBColor(.50f, .60f, .80f);
	private final static Color DOUBLE_WORD = Color.getHSBColor(.05f, .60f, .90f);
	private final static Color TRIPPLE_WORD = Color.getHSBColor(1.f, .60f, .90f);
	private Board board;
	private Alphabet alphabet;
	private JTextField[][] fieldMatrix = new JTextField[BOARD_WIDTH][BOARD_HEIGHT];
	private Map<JTextField, Coord> fieldMap = new HashMap<JTextField, Coord>();
	private List<BoardListener> listenerList = new LinkedList<BoardListener>();
	private Scoring scoring;

	public BoardComponent(Board board) {
		this.board = board;
		this.alphabet = board.getAlphabet();
		this.scoring = board.getScoring();
		initComponents();
	}

	public void addBoardListener(BoardListener listener) {
		listenerList.add(listener);
	}

	public Board getBoard() {
		return board;
	}

	public void applyCombo(Combo combo) {
		readBoard();
		List<Assigment> list = combo.getAssignmentList(alphabet);
		for (Assigment assigment : list) {
			setField(assigment.x, assigment.y, assigment.c);
		}
	}

	private void setField(int x, int y, int c) {
		setField(x, y, Character.toString((char) c));
	}

	private void setField(int x, int y, String s) {
		fieldMatrix[x][y].setText(filterString(s).toUpperCase());
	}

	private int getField(int x, int y) {
		return filterString(fieldMatrix[x][y].getText().toUpperCase()).codePointAt(0);
	}

	private String filterString(String s) {
		s = s.trim();
		if (s.length() == 0) {
			s = " ";
		} else if (s.length() > 1) {
			s = s.substring(0, 1);
		}
		return s;
	}

	public Set<Combo> solveBoard(String tokens, StringBuilder report) {
		writeBoard();
		return board.solve(tokens.toUpperCase(), report);
	}

	public void writeBoard() {
		for (int y = 0; y < BOARD_HEIGHT; y++) {
			for (int x = 0; x < BOARD_WIDTH; x++) {
				board.setCode(x, y, getField(x, y));
			}
		}
	}

	public void readBoard() {
		for (int y = 0; y < BOARD_HEIGHT; y++) {
			for (int x = 0; x < BOARD_WIDTH; x++) {
				setField(x, y, board.getCode(x, y));
			}
		}
	}

	public String toStringStorage() {
		StringBuilder builder = new StringBuilder();
		for (int y = 0; y < BOARD_HEIGHT; y++) {
			for (int x = 0; x < BOARD_WIDTH; x++) {
				builder.append((char) getField(x, y));
			}
			builder.append("\n");
		}
		return builder.toString();
	}

	public void fromStringStorage(String content) {
		String[] split = content.split("\n");
		if (split.length < BOARD_HEIGHT) {
			System.out.println("Unable to load: incorrect height: " + split.length + " < " + BOARD_HEIGHT);
			return;
		}
		for (int y = 0; y < BOARD_HEIGHT; y++) {
			String line = split[y];
			if (line.length() < BOARD_WIDTH) {
				System.out.println("Unable to load: incorrect width: " + line.length() + " < " + BOARD_WIDTH);
				return;
			}
			for (int x = 0; x < BOARD_WIDTH; x++) {
				setField(x, y, line.charAt(x));
			}
		}
	}

	private void initComponents() {
		setLayout(new GridLayout(BOARD_HEIGHT + 1, BOARD_WIDTH + 1, 1, 1));
		for (int y = 0; y < BOARD_HEIGHT; y++) {
			if (y == 0) {
				add(new JLabel(" ", SwingConstants.CENTER));
				for (int x = 0; x < BOARD_WIDTH; x++) {
					add(new JLabel("" + (x + 1), SwingConstants.CENTER));
				}
			}
			for (int x = 0; x < BOARD_WIDTH; x++) {
				if (x == 0) {
					add(new JLabel("" + (y + 1), SwingConstants.CENTER));
				}
				JTextField field = new JTextField();
				field.setHorizontalAlignment(JTextField.CENTER);
				int bonus = scoring.getBonusFor(new Coord(x, y));
				Color color;
				switch (bonus) {
				case -3:
					color = TRIPPLE_LETTER;
					break;
				case -2:
					color = DOUBLE_LETTER;
					break;
				case 0:
					color = DEFAULT;
					break;
				case 2:
					color = DOUBLE_WORD;
					break;
				case 3:
					color = TRIPPLE_WORD;
					break;
				default:
					throw new IllegalStateException("Unhandeld case: " + bonus);
				}
				field.setBackground(color);
				// field.setForeground(Color.white);
				field.setFont(getFont().deriveFont(Font.BOLD));
				field.addFocusListener(new FocusListener() {
					public void focusGained(FocusEvent e) {
						JTextField source = (JTextField) e.getSource();
						Coord coord = fieldMap.get(source);
						for (BoardListener listener : listenerList) {
							listener.selected(coord);
						}
					}

					public void focusLost(FocusEvent e) {
					}
				});
				field.addKeyListener(new KeyListener() {
					public void keyTyped(KeyEvent e) {
					}

					public void keyReleased(KeyEvent e) {
					}

					public void keyPressed(KeyEvent e) {
						Coord coord = new Coord(fieldMap.get(e.getSource()));
						switch (e.getKeyCode()) {
						case KeyEvent.VK_UP:
							if (coord.y > 0)
								coord.y--;
							break;
						case KeyEvent.VK_DOWN:
							if (coord.y + 1 < BOARD_WIDTH)
								coord.y++;
							break;
						case KeyEvent.VK_LEFT:
							if (coord.x > 0)
								coord.x--;
							break;
						case KeyEvent.VK_RIGHT:
							if (coord.x + 1 < BOARD_WIDTH)
								coord.x++;
							break;
						}
						fieldMatrix[coord.x][coord.y].requestFocusInWindow();
					}
				});
				add(field);
				fieldMatrix[x][y] = field;
				fieldMap.put(field, new Coord(x, y));
			}
		}
		setField(BOARD_WIDTH / 2, BOARD_HEIGHT / 2, "_");
	}
}
