import java.io.*;
import java.net.*;
import java.util.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;

public class Downloader{
	private String Stock;
	private LocalDate startDate;
	private LocalDate endDate;
	private String URL1 = "https://quotes.wsj.com/";
	private String URL2 = "/historical-prices/download?MOD_VIEW=page&num_rows=300&startDate=";
	private String URL3 = "&endDate=";

	public Downloader(String inStock, LocalDate startDate, LocalDate endDate) {
		this.Stock = inStock;
		this.startDate = startDate;
		this.endDate = endDate;
	}
	
	
	public ArrayList<String[]> getShares() throws IOException {
		DateTimeFormatter inFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
		DateTimeFormatter outFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

		ArrayList<String> dateArray = new ArrayList<String>();
		dateArray.add(this.startDate.format(outFormatter));
		
		LocalDate movingDate = startDate;

		while(this.endDate.isAfter(movingDate.plusYears(1))) {
			movingDate = movingDate.plusYears(1);
			dateArray.add(movingDate.format(outFormatter));
			movingDate = movingDate.plusDays(1);
			dateArray.add(movingDate.format(outFormatter));
		}
		dateArray.add(endDate.format(outFormatter));
		
		ArrayList<String[]> stockArray = new ArrayList<String[]>();			
		
		Iterator<String> i = dateArray.iterator();
		
		try{
			while (i.hasNext()) {
				String firstDate = i.next();
				String secondDate = i.next();
				String URLString = URL1 + this.Stock + URL2 + firstDate + URL3 + secondDate;

				URL oracle = new URL(URLString);
				BufferedReader in = new BufferedReader(
				new InputStreamReader(oracle.openStream()));
				
				in.readLine(); //skip header
				
				String inputLine;
				ArrayList<String[]> tempArray = new ArrayList<String[]>();

				while ((inputLine = in.readLine()) != null){
					String[] csvArray = inputLine.split(",");
					tempArray.add(csvArray);
				}
				in.close();
				firstDate = secondDate;

				Collections.reverse(tempArray);
				stockArray.addAll(tempArray);
			}
		}
		catch(Exception e) {
				throw e;
		}
		return (stockArray);
	}
}
	

