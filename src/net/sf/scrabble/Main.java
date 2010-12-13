package net.sf.scrabble;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import net.sf.scrabble.gui.ScrabbleComponent;

public class Main {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ScrabbleComponent component = new ScrabbleComponent();
				JFrame mainFrame = new JFrame("Scrabble");
				mainFrame.setLayout(new BorderLayout());
				mainFrame.add(component, BorderLayout.CENTER);
				mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				mainFrame.setSize(800, 600);
				mainFrame.setVisible(true);
			}
		});
	}
}
