package project1;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.HashMap;
import java.util.Map;
import java.util.*;

/*
 * This renders line numbers and simple code folding
 * based on matching '{' and '}'
 */



public class Folding extends JComponent
	implements MouseListener, CaretListener, DocumentListener, PropertyChangeListener,
	AdjustmentListener {
	
	private static final String PLACEHOLDER = "...";
	private static final int MARGIN = 4;
	
	private final JTextPane textPane;
	private final FontMetrics fm;
	private final int lineHeight, ascent;
	private final Map<Integer,String> foldedBlocks = new HashMap<>();
	
	public Folding(JTextPane tp){
		this.textPane =tp;
		Font f = tp.getFont();
		this.fm = getFontMetrics(f);
		this.lineHeight = fm.getHeight();
		this.ascent = fm.getAscent();
		
		tp.addMouseListener(this);
		tp.addCaretListener(this);
		tp.addPropertyChangeListener("font",this);
		
		
		JScrollPane sp = (JScrollPane)SwingUtilities.getAncestorOfClass(
				JScrollPane.class, tp);
		if (sp!=null) sp.getVerticalScrollBar()
					.addAdjustmentListener(this);
		
		this.addMouseListener(this);
		updatePreferredWidth();
		setOpaque(false);
		
		setFont(f);
		setPreferredSize(new Dimension(fm.charWidth('0')*3 + 8, Integer.MAX_VALUE));
	}
	private void updatePreferredWidth() {
		int lines = textPane.getDocument().getDefaultRootElement()
				.getElementCount();
		int digits = String.valueOf(lines).length();
		int width = MARGIN*2 + fm.charWidth('0') * digits;
		setPreferredSize(new Dimension(width, Integer.MAX_VALUE));
		revalidate();
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Rectangle clip = g.getClipBounds();
		try {
			int startOff = textPane.viewToModel(new Point(0,clip.y));
			int endOff = textPane.viewToModel(new Point(0, clip.y+clip.height));
			
			Element root = textPane.getDocument().getDefaultRootElement();
			int startLine = root.getElementIndex(startOff);
			int endLine = root.getElementIndex(endOff);
			
			int y0 = textPane.modelToView(startOff).y;
			
			for (int line=startLine; line<=endLine; line++) {
				Element el = root.getElement(line);
				int offset = el.getStartOffset();
				int y = y0 + (line-startLine)*lineHeight + ascent;
				
				String num = String.valueOf(line+1);
				int xNum = getWidth()-MARGIN - fm.stringWidth(num);
				g.drawString(num,xNum,y);
				
				boolean isFolded = foldedBlocks.containsKey(offset);
				String firstChar = textPane.getDocument()
						.getText(offset,1);
				
				
				// if this line starts with "{" draw a fold icon
				String txt = textPane.getDocument()
						.getText(offset, 1);
				if (firstChar.equals("{") || isFolded) {
					int size = ascent;
					int xIcon = MARGIN;
					g.drawRect(xIcon, y -ascent, size,size);
					if (isFolded) {
						g.fillRect(xIcon+1, y-ascent+1, size-1, size-1);
					}
				}

			}
		} catch (BadLocationException ex) {
			//ignore
		}
	}
	
	@Override public void mouseClicked(MouseEvent e) {
		int line = e.getY() / lineHeight;
		
		Element root = textPane.getDocument().getDefaultRootElement();
		
		if (line < 0 || line >= root.getElementCount()) return;
		
		int startOff = root.getElement(line).getStartOffset();
		
		if (foldedBlocks.containsKey(startOff)) {
			unfoldBlock(startOff);
		} else {
			try {
				// only fold if ther's an opening brace here
				String one = textPane.getDocument().getText(startOff,1);
				if (one.equals("{")) foldBlock(startOff);
			} catch (BadLocationException ignored) {}
		}
		repaint();
	}
	
	private void foldBlock(int start) throws BadLocationException {
		Document doc = textPane.getDocument();
		int end = findMatchingBrace(start);
		if (end <= start) return;
		
		String block = doc.getText(start,end-start+1);
		foldedBlocks.put(start,block);
		//replace the entire block with the placeholder
		doc.remove(start,block.length());
		doc.insertString(start,PLACEHOLDER, null);
	}
	
	private void unfoldBlock(int start) {
		try {
			Document doc = textPane.getDocument();
			String block = foldedBlocks.remove(start);
			if (block==null) return;
			
			// remove placeholder and re-insert original block
			doc.remove(start, PLACEHOLDER.length());
			doc.insertString(start, block, null);
		} catch (BadLocationException ex) {
			ex.printStackTrace();
		}
	}
	
	
	private int findMatchingBrace(int start) throws BadLocationException {
		Document doc = textPane.getDocument();
		String text =doc.getText(0,doc.getLength());
		int depth = 0;
		for (int i = start; i < text.length(); i++){
			char c =text.charAt(i);
			if (c == '{') depth++;
			else if (c == '}') {
				depth--;
				if (depth ==0) return i;
			}
		}
		return -1; //no match found
	}
    @Override public void insertUpdate(DocumentEvent e) { updatePreferredWidth(); repaint(); }
    @Override public void removeUpdate(DocumentEvent e) { updatePreferredWidth(); repaint(); }
    @Override public void changedUpdate(DocumentEvent e) { repaint(); }
    
	@Override
	public void caretUpdate(CaretEvent e) {
		int pos = e.getDot();
		//check if caret is on a placeholder
		for (Integer start : new ArrayList<>(foldedBlocks.keySet())) {
			if (pos >= start && pos < start + PLACEHOLDER.length()){
				unfoldBlock(start);
				repaint();
				break;
			}
		}
	}
	
	@Override public void propertyChange(PropertyChangeEvent evt){
		if ("font".equals(evt.getPropertyName())) {
			Font f = textPane.getFont();
			setFont(f);
			fm.getFont();
			updatePreferredWidth();
			repaint();
		}
	}
	
	@Override public void adjustmentValueChanged(AdjustmentEvent e){
		repaint();
	}
	
	
	
    public void mousePressed(MouseEvent e)   {}
    public void mouseReleased(MouseEvent e)  {}
    public void mouseEntered(MouseEvent e)   {}
    public void mouseExited(MouseEvent e)    {}		
	
}


