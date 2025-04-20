package project1;
//
//asdfasdfasdfasdfasdf

import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.*;
import java.io.*;


// My classes
import project1.FindReplaceDialog;


public class Main {
	private JFrame frame;
	private JTextPane textPane;
	private JFileChooser fileChooser;
	
	// 1) Add an UndoManager
	private final UndoManager undoManager = new UndoManager();
	
	public Main() {
		frame = new JFrame("Simple Java Editor");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800,600);
		
		textPane =  new JTextPane();
		// 2) Register the UndoManager on the document
		textPane.getDocument().addUndoableEditListener(new UndoableEditListener(){
			@Override
			public void undoableEditHappened(UndoableEditEvent e){
				undoManager.addEdit(e.getEdit());
			}
		});
		
		JScrollPane scrollPane = new JScrollPane(textPane);
		LineNumberView lineNumbers = new LineNumberView(textPane);
		scrollPane.setRowHeaderView(lineNumbers);
		frame.add(scrollPane, BorderLayout.CENTER);
		
		
		fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files","txt","text"));
		
		
		// 3) Build the menubar with file + edit
		JMenuBar menuBar = new JMenuBar();
		
		JMenu fileMenu = new JMenu("File");
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
		
		// Undo/Redo
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
		frame.setVisible(true);
	}
	
	// onOpen and onSave
	
	private void onOpen() {
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                textPane.read(reader, null);
                frame.setTitle("Simple Java Editor – " + file.getName());
            } catch (IOException ex) {
                showError("Error opening file:\n" + ex.getMessage());
            }
        }
    }

    private void onSave() {
        int result = fileChooser.showSaveDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                textPane.write(writer);
                frame.setTitle("Simple Java Editor – " + file.getName());
            } catch (IOException ex) {
                showError("Error saving file:\n" + ex.getMessage());
            }
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
}