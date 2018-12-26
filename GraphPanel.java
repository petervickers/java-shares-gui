import javax.swing.JPanel;
import javax.swing.ToolTipManager;
import java.awt.*;
import java.util.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.awt.geom.AffineTransform;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;


public class GraphPanel extends JPanel implements MouseMotionListener{
	private final static int PANEL_BORDER = 40;
	private final static int PANEL_BORDER_SOUTH = 150;
	private final static int X_LABEL_X_OFFSET = -3;
	private final static int X_LABEL_Y_OFFSET = 5;
	private final static int Y_LABEL_X_OFFSET = -30;
	private final static int Y_LABEL_Y_OFFSET = 5;

	int numPoints;
	private int[] stockPointsX;
	private int[] stockPointsY;
	
	private double[] stockOpenValues;
	private double[] stockHighValues;
	private double[] stockLowValues;
	public double[] stockCloseValues;
	private double[] stockVolumes;
	private String[] stockDates;
	
	double maxStockValue = 0;
	int maxGraphValue = 0;
	
	private int[] yLabels;
	
	private int horizontalGraphSize;
	private int verticalGraphSize;
	
	private double horizontalScale;
	private double verticalScale;
	
	private double ellipseXPos = -1;
	private double ellipseYPos = -1;

	private ArrayList<ArrayList<Integer>> xLinePos = new ArrayList<ArrayList<Integer>>();
	private ArrayList<String> xLabels = new ArrayList<String>();
	
	private ArrayList<ArrayList<Integer>> yLinePos = new ArrayList<ArrayList<Integer>>();
	private ArrayList<String> yLabelsList = new ArrayList<String>();

	private Font rotatedFont = this.getRotatedFont();
	
	private Ellipse2D ellipse = new Ellipse2D.Double(0, 0, 0, 0);
	
	Map<Integer, Integer> stockTooltipPointer = new HashMap<Integer, Integer>(); 
	Map<Integer, Integer> stockTooltipValue = new HashMap<Integer, Integer>(); 
	Map<Integer, Integer> stockTooltipMin = new HashMap<Integer, Integer>(); 
	Map<Integer, Integer> stockTooltipMax = new HashMap<Integer, Integer>(); 

	
	public GraphPanel(ArrayList<String[]> StockArray, Dimension windowSize) {
		this.numPoints = StockArray.size();
		this.stockPointsX = new int[numPoints];
		this.stockPointsY = new int[numPoints];
		this.stockOpenValues = new double[numPoints];
		this.stockHighValues = new double[numPoints];
		this.stockLowValues = new double[numPoints];
		this.stockCloseValues = new double[numPoints];
		this.stockVolumes = new double[numPoints];
		this.stockDates = new String[numPoints];
		
		this.addMouseMotionListener(this);
        this.setBackground(Color.WHITE);
		
		DateTimeFormatter inFormatter = DateTimeFormatter.ofPattern("MM/dd/yy");
		DateTimeFormatter outFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		
		Iterator<String[]> i = StockArray.iterator();
		int index = 0;
		while (i.hasNext()) {
			String[] csv = i.next();
			this.stockOpenValues[index] = Double.parseDouble(csv[1]);
			this.stockHighValues[index] = Double.parseDouble(csv[2]);
			this.stockLowValues[index] = Double.parseDouble(csv[3]);
			this.stockCloseValues[index] = Double.parseDouble(csv[4]);
			this.stockVolumes[index] = Double.parseDouble(csv[5]);

			if (Double.parseDouble(csv[4]) > this.maxStockValue) {
				this.maxStockValue = Double.parseDouble(csv[4]);
			}
			
			LocalDate aDate = LocalDate.parse(csv[0], inFormatter);
			this.stockDates[index++] = aDate.format(outFormatter);			
		}
		
		ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
		ToolTipManager.sharedInstance().setInitialDelay(0);
		
		this.setMaxY(this.maxStockValue);
		

		this.setGraphSize(windowSize);
	}	
	
	public void setGraphSize(Dimension windowSize) {
		this.horizontalGraphSize = (windowSize.width - 2 * PANEL_BORDER);
		this.horizontalScale =  this.horizontalGraphSize / (double)(this.numPoints - 1);
		this.verticalGraphSize = (windowSize.height - (PANEL_BORDER + PANEL_BORDER_SOUTH));
		this.verticalScale = (double) this.verticalGraphSize / (double) this.maxGraphValue;
		
		for(int i = 0; i < this.numPoints; i++) {
			
				this.stockPointsX[i] = graphXScale(i);
				this.stockPointsY[i] = graphYScale(this.stockCloseValues[i]);
		}

		this.setLableY();
		this.labelXPosCalc();
		this.labelYPosCalc();
		this.toolTipCalc();
		this.drawEllipse();

		this.repaint();
	}
	
	
	private int graphXScale (double xValue) {
		return ((int) Math.round(xValue * this.horizontalScale + this.PANEL_BORDER));
	}
	
	private int graphYScale (double yValue) {
		return ((int) Math.round(this.verticalGraphSize + this.PANEL_BORDER - yValue * this.verticalScale));
	}
	
	private void setMaxY(double inputValue) {
		int maxScale = 10;
		if (inputValue > 250) {
			maxScale = 100;
		}
		else if (inputValue < 25) {
			maxScale = 1;
		}
		this.maxGraphValue = (int) Math.round((Math.round(inputValue * 1.2)/maxScale)*maxScale);
	}
	
	private Font getRotatedFont() {
		// from https://stackoverflow.com/questions/10083913/how-to-rotate-text-with-graphics2d-in-java
		Font aFont = new Font(null, Font.PLAIN, 10);    
		AffineTransform affineTransform = new AffineTransform();
		affineTransform.rotate(Math.toRadians(90), 0, 0);
		return (aFont.deriveFont(affineTransform));
	}
	
	private void setLableY() {
		int maxLabel = this.maxGraphValue;
		double decimalScale = 1;
		if (maxLabel > 300) {
			decimalScale = 0.1;
		}
		else if (maxLabel < 25) {
			decimalScale = 10;
		}
		
		int[] lableY = new int[(int) ((maxLabel*decimalScale/10 + 1))];
		int counter = 0;
		for(int i = 0; i <= (int)maxLabel; i+= 10/decimalScale) {
			lableY[counter++] = i;
		}
		this.yLabels = lableY;
	}
	
	private void labelXPosCalc() {
		double labelJump = (double) (this.numPoints-1) / (double) 10;

		this.xLinePos.clear();

		ArrayList<Integer> inner = new ArrayList<Integer>();
		for (double i = 0; Math.round(i) <= (double) this.numPoints; i += labelJump) {
			inner.addAll(Arrays.asList((int) Math.round(i * this.horizontalScale + this.PANEL_BORDER), this.verticalGraphSize + this.PANEL_BORDER, 
									   (int) Math.round(i * this.horizontalScale + this.PANEL_BORDER), graphYScale(yLabels[yLabels.length-1])));
			this.xLinePos.add(new ArrayList<Integer>(inner));
			this.xLabels.add(this.stockDates[(int) Math.round(i)]);
			inner.clear();
		}
	}
	
	private void labelYPosCalc() {
		this.yLinePos.clear();

		ArrayList<Integer> inner = new ArrayList<Integer>();
		for (int yLabel : yLabels) {
			inner.addAll(Arrays.asList(this.PANEL_BORDER, graphYScale(yLabel), this.horizontalGraphSize + this.PANEL_BORDER, graphYScale(yLabel)));
			this.yLinePos.add(new ArrayList<Integer>(inner));
			this.yLabelsList.add(Integer.toString(yLabel));
			inner.clear();
		}

	}
	
	private void toolTipCalc() {
		for (int i = 0; i <= this.horizontalGraphSize; i++) { 
			int interpolatedPoint = (int) Math.round((double) i / this.horizontalScale);
			this.stockTooltipPointer.put(i + this.PANEL_BORDER, interpolatedPoint);
			this.stockTooltipValue.put(i + this.PANEL_BORDER, this.stockPointsY[interpolatedPoint]);
		}
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setBackground(Color.white);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		
		g2.setPaint(Color.gray);
		g2.setStroke(new BasicStroke(1));
		for (int i = 0; i < this.yLinePos.size(); i++) {
			g2.drawLine(this.yLinePos.get(i).get(0), this.yLinePos.get(i).get(1), this.yLinePos.get(i).get(2), this.yLinePos.get(i).get(3));
		}
		
		for (int i = 0; i < this.xLinePos.size(); i++) {
			g2.drawLine(this.xLinePos.get(i).get(0), this.xLinePos.get(i).get(1), this.xLinePos.get(i).get(2), this.xLinePos.get(i).get(3));
		}
		
		g2.setPaint(Color.black);
		for (int i = 0; i < this.yLinePos.size(); i++) {
			g2.drawString(this.yLabelsList.get(i), this.yLinePos.get(i).get(0) + this.Y_LABEL_X_OFFSET, this.yLinePos.get(i).get(1) + this.Y_LABEL_Y_OFFSET);
		}
		
		g2.setFont(this.rotatedFont);
		for (int i = 0; i < this.xLinePos.size(); i++) {
			g2.drawString(this.xLabels.get(i), this.xLinePos.get(i).get(0) + this.X_LABEL_X_OFFSET, this.xLinePos.get(i).get(1) + this.X_LABEL_Y_OFFSET);
		}
		
		g2.setPaint(Color.red);		
		g2.setStroke(new BasicStroke(2));
		for (int i = 0; i < this.stockPointsX.length-1; i++) {
			g2.drawLine(this.stockPointsX[i], this.stockPointsY[i], this.stockPointsX[i+1], this.stockPointsY[i+1]);
		}
		
		g2.fill(this.ellipse);
	}
	
	public void setEllipsePos(int MouseXPos, int MouseYPos) {
		this.ellipseXPos = (double) (MouseXPos - PANEL_BORDER) / this.horizontalGraphSize;
		this.ellipseYPos = (double) (MouseYPos - PANEL_BORDER) / this.verticalGraphSize;
	}
	
	public void drawEllipse() {
		this.ellipse.setFrame(this.ellipseXPos * this.horizontalGraphSize - 6 + PANEL_BORDER, this.ellipseYPos * this.verticalGraphSize - 6 + PANEL_BORDER, 12, 12);
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		int mouseX = e.getX();
		int mouseY = e.getY();
		if (mouseX >= this.PANEL_BORDER && mouseX <= this.horizontalGraphSize + PANEL_BORDER) {
			if (mouseY >= this.PANEL_BORDER && mouseY <= this.verticalGraphSize + this.PANEL_BORDER) {
				setToolTipText("<html>" + this.stockDates[this.stockTooltipPointer.get(mouseX)] + "<br>" +
				"Open: " + this.stockOpenValues[this.stockTooltipPointer.get(mouseX)] + "<br>" +
				"Close: " + this.stockCloseValues[this.stockTooltipPointer.get(mouseX)] + "<br>" +
				"High: " + this.stockHighValues[this.stockTooltipPointer.get(mouseX)] + "<br>" +
				"Low: " + this.stockLowValues[this.stockTooltipPointer.get(mouseX)] + "<br>" +
				"Volume: " + String.format("%.0f", this.stockVolumes[this.stockTooltipPointer.get(mouseX)]) + "<br>" + "</html>");
				this.setEllipsePos(mouseX, this.stockTooltipValue.get(mouseX));
				this.drawEllipse();
				this.repaint();
			}
		}
	}
}
