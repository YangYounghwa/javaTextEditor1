package project1;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.*;



public class FindReplaceDialog extends JDialog {
	private final JTextPane textPane;
	private final JTextField findField	= new JTextField(20);
	private final JTextField replaceField	= new JTextField(20);
	private int lastMatchPos = 0;
	private String lastSearchTerm = "";
	
	public FindReplaceDialog(JFrame owner, JTextPane textPane) {
		super(owner, "Find & Replace", false);
		this.textPane = textPane;
		initComponents();
	}
	
	private void initComponents() {
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(4,4,4,4);
		gbc.anchor = GridBagConstraints.WEST;
		
		// Row 0 : Find label + field
		gbc.gridx = 0; gbc.gridy = 0; 
		add(new JLabel("Find:"),gbc);
		gbc.gridx =1; add(findField,gbc);
		
		// Row 1 : Replace label + field
		gbc.gridx = 0; gbc.gridy=1;
		add(new JLabel("Replace:"),gbc);
		gbc.gridx=1;
		add(replaceField,gbc);
		
		//Row 2; Buttons
		JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton findNextBtn		= new JButton("Find Next");
		JButton replaceBtn		= new JButton("Replace");
		JButton replaceAllBtn	= new JButton("Replace All");
		JButton	closeBtn		= new JButton("Close");
		btnPanel.add(findNextBtn);
		btnPanel.add(replaceBtn);
		btnPanel.add(replaceAllBtn);
		btnPanel.add(closeBtn);
		
		gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth =2;
		add(btnPanel,gbc);
		
		pack();
		setLocationRelativeTo(getOwner());
		
		// Actions
		findNextBtn.addActionListener(e -> findNext());
		replaceBtn.addActionListener(e -> replace());
		replaceAllBtn.addActionListener(e -> replaceAll());
		closeBtn.addActionListener(e -> dispose());
		findField.addActionListener(e -> findNext());
		
	}
	
	private void findNext() {
		String target = findField.getText();
		if (target.isEmpty()) {
			// nothing to do
			return ;
		}
		// 1) if user typed a new search term, restart from beginning
		if (!target.equals(lastSearchTerm)) {
			lastMatchPos = 0;
			lastSearchTerm = target;
		}
		try {
			// 2) pull the full text from the Document
			javax.swing.text.Document doc = textPane.getDocument();
			String fullText =doc.getText(0,doc.getLength());
			
			// 3) find next occurrence
			int pos = fullText.indexOf(target, lastMatchPos);
			// wrap-around if we've hit the end
			if ( pos == -1 && lastMatchPos >0 ) {
				lastMatchPos= 0;
				pos = fullText.indexOf(target, 0);
			}
			if(pos != -1) {
				// select it in the pane
				textPane.requestFocusInWindow();
				textPane.select(pos,pos+target.length());
				//advance our cursor
				lastMatchPos = pos+target.length();
			} else {
				JOptionPane.showMessageDialog(this, 
						"no more occurrences of \"" + target + "\"",
						"Find", 
						JOptionPane.INFORMATION_MESSAGE);
			}
		} catch (javax.swing.text.BadLocationException ble) {
			ble.printStackTrace();
		}
	}
	

	
	
	private void replace() {
		String sel = textPane.getSelectedText();
		String t = findField.getText();
		String r = replaceField.getText();
		if (sel != null && sel.equals(t)) {
			try {
				int start =textPane.getSelectionStart();
				textPane.getDocument().remove(start,t.length());
				textPane.getDocument().insertString(start,r,null);
				lastMatchPos = start + r.length();
			} catch (BadLocationException ignored) {}
		}
		findNext();
	}
	
	private void replaceAll() {
		String text = textPane.getText();
		String target = findField.getText();
		String r = replaceField.getText();
		if (target.isEmpty()) return;
		
		textPane.setText(text.replace(target, r));
		lastMatchPos = 0;
	}
}

		
		

