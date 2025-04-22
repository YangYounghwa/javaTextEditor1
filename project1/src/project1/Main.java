package project1;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.undo.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Main {
	private JFrame frame;
	private JTabbedPane tabbedPane;
	private JFileChooser fileChooser;
	private Timer autoSaveTimer;
	private final StyleContext styleContext = StyleContext.getDefaultStyleContext();
	private final AttributeSet defaultStyle = styleContext.getStyle(StyleContext.DEFAULT_STYLE);
	private final AttributeSet keywordStyle;
	private final String[] JAVA_KEYWORDS = {
		"abstract","assert","boolean","break","byte","case","catch","char",
		"class","const","continue","default","do","double","else","enum",
		"extends","final","finally","float","for","goto","if","implements",
		"import","instanceof","int","interface","long","native","new",
		"package","private","protected","public","return","short","static",
		"strictfp","super","switch","synchronized","this","throw","throws",
		"transient","try","void","volatile","while"
	};

	private final Map<JTextPane, UndoManager> undoMap = new HashMap<>();

	public Main() {
		frame = new JFrame("Tabbed Java Editor");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 600);

		tabbedPane = new JTabbedPane();
		frame.add(tabbedPane, BorderLayout.CENTER);

		fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt", "text"));

		Style s = styleContext.addStyle("Keyword_Style", null);
		StyleConstants.setForeground(s, Color.BLUE);
		StyleConstants.setBold(s, true);
		keywordStyle = styleContext.getStyle("Keyword_Style");

		setupMenuBar();
		onNew();

		autoSaveTimer = new Timer(60000, e -> autoSave());
		autoSaveTimer.setRepeats(true);
		autoSaveTimer.start();

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private void setupMenuBar() {
		JMenuBar menuBar = new JMenuBar();

		JMenu fileMenu = new JMenu("File");
		JMenuItem newItem = new JMenuItem("New");
		newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
		newItem.addActionListener(e -> onNew());
		fileMenu.add(newItem);

		JMenuItem openItem = new JMenuItem("Open...");
		openItem.addActionListener(e -> onOpen());
		fileMenu.add(openItem);

		JMenuItem saveItem = new JMenuItem("Save...");
		saveItem.addActionListener(e -> onSave(getCurrentEditor()));
		fileMenu.add(saveItem);

		JMenu editMenu = new JMenu("Edit");
		JMenuItem findReplaceItem = new JMenuItem("Find/Replace...");
		findReplaceItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK));
		findReplaceItem.addActionListener(e -> {
			JTextPane editor = getCurrentEditor();
			if (editor != null) new FindReplaceDialog(frame, editor).setVisible(true);
		});
		editMenu.add(findReplaceItem);

		JMenuItem undoItem = new JMenuItem("Undo");
		undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
		undoItem.addActionListener(e -> {
			JTextPane editor = getCurrentEditor();
			if (editor != null) {
				UndoManager um = undoMap.get(editor);
				if (um != null && um.canUndo()) um.undo();
			}
		});
		editMenu.add(undoItem);

		JMenuItem redoItem = new JMenuItem("Redo");
		redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));
		redoItem.addActionListener(e -> {
			JTextPane editor = getCurrentEditor();
			if (editor != null) {
				UndoManager um = undoMap.get(editor);
				if (um != null && um.canRedo()) um.redo();
			}
		});
		editMenu.add(redoItem);

		JMenu formatMenu = new JMenu("Format");
		JMenuItem fontItem = new JMenuItem("Font...");
		fontItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		fontItem.addActionListener(e -> {
			JTextPane editor = getCurrentEditor();
			if (editor != null) {
				JScrollPane scroll = (JScrollPane) tabbedPane.getSelectedComponent();
				Folding folding = (Folding) scroll.getRowHeader().getView();
				new FontDialog(frame, editor, folding).setVisible(true);
			}
		});
		formatMenu.add(fontItem);

		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(formatMenu);
		frame.setJMenuBar(menuBar);
	}

	private void onNew() {
		JTextPane editor = createEditor();
		JScrollPane scrollPane = new JScrollPane(editor);
		Folding folding = new Folding(editor);
		scrollPane.setRowHeaderView(folding);
		tabbedPane.addTab("Untitled", scrollPane);
		tabbedPane.setSelectedComponent(scrollPane);
	}

	private JTextPane createEditor() {
		JTextPane textPane = new JTextPane();
		textPane.setFont(new Font("Monospaced", Font.PLAIN, 14));

		UndoManager undoManager = new UndoManager();
		textPane.getDocument().addUndoableEditListener(undoManager);
		undoMap.put(textPane, undoManager);

		textPane.getDocument().addDocumentListener(new DocumentListener() {
			@Override public void insertUpdate(DocumentEvent e) {
    SwingUtilities.invokeLater(() -> maybeHighlight(textPane));
}
			@Override public void removeUpdate(DocumentEvent e) {
    SwingUtilities.invokeLater(() -> maybeHighlight(textPane));
}
			@Override public void changedUpdate(DocumentEvent e) {}
		});
		return textPane;
	}

	private JTextPane getCurrentEditor() {
		Component comp = tabbedPane.getSelectedComponent();
		if (comp instanceof JScrollPane) {
			JViewport viewport = ((JScrollPane) comp).getViewport();
			Component view = viewport.getView();
			if (view instanceof JTextPane) return (JTextPane) view;
		}
		return null;
	}

	private void onOpen() {
		if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
				JTextPane editor = createEditor();
				editor.read(reader, null);
				JScrollPane scrollPane = new JScrollPane(editor);
				Folding folding = new Folding(editor);
				scrollPane.setRowHeaderView(folding);
				tabbedPane.addTab(file.getName(), scrollPane);
				tabbedPane.setSelectedComponent(scrollPane);
			} catch (IOException ex) {
				showError("Error opening file:\n" + ex.getMessage());
			}
		}
	}

	private void onSave(JTextPane editor) {
		if (editor == null) return;
		if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
				editor.write(writer);
				tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), file.getName());
			} catch (IOException ex) {
				showError("Error saving file:\n" + ex.getMessage());
			}
		}
	}

	private void autoSave() {
		JTextPane editor = getCurrentEditor();
		if (editor != null) {
			try (BufferedWriter writer = new BufferedWriter(new FileWriter("autosave.java"))) {
				editor.write(writer);
			} catch (IOException ex) {
				System.err.println("Autosave failed: " + ex.getMessage());
			}
		}
	}

	private void maybeHighlight(JTextPane textPane) {
		StyledDocument doc = textPane.getStyledDocument();
		try {
			String text = doc.getText(0, doc.getLength());
			doc.setCharacterAttributes(0, text.length(), defaultStyle, true);
			for (String kw : JAVA_KEYWORDS) {
				int pos = 0;
				while ((pos = text.indexOf(kw, pos)) >= 0) {
					boolean leftOK = pos == 0 || !Character.isJavaIdentifierPart(text.charAt(pos - 1));
					boolean rightOK = (pos + kw.length() == text.length()) || !Character.isJavaIdentifierPart(text.charAt(pos + kw.length()));
					if (leftOK && rightOK) {
						doc.setCharacterAttributes(pos, kw.length(), keywordStyle, false);
					}
					pos += kw.length();
				}
			}
		} catch (BadLocationException ignored) {}
	}

	private void showError(String msg) {
		JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(Main::new);
	}
} 
