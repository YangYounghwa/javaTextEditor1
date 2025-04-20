package project1;

import javax.swing.*;										// JFrame, JTextPane, JMenuBar ..
import javax.swing.event.*;  //DocumentEvent, DocumentListener, UndoableEditListener, etc.
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.UndoManager;
import javax.swing.text.*;  //StyleContext, Style, StyleConstants, AttributeSet, StyledDocument, BadLocationException
import java.awt.*;   //color
import java.awt.event.*;  // for KeyEvent, InputEvent, ActionEvent
import java.io.*;  // File, IOException, BufferedReader/Writer
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.io.File;


// My classes
import project1.FindReplaceDialog;
import project1.FontDialog;

public class Main {
	private JFrame frame;
	private JTextPane textPane;
	private JFileChooser fileChooser;
	private File currentFile;
	
	private LineNumberView lineNumberView;
	
	
	private static final String[] JAVA_KEYWORDS = {
			 "abstract","assert","boolean","break","byte","case","catch","char",
			    "class","const","continue","default","do","double","else","enum",
			    "extends","final","finally","float","for","goto","if","implements",
			    "import","instanceof","int","interface","long","native","new",
			    "package","private","protected","public","return","short","static",
			    "strictfp","super","switch","synchronized","this","throw","throws",
			    "transient","try","void","volatile","while"
	};
	
	private final StyleContext styleContext = StyleContext.getDefaultStyleContext();
	private final AttributeSet defaultStyle = styleContext.getStyle(StyleContext.DEFAULT_STYLE);
	private final AttributeSet keywordStyle;
	
	
	
	
	// 1) Add an UndoManager
	private final UndoManager undoManager = new UndoManager();
	
	public Main() {
		frame = new JFrame("Simple Java Editor");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.setSize(800,600);
		
		textPane =  new JTextPane();
		textPane.setEditable(false);
		
		// 2) Register the UndoManager on the document
		textPane.getDocument().addUndoableEditListener(new UndoableEditListener(){
			@Override
			public void undoableEditHappened(UndoableEditEvent e){
				undoManager.addEdit(e.getEdit());
			}
		});
		
		
		lineNumberView = new LineNumberView(textPane);
		JScrollPane scrollPane = new JScrollPane(textPane);
		scrollPane.setRowHeaderView(lineNumberView);

		frame.add(scrollPane, BorderLayout.CENTER);
		
		
		fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files","txt","text"));
		
		
		// 3) Build the menubar with file + edit
		JMenuBar menuBar = new JMenuBar();
		
		JMenu fileMenu = new JMenu("File");
		
		// New
		JMenuItem newItem = new JMenuItem("New");
		newItem.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK)
				);
		newItem.addActionListener(e -> onNew());
		fileMenu.add(newItem);
		
		fileMenu.addSeparator();
		JMenuItem openItem = new JMenuItem("Open...");
		openItem.addActionListener(e->onOpen());
		JMenuItem saveItem = new JMenuItem("Save...");
		saveItem.addActionListener(e -> onSave());
		fileMenu.add(openItem);
		fileMenu.add(saveItem);
		
		
		// - edit menu 
		JMenu editMenu = new JMenu("Edit");
		
		// Cut
		JMenuItem cutItem = new JMenuItem("Cut");
		cutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,InputEvent.CTRL_DOWN_MASK));
		cutItem.addActionListener(e -> textPane.cut());
		editMenu.add(cutItem);
		
		// Copy
		JMenuItem copyItem = new JMenuItem("Copy");
		copyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
		copyItem.addActionListener( e-> textPane.copy());
		editMenu.add(copyItem);
		
		// Paste
		JMenuItem pasteItem = new JMenuItem("Paste");
		pasteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
		pasteItem.addActionListener(e -> textPane.paste());
		editMenu.add(pasteItem);
		
		editMenu.addSeparator();
		
		// Undo Redo
		JMenuItem undoItem = new JMenuItem("Undo");
		undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
		undoItem.addActionListener( e -> onUndo());
		
		JMenuItem redoItem = new JMenuItem("Redo");
		redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));
		redoItem.addActionListener(e->onRedo());
		
		editMenu.add(undoItem);
		editMenu.add(redoItem);
		
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		
		JMenu formatMenu = new JMenu("Format");
		JMenuItem fontItem = new JMenuItem("Font...");
		fontItem.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK)
				);
		fontItem.addActionListener( e ->
			new FontDialog(frame, textPane, lineNumberView).setVisible(true)
		);
		
		formatMenu.add(fontItem);
		menuBar.add(formatMenu);
		
		frame.setJMenuBar(menuBar);
		
		JMenuItem findReplaceItem = new JMenuItem("Find/Replace...");
		findReplaceItem.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK)
				);
		findReplaceItem.addActionListener(e->{
			new FindReplaceDialog(frame, textPane).setVisible(true);
		});
		editMenu.addSeparator();
		editMenu.add(findReplaceItem);
		
		// Find, Replace
		
		
		
		// 4) Also bind keys on the textPane so menu state updates correctly
		textPane.getInputMap().put(
			KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK),
			"Undo"
			);
		textPane.getActionMap().put("Undo",new AbstractAction() {
			@Override public void actionPerformed(ActionEvent e) { onUndo(); }
			});
		textPane.getInputMap().put(
			KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK),
			"Redo"
		);
		textPane.getActionMap().put("Redo", new AbstractAction() {
			@Override public void actionPerformed(ActionEvent e) { onRedo();}
		});
		
		frame.setLocationRelativeTo(null);
		
		Style s = styleContext.addStyle("Keyword_Style",null);
		StyleConstants.setForeground(s,Color.BLUE);
		StyleConstants.setBold(s,true);
		keywordStyle = styleContext.getStyle("Keyword_Style");
		
		textPane.getDocument().addDocumentListener(new DocumentListener(){
			@Override public void insertUpdate(DocumentEvent e) { maybeHighlight();}
			@Override public void removeUpdate(DocumentEvent e) { maybeHighlight();}
			@Override public void changedUpdate(DocumentEvent e)  { /* ignore */ }
		});
		
		
		onNew();
		frame.setVisible(true);
	}
	
	// onOpen and onSave
	
	private void onNew() {
		// 
		textPane.setText("");
		
		//currentFile = null;
		currentFile = new File("Untitled.java"); 
		textPane.setEditable(true);
		frame.setTitle("Simple Java Editor - Untitled");
		textPane.setEditable(true);
		maybeHighlight();
	}
	
	private void onOpen() {
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
        	currentFile = fileChooser.getSelectedFile();
            File file = fileChooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                textPane.read(reader, null);
                frame.setTitle("Simple Java Editor – " + file.getName());
                
            } catch (IOException ex) {
                showError("Error opening file:\n" + ex.getMessage());
            }
            textPane.setEditable(true);
            maybeHighlight();
        }
    }

    private void onSave() {
        int result = fileChooser.showSaveDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
        	currentFile = fileChooser.getSelectedFile();
            File file = fileChooser.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                textPane.write(writer);
                frame.setTitle("Simple Java Editor – " + file.getName());
            } catch (IOException ex) {
                showError("Error saving file:\n" + ex.getMessage());
            }
            maybeHighlight();
        }
    }	
		
	private void onUndo() {
		if (undoManager.canUndo()) {
			try {
				undoManager.undo();
			} catch (CannotUndoException ex) {
			}
		}
	}
	
	private void onRedo() {
		if (undoManager.canRedo()) {
			try {
				undoManager.redo();
			} catch (CannotRedoException ex) {
			}
		}
	}
	

	
	private void showError(String msg) {
		JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE);
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(Main::new);
	}
	
	private void maybeHighlight() {
		if (currentFile == null || !currentFile.getName().endsWith(".java")){
			// clear all styles, restore default
			StyledDocument doc = textPane.getStyledDocument();
			doc.setCharacterAttributes(0, doc.getLength(), defaultStyle, true);
			return;
		}
		
		SwingUtilities.invokeLater( () -> {
			StyledDocument doc = textPane.getStyledDocument();
			String text;
			try {
				text = doc.getText(0, doc.getLength());
			} catch (BadLocationException ex) {
				return;
				}
			//1)reset to default
			doc.setCharacterAttributes(0,text.length(),defaultStyle,true);
			// 2) for each keyword, find and style
			for (String kw : JAVA_KEYWORDS) {
				int pos = 0;
				while ((pos = text.indexOf(kw, pos)) >= 0){
					//ensure it's a standalone word
					boolean leftOK = pos == 0 || !Character.isJavaIdentifierPart(text.charAt(pos -1));
					boolean rightOK = (pos+kw.length() == text.length())
							|| !Character.isJavaIdentifierPart(text.charAt(pos+kw.length()));
					if (leftOK && rightOK) {
						doc.setCharacterAttributes(pos,kw.length(),keywordStyle,false);
					}
					pos += kw.length();
				}
			}
		}
		);
		
	}
}