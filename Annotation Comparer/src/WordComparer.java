import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WordComparer extends TfidfTestingMaximization{
	static double threshstart,precision,recall;
	public WordComparer(double precision, double recall) {
		super(precision, recall);

	}
	private static double findMatch(HashMap<String, Double> weights, String s) {
		Iterator<String> iter = weights.keySet().iterator();
		while(iter.hasNext()){
			String sentence = iter.next();
			if(sentence.toLowerCase().replace(" ", "").replace(".", "").contains(s.toLowerCase().replace(" ", "").replace(".", ""))){
				return weights.get(sentence);
			}
		}
		return -1;
	}
	public static void main(String[] args){
		XSSFWorkbook workbook = new XSSFWorkbook();
		workbook.createSheet();
		XSSFSheet sheet = workbook.getSheetAt(0);
		sheet.createRow(0);
		XSSFCell cell = sheet.getRow(0).createCell(0);
		cell.setCellValue("PMC Table");
		XSSFCell cell1 = sheet.getRow(0).createCell(1);
		cell1.setCellValue("Precision");
		XSSFCell cell2 = sheet.getRow(0).createCell(2);
		cell2.setCellValue("Recall");
		XSSFCell cell3 = sheet.getRow(0).createCell(3);
		cell3.setCellValue("TP");
		XSSFCell cell4 = sheet.getRow(0).createCell(4);
		cell4.setCellValue("FP");
		XSSFCell cell5 = sheet.getRow(0).createCell(5);
		cell5.setCellValue("TN");
		XSSFCell cell6 = sheet.getRow(0).createCell(6);
		cell6.setCellValue("FN");
		//upload directories
		File pdirectory = new File("formattedPapers");
		File tdirectory = new File("rasTables");
		File[] papers = pdirectory.listFiles();
		File[] tables = tdirectory.listFiles();
		
		for(int p =0;p<=papers.length;p++){
			sheet.createRow(p+1);
			XSSFRow row = sheet.getRow(p+1);
		
		WordComparer t=new WordComparer(0, 0);
		File wordDoc = new File("PMC3071831 table 1.htm");
		try {
			Document doc = Jsoup.parse(wordDoc, null);
			Elements spans = doc.getElementsByTag("span").select("*[style*='background:yellow']");
			ArrayList<String> sentences = new ArrayList<String>();
			for (Element span: spans){
				String[] sentenceArray = span.text().split("\\. ");
				for(String s:sentenceArray){
					sentences.add(s);
				}
			}
			
			HashMap<String,Double> weights = TfIdfAnalysis.annotatePaper("PMC3071831", "PMC3071831.html", "PMC3071831pone-0018424-t001.html");
			for(String s: sentences){
				double weight = findMatch(weights, s);
				//System.out.println(weight + " " + s);
			}
			// convert hashmap to arraylist
			ArrayList<String> keys = new ArrayList<String>();
			ArrayList<Double> values = new ArrayList<Double>();
			double maxVal =0;
			double tstart=0;
			double tend=1.0;
			double increment=0.001;
			double maxprecision=0;
			double maxrecall=0;
			//threshold ranges from 0 to 1 1 is100% .7 = 70% of max etc
			double[] maxpair={tstart, 0};
	        for(double i=tstart; i<=tend; i+=increment){
	        	
	            double threshold1=i;
	            /*Here's where the new precision and recall evaluations need to happen.
	            Set p to precision and r to recall, the ones are placeholder values.
	            */
	           
	            
	        
			ArrayList<Entry<String,Double>> entries = new ArrayList<Entry<String,Double>>();
			entries.addAll(weights.entrySet());
			
			
			
			
			Collections.sort(entries, new Comparator<Entry<String, Double>>() {
				public int compare(Entry<String, Double> a, Entry<String, Double> b) {
					if(a.getValue()>b.getValue()){
						return 1;
						
					}
					if(a.getValue()<b.getValue()){
						
						return -1;
					}
					else{
						return 0;
					}
				}
			});
          
			// print precision and recall for given threshold
			int trupos,macgu,sen,threshin;
			
			trupos=0;
			threshin=0;
			
			sen=values.size();
			//find true positives
			threshstart=threshold1*entries.get(entries.size()-1).getValue();
			//System.out.println(threshstart);
			for(int i1=0; i1< entries.size();i1++){
				if(entries.get(i1).getValue()>=threshstart){
					threshin=i1;
					break;
				}
				}
			HashMap<String, Boolean> caughtSentences = new HashMap<String, Boolean>();
			for (String s: sentences){
				caughtSentences.put(s,false);
			}
			for(int i1=threshin;i1<entries.size();i1++){
				String s = findMatch(entries.get(i1).getKey(), sentences);
				if(s != null){
					trupos++;
					//System.out.println("HELLO" + entries.get(i1).getKey());
					caughtSentences.put(s, true);
				}
				//System.out.println(trupos);
			}
			
			macgu=entries.size()-threshin;
			precision= (double)trupos/(double)macgu;
			recall= ((double)trupos)/(double)sentences.size();
			
			if(f1(precision, recall)>maxpair[1]){
                maxpair[0]=i;
                maxpair[1]=f1(precision, recall);
                maxprecision=precision;
                maxrecall=recall;
               System.out.println("Sentences missed by TFIDF:");
                for(Entry<String,Boolean> e: caughtSentences.entrySet()){
    				if(!e.getValue()){
    				System.out.println(e.getKey());
    			}
    			}
            }
	        }
	        //output into txt file
	       /* File outFile = new File("/annotatortfidfacomparison/test.txt");
			FileOutputStream fos;
			try {
				fos = new FileOutputStream(outFile);
				PrintWriter pw = new PrintWriter(fos);
				pw.write("hi");
				pw.close();
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		*/
			System.out.println("Precision:" + maxprecision);
			System.out.println("Recall:"+maxrecall);
			System.out.println("Maximum at: "+ Arrays.toString(maxpair));
			
				XSSFCell precisionCell = row.createCell(1);
				precisionCell.setCellValue(maxprecision);
				XSSFCell recallCell = row.createCell(2);
				recallCell.setCellValue(maxrecall);
			
			
			  /*for(int i =0;i<entries.size();i++){
					
					//System.out.println(entries.get(i).getKey());
				}*/
			//System.out.println("________________________________________________________");
			for(String s: sentences){
				//System.out.println(s);
				
			}
			
			//System.out.print(trupos);
			
		} catch (IOException e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		}
		
		File precisionFile = new File("precisionResults.xlsx");
		try {
			FileOutputStream fos= new FileOutputStream(precisionFile);
			workbook.write(fos);
			workbook.close();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	private static String findMatch(String s,ArrayList<String> p) {
		Iterator<String> iter = p.iterator();
		while(iter.hasNext()){
			String sentence = iter.next();
			if(s.toLowerCase().replace(" ", "").replace(".", "").contains(sentence.toLowerCase().replace(" ", "").replace(".", ""))){
				return sentence;
			}
		}
		return null;
	}
  

}
