import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Container;
import javax.swing.*;
import javax.swing.text.*;
import java.util.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.io.*;
import java.net.*;


public class MarketGUI extends JFrame implements ActionListener {
	private int DEFAULT_WIDTH = 700;
	private int DEFAULT_HEIGHT = 400;
	private LocalDate startDate;
	private LocalDate endDate;
	private JComboBox<String> stockCombo;
	private JComboBox<String> startDayCombo;
	private JComboBox<String> startMonthCombo;
	private JComboBox<String> startYearCombo;
	private JComboBox<String> endDayCombo;
	private JComboBox<String> endMonthCombo;
	private JComboBox<String> endYearCombo;
	private JLabel errorLabel = new JLabel();

	public MarketGUI() { 
		setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		Container contentPane = this.getContentPane(); 
		
		JPanel buttonPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		String[] stockStrings = {"FB", "AMZN", "AAPL", "NFLX", "GOOG", "PCAR"};
		String[] startDayList = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"};
		String[] startMonthList = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};		
		String[] startYearList = {"2000", "2001", "2002", "2003", "2004", "2005", "2006", "2007", "2008", "2009", "2010", "2011", "2012", "2013", "2014", "2015", "2016", "2017", "2018"};		
		String[] endDayList = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"};
		String[] endMonthList = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};		
		String[] endYearList = {"2000", "2001", "2002", "2003", "2004", "2005", "2006", "2007", "2008", "2009", "2010", "2011", "2012", "2013", "2014", "2015", "2016", "2017", "2018"};		

		this.stockCombo = new JComboBox<String>(stockStrings);
		this.startDayCombo = new JComboBox<String>(startDayList);
		this.startMonthCombo = new JComboBox<String>(startMonthList);
		this.startYearCombo = new JComboBox<String>(startYearList);
		this.endDayCombo = new JComboBox<String>(endDayList);
		this.endMonthCombo = new JComboBox<String>(endMonthList);
		this.endYearCombo = new JComboBox<String>(endYearList);
		JButton submitButton = new JButton("Load Graph");

		submitButton.addActionListener(this);

		c.ipadx = 4;
		c.ipady = 4;
		c.insets = new Insets(4, 2, 4, 2);
		c.gridx = 0;
		c.gridy = 0;
		buttonPanel.add(new JLabel("Stock"), c);
		c.gridx = 2;
		buttonPanel.add(stockCombo, c); 
		c.gridx = 0;
		c.gridy = 1;		
		buttonPanel.add(new JLabel("Start Date"), c);
		c.gridx = 1;
		buttonPanel.add(startDayCombo, c);  
		c.gridx = 2;
		buttonPanel.add(startMonthCombo, c); 
		c.gridx = 3;		
		buttonPanel.add(startYearCombo, c);  
		c.gridx = 0;
		c.gridy = 2;
		buttonPanel.add(new JLabel("End Date"), c);
		c.gridx = 1;
		buttonPanel.add(endDayCombo, c);
		c.gridx = 2;		
		buttonPanel.add(endMonthCombo, c);
		c.gridx = 3;		
		buttonPanel.add(endYearCombo, c); 
		c.gridx = 2;
		c.gridy = 3;		
		buttonPanel.add(submitButton, c);  

		
		contentPane.add(buttonPanel, BorderLayout.NORTH); 
				
		JPanel errorPanel = new JPanel();
		errorLabel.setFont(new Font(null, Font.PLAIN, 14));
		errorLabel.setText("Click 'Submit' to load the stock price graph");
		errorPanel.add(errorLabel);
		contentPane.add(errorPanel, BorderLayout.CENTER); 	
		
		//Quit Panel
		JPanel quitPanel = new JPanel();
		JButton quitButton = new JButton("Quit");
		quitButton.addActionListener(this); 
		quitPanel.add(quitButton);  
		contentPane.add(quitPanel, BorderLayout.SOUTH); 
	}			
	
	//Quitting functionality
	public void actionPerformed(ActionEvent e) {
		String source = e.getActionCommand(); 

		if (source == "Quit") {
			System.exit(0);
		}		
		else if (source == "Load Graph") {
			try{
				String startDay = (String)startDayCombo.getSelectedItem();
				String startMonth = ((String)startMonthCombo.getSelectedItem()).substring(0,3);
				String startYear = (String)startYearCombo.getSelectedItem();
				String endDay = (String)endDayCombo.getSelectedItem();
				String endMonth = ((String)endMonthCombo.getSelectedItem()).substring(0,3);
				String endYear = (String)endYearCombo.getSelectedItem();
				
				String startDateToFormat = startDay + "-" + startMonth + "-" + startYear;
				String endDateToFormat = endDay + "-" + endMonth + "-" + endYear;
				
				DateTimeFormatter inFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
				DateTimeFormatter outFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
				DateTimeFormatter toTextFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

				LocalDate startDate = LocalDate.parse(startDateToFormat, inFormatter);
				LocalDate endDate = LocalDate.parse(endDateToFormat, inFormatter);
					
				Thread t1 = new Thread(new MyClass((String)stockCombo.getSelectedItem(), startDate, endDate));
				t1.start();
			}
			catch(Exception ex) {
				System.out.println(ex);
			}
		}
	}
	
	public class MyClass implements Runnable {
		private LocalDate startDate;
		private LocalDate endDate;
		private String stockSymbol;
		private ArrayList<String[]> stockArray;

		public MyClass(String inStock, LocalDate inStartDate, LocalDate inEndDate) {
			this.stockSymbol = inStock;
			this.startDate = inStartDate;
			this.endDate = inEndDate;
		}
		
		@Override
		public void run() {
			try{
				if (!endDate.isAfter(startDate)) {
					//this.errorLabel.setText("Error: end date not after start date");
					throw new IllegalArgumentException("Error: end date not after start date");
				}
				Downloader myDownloader = new Downloader(this.stockSymbol, this.startDate, this.endDate);
				errorLabel.setText("Downloading data");
				this.stockArray = myDownloader.getShares();
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
					  if(stockArray.size() > 1) {
							JFrame f1 = new GraphFrame(stockArray, (String)stockCombo.getSelectedItem(), stockArray.get(0)[0], stockArray.get(stockArray.size() - 1)[0]);
							f1.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
							f1.setVisible(true); 
							errorLabel.setText("Launching graph");
							stockArray.clear();
						}
						else {
							throw new IllegalArgumentException("Error: no share price data for this time period");
						}
					}
				});
			} 
			catch(Exception e) {
				System.out.println(e);
				errorLabel.setText(e.toString());
			}
		}
	}
	
	public static void main (String[] args) {
		JFrame f = new MarketGUI();
		f.setMinimumSize(new Dimension(500, 300));
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		f.setVisible(true); 
	}
}