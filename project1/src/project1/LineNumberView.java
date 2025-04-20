package project1;


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.FontMetrics;
import java.beans.*;


// The serializable class does not declare a static final serialversionUID field of type long.
//  -> No need to worry about it for now.



public class LineNumberView extends JComponent 
	implements CaretListener, DocumentListener, PropertyChangeListener, AdjustmentListener{

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
		
		// listen for changes
		textPane.getDocument().addDocumentListener(this);
		textPane.addCaretListener(this);
		textPane.addPropertyChangeListener("font",this);
		
		// also need to repaint when scrolling
		JScrollPane sp = (JScrollPane) SwingUtilities.getAncestorOfClass(
				JScrollPane.class, textPane);
		if ( sp != null){
			sp.getVerticalScrollBar().addAdjustmentListener(this);
		}
		
		setFont(font);
		updateWidth();
	}
	
	
	private void updateWidth() {
		int lineCount = getLineCount();
		int digits = String.valueOf(lineCount).length();
		if ( digits != lastDigits) {
			lastDigits = digits;
			int width = MARGIN * 2 + digitWidth * digits;
			setPreferredSize(new Dimension(width, Integer.MAX_VALUE));
			revalidate();
		}
	}
	
	
	private int getLineCount() {
		Element root = textPane.getDocument().getDefaultRootElement();
		return root.getElementCount();
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Rectangle clip = g.getClipBounds();
		
		int startOffset = textPane.viewToModel(new Point(0,clip.y));
		int endOffset = textPane.viewToModel(new Point(0, clip.y+clip.height));
		
		
		Element root = textPane.getDocument().getDefaultRootElement();
		int startLine = root.getElementIndex(startOffset);
		int endLine = root.getElementIndex(endOffset);
		
		int baseY;
		try {
			Rectangle r = textPane.modelToView(startOffset);
			baseY = r.y;
		} catch (BadLocationException e){
			baseY =0;
		}
		for ( int line = startLine; line <= endLine; line++){
			String num = String.valueOf(line+1);
			int y = baseY + (line - startLine) * lineHeight + fontMetrics.getAscent();
			int x= getWidth() - MARGIN - fontMetrics.stringWidth(num);
			g.drawString(num, x,y);
					
		}
		
		
		}
	
	/*
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Rectangle clip = g.getClipBounds();
		int startOffset = textPane.viewToModel(new Point(0, clip.y));
		int endOffset = textPane.viewToModel(new Point(0, clip.y + clip.height));
		
		Element root = textPane.getDocument().getDefaultRootElement();
		int startLine = root.getElementIndex(startOffset);
		int endLine = root.getElementIndex(endOffset);
		
		int baseY = textPane.modelToView(startOffset).y;
		for ( int line = startLine; line <= endLine; line++){
			String num= String.valueOf(line+1);
			int y = baseY + (line - startLine) * lineHeight + fontMetrics.getAscent();
			int x = getWidth() - MARGIN - fontMetrics.stringWidth(num);
			g.drawString(num,x,y);
		}
	}*/
	
	//Listeners : any change should repaint gutter
	@Override public void caretUpdate(CaretEvent e) { repaint();}
	@Override public void insertUpdate(DocumentEvent e) {
		updateWidth(); repaint();}
	@Override public void removeUpdate(DocumentEvent e) { 
		updateWidth(); repaint(); }
	@Override public void changedUpdate(DocumentEvent e) {/*attrs change*/}
	@Override public void propertyChange(PropertyChangeEvent evt){
		if ("font".equals(evt.getPropertyName())) {
			setFont(textPane.getFont());
	
		}
	}
	@Override public void adjustmentValueChanged(AdjustmentEvent e){
		repaint();
	}
}














