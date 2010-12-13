package net.sf.scrabble.gui;

import java.awt.BorderLayout;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sf.scrabble.core.Alphabet;
import net.sf.scrabble.core.Board;
import net.sf.scrabble.core.Combo;
import net.sf.scrabble.core.Coord;
import net.sf.scrabble.local.DutchFactory;
import net.sf.scrabble.local.ScrabbleFactory;

public class ScrabbleComponent extends JFrame {

	private static final long serialVersionUID = 1L;

	private BoardComponent boardComponent;
	private JTextArea infoArea;
	private JList comboList;
	private JTextField tokenField;
	private JLabel statusLabel;

	private ScrabbleFactory scrabbleFactory = new DutchFactory();
	private Alphabet alphabet;
	private Board board;

	public class ComboEntry {

		private Combo combo;

		public ComboEntry(Combo combo) {
			this.combo = combo;
		}

		@Override
		public String toString() {
			Integer credits = Integer.valueOf(combo.getCredits());
			String word = combo.getString(alphabet);
			Coord coord = combo.getCoord();
			Integer x = Integer.valueOf(coord.x + 1);
			Integer y = Integer.valueOf(coord.y + 1);
			String orientation = combo.isHorizontal() ? "H" : "V";
			return String.format("%2d %-8s  %2d,%2d  %S", credits, word, x, y, orientation);
		}
	}

	public ScrabbleComponent() {
		initComponents();
		initWindow();
		statusLabel.setText("Startup complete");
	}

	private void updateInfo(Coord coord) {
		StringBuilder builder = new StringBuilder();
		builder.append(boardComponent.getBoard().info(coord));
		infoArea.setText(builder.toString());
	}

	private void initWindow() {
		setTitle("Scrabble");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(800, 600);
	}

	private void initComponents() {
		board = scrabbleFactory.createBoard();
		alphabet = board.getAlphabet();

		boardComponent = new BoardComponent(alphabet, board);
		// try {
		// boardComponent.loadDictionary();
		// } catch (IOException e1) {
		// e1.printStackTrace();
		// }
		boardComponent.addBoardListener(new BoardListener() {
			public void selected(Coord coord) {
				updateInfo(coord);
			}
		});

		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					FileDialog dialog = new FileDialog(ScrabbleComponent.this, "Select Scrabble file", FileDialog.SAVE);
					dialog.setVisible(true);
					if (dialog.getFile() != null) {
						String path = dialog.getDirectory() + dialog.getFile();
						Writer writer = new OutputStreamWriter(new FileOutputStream(path), "UTF-8");
						writer.write(boardComponent.toStringStorage());
						writer.flush();
						statusLabel.setText("Save successful");
					}
				} catch (Exception ex) {
					statusLabel.setText("ERROR: " + ex);
					ex.printStackTrace();
				}
			}
		});

		JButton loadButton = new JButton("Load");
		loadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					FileDialog dialog = new FileDialog(ScrabbleComponent.this, "Select Scrabble file", FileDialog.LOAD);
					dialog.setVisible(true);
					if (dialog.getFile() != null) {
						String path = dialog.getDirectory() + dialog.getFile();
						File f = new File(path);
						if (!f.exists()) {
							f.createNewFile();
						}
						BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path),
								"UTF-8"));
						StringBuilder builder = new StringBuilder();
						for (String line = reader.readLine(); line != null; line = reader.readLine()) {
							builder.append(line);
							builder.append("\n");
						}
						if (builder.length() == 0) {
							System.out.println("Nothing was read");
						} else {
							boardComponent.fromStringStorage(builder.toString());
						}
						statusLabel.setText("Load successful");
					}
				} catch (Exception ex) {
					statusLabel.setText("ERROR: " + ex);
					ex.printStackTrace();
				}
			}
		});

		JPanel northPanel = new JPanel();
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.X_AXIS));
		northPanel.add(loadButton);
		northPanel.add(saveButton);

		JButton refreshButton = new JButton("Refresh");
		refreshButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					StringBuilder report = new StringBuilder();
					Set<Combo> comboSet = boardComponent.solveBoard(tokenField.getText().toLowerCase(), report);
					DefaultListModel listModel = (DefaultListModel) comboList.getModel();
					listModel.removeAllElements();
					for (Combo tuple : comboSet) {
						listModel.addElement(new ComboEntry(tuple));
					}
					System.out.println(report);
					statusLabel.setText("Search done, " + comboSet.size() + " results");
				} catch (Exception ex) {
					statusLabel.setText("ERROR: " + ex);
					ex.printStackTrace();
				}
			}
		});

		tokenField = new JTextField();

		JPanel applyPanel = new JPanel(new BorderLayout());
		applyPanel.add(refreshButton, BorderLayout.NORTH);
		applyPanel.add(tokenField, BorderLayout.SOUTH);

		comboList = new JList(new DefaultListModel());
		comboList.setFont(new Font("monospaced", Font.PLAIN, 11));
		comboList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				ComboEntry comboEntry = (ComboEntry) comboList.getSelectedValue();
				if (comboEntry != null) {
					boardComponent.applyCombo(comboEntry.combo);
				}
			}
		});

		JPanel westPanel = new JPanel(new BorderLayout());
		westPanel.add(applyPanel, BorderLayout.NORTH);
		westPanel.add(new JScrollPane(comboList), BorderLayout.CENTER);

		infoArea = new JTextArea();
		statusLabel = new JLabel("Starting up..");
		JPanel southPanel = new JPanel(new BorderLayout());
		southPanel.add(infoArea, BorderLayout.CENTER);
		southPanel.add(statusLabel, BorderLayout.SOUTH);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(northPanel, BorderLayout.NORTH);
		getContentPane().add(boardComponent, BorderLayout.CENTER);
		getContentPane().add(westPanel, BorderLayout.EAST);
		getContentPane().add(southPanel, BorderLayout.SOUTH);
	}

	public static void main(String[] args) {
		ScrabbleComponent frame = new ScrabbleComponent();
		frame.setVisible(true);
	}
}
