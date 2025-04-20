package project1;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.awt.*;

import java.awt.FontMetrics;
import java.beans.*;

public class LineNumberView extends JComponent 
	implements CareListener, DocumnetListener, PropertyChangeListener, AdjustmentListener{

	private static final int MARGIN = 5;
	private final JTextPane textPane;
	private final FontMetrics fontMetrics;
	private final int lineHeight;
	private final int digitWidth;
	private int lastDigits;
	
	public LineNumberView(JTextPane textPane) {
		this.textPane=textPane;
		Font font = textPane.getFont();
		this.fontMetrics = getFontMetrics(font);
		this.lineHeight = fontMetrics.getHeight();
		this.digitWidth = fontMetrics.charWidth('0');
		
	}
}
