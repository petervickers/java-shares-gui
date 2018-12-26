import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;
import javax.swing.*;
import javax.swing.text.*;
import java.util.*;

public class GraphFrame extends JFrame implements ComponentListener{
	private GraphPanel myPanel; 
	
	public GraphFrame(ArrayList<String[]> StockArray, String stockName, String startDate, String endDate) { 
	    setMinimumSize(new Dimension(450, 350));
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setSize(screenSize.width*2/3, screenSize.height*2/3);
		Container contentPane = this.getContentPane(); 
		
		JPanel namePanel = new JPanel(new GridBagLayout());
		namePanel.setBackground(Color.WHITE);
		JLabel nameLabel = new JLabel(stockName + " " + startDate + " - " + endDate);
		nameLabel.setFont(new Font(null, Font.PLAIN, 20));
		namePanel.add(nameLabel);  
		contentPane.add(namePanel, BorderLayout.PAGE_START); 	

		myPanel = new GraphPanel(StockArray, this.getSize());
		contentPane.add(myPanel, BorderLayout.CENTER); 		
		addComponentListener(this);
	}
	
	@Override
	public void componentHidden(ComponentEvent e) {}

	@Override
    public void componentMoved(ComponentEvent e) {}

	@Override
    public void componentResized(ComponentEvent e) {
		myPanel.setGraphSize(e.getComponent().getBounds().getSize());
    }
	
	@Override
    public void componentShown(ComponentEvent e) {}
}