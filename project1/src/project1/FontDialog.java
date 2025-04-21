package project1;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.GraphicsEnvironment;



public class FontDialog extends JDialog {
	private final JTextPane textPane;
	private final JList<String> fontList;
	private final JList<Integer> sizeList;
	private final Folding folding;
	
	public FontDialog(JFrame owner, JTextPane textPane, Folding folding){
		super(owner, "Choose Font", true);
		this.textPane = textPane;
		this.folding = folding;
		
		// Font families
		String[] fonts = 
				GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		fontList = new JList<>(fonts);
		fontList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		fontList.setSelectedValue(textPane.getFont().getFamily(), true);
		
		// Common sizes
		Integer[] sizes = {8,9,10,11,12,14,16,18,20,24,28,31,48,72};
		sizeList = new JList<>(sizes);
		sizeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		sizeList.setSelectedValue(textPane.getFont().getSize(), true);
		
		initComponents();
	}
	private void initComponents() {
		setLayout(new BorderLayout(10,10));
		JPanel lists = new JPanel(new GridLayout(1,2,5,5));
		lists.add(new JScrollPane(fontList));
		lists.add(new JScrollPane(sizeList));
		add(lists, BorderLayout.CENTER);
		
		JButton ok		= new JButton("OK");
		JButton cancel	= new JButton("Cancel");
		JPanel btns		= new JPanel(new FlowLayout(FlowLayout.RIGHT));
		btns.add(ok);
		btns.add(cancel);
		add(btns, BorderLayout.SOUTH);
		
		ok.addActionListener(e -> applyFont());
		cancel.addActionListener(e -> dispose());
		
		pack();
		
		setLocationRelativeTo(getOwner());
	}
	
	private void applyFont() {
		String family = fontList.getSelectedValue();
		Integer size = sizeList.getSelectedValue();
		if (family != null && size != null) {
			Font f= new Font(family, Font.PLAIN, size);
			textPane.setFont(f);
			folding.setFont(f);
		}
		dispose();
	}

}
