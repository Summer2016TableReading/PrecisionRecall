import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Array;
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

public class WordComparer extends TfidfTestingMaximization {
	// threshstart is the numerical value where you start considering tfidf
	// sentences positive, the threshold
	static double threshstart, precision, recall;
	static XSSFCell cell8;
	static XSSFCell cell70;

	public WordComparer(double precision, double recall) {
		super(precision, recall);

	}

	// sees whether a string is contained within a set of strings, if so returns
	// the string
	private static double findMatch(HashMap<String, Double> weights, String s) {
		Iterator<String> iter = weights.keySet().iterator();
		while (iter.hasNext()) {
			String sentence = iter.next();
			if (sentence.toLowerCase().replace(" ", "").replace(".", "")
					.equals(s.toLowerCase().replace(" ", "").replace(".", ""))) {
				return weights.get(sentence);
			}
		}
		return -1;
	}

	public static void main(String[] args) {
		// variables below store the tp, fn,fp for the best f1 score
		int maxtrupos, maxfn, maxfp;
		maxtrupos = 0;
		maxfn = 0;
		maxfp = 0;
		// setting up the excel sheet headers
		
		XSSFWorkbook workbook = new XSSFWorkbook();
		workbook.createSheet();
		workbook.createSheet();
		XSSFSheet sheet2 = workbook.getSheetAt(1);
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
		cell5.setCellValue("FN");
		XSSFCell cell6 = sheet.getRow(0).createCell(6);
		cell6.setCellValue("Threshold");
		XSSFCell cell7 = sheet.getRow(0).createCell(7);
		cell7.setCellValue("F1 Score");
		cell8 = sheet.getRow(0).createCell(8);

		cell8.setCellValue("Formatting info");
		XSSFCell cell9 = sheet.getRow(0).createCell(9);
		cell9.setCellValue("TP + FP");
		// upload directories
		File pdirectory = new File("formattedPapers");//formattedPapers
		File tdirectory = new File("allRasTables");
		File gdirectory = new File("goldStandard"); //goldStandard
		File[] papers = pdirectory.listFiles();
		File[] tables = tdirectory.listFiles();
		File[] goldstandard = gdirectory.listFiles();
		System.out.println(Arrays.toString(goldstandard));
		// an array of strings to hold the name of the papers without annotation
		String[] papers2 = new String[papers.length];
		for (int i = 0; i < papers.length; i++) {
			papers2[i] = papers[i].getName();
		}

		// this for-loop loops through all of the goldstandard annotations and
		// compares them to tfidf
        int specifics =0;
        int specifice=0;
        if(specifics==0){
        	specifice =goldstandard.length;
           specifics =1;
        }
        else{
        	specifice=specifics+1;
        }
		for (int p = specifics; p < specifice; p++) {
           
			sheet.createRow(p + 1);
			XSSFRow row = sheet.getRow(p + 1);
			XSSFCell PMCCell = row.createCell(0);
			WordComparer t = new WordComparer(0, 0);
			// is the name of the p'th goldstandard file
			File wordDoc = new File(goldstandard[p].toString());
			String pmcnum = goldstandard[p].toString();
			// gets the PMC number of the file
			pmcnum = pmcnum.substring(16, 23);
			try {
				// searches for highlighted sentences in goldstandard and adds
				// them to the arraylist sentences
				Document doc = Jsoup.parse(wordDoc, null);
				Elements spans = doc.getElementsByTag("span").select("*[style*='background:yellow']");
				ArrayList<String> sentences = new ArrayList<String>();
				for (Element span : spans) {
					String[] sentenceArray = span.text().split("\\. ");
					for (String s : sentenceArray) {
						sentences.add(s);
					}
				}
				
				
				XSSFRow row2 = sheet2.createRow(0);
				XSSFCell cellmissed = row2.createCell(0);
				
				cellmissed.setCellValue("Sentences missed by TFIDF");
				String paperid = "PMC" + pmcnum;
				String papername = paperid + ".html";

				int rasTable = 0;
				boolean tablefound = false;
				for (int l = 0; l < goldstandard.length; l++) {
					// table name is the string of the table file name at l and
					// gsname is the string of the goldstandard file name at p
					String tablename = tables[l].getName();
					String gsname = goldstandard[p].getName();
					// finds the corresponding table for a goldstandard file
					// checks with paper id and with the 6 and 7th from last
					// characters, these characters give the table number and
					// relevant a/b endings
					if (tablename.contains(paperid)
							&& (tablename.charAt(tablename.length() - 6) == gsname.charAt(gsname.length() - 5)
									|| tablename.substring(tablename.length() - 7, tablename.length() - 5)
											.equals(gsname.substring(gsname.length() - 6, gsname.length() - 4)))) {

						rasTable = l;
						tablefound = true;
					}
				}
				if (!tablefound) {
					PMCCell.setCellValue("Error: Table not found");
					continue;
				}
				// creates hashmap of sentences with their tfidf score
				HashMap<String, Double> weights = TfIdfAnalysis.annotatePaper(paperid, papername,
						tables[rasTable].getName());
				// prints sentences with their scores
				for (String s : sentences) {
					double weight = findMatch(weights, s);
					// System.out.println(weight + " " + s);
				}
				// convert hashmap to two arraylists- one with sentences, one
				// with tfidf scores for the sentences
				ArrayList<String> keys = new ArrayList<String>();
				ArrayList<Double> values = new ArrayList<Double>();
				// tstart is the value which the maximization program sets as
				// the initial threshold, it should always be 0
				// tend is where the maximization program stops the
				// incrementing, should always be <1
				// increment is the amount by which the program increases the
				// increment for each run by the f1 maximization program
				// max precision and max recall are the precision and recall at
				// the best f1 score
				// trupos, fp, and fn are the values for true positive, false
				// pos, and false negative at the current threshold
				// noise is the number of sentences which aren't caught when
				// threshold is equal to 0
				// max macgu is the macgu at the best f1 score
				// macgu is the tp +fp
				double tstart = 0;
				double tend = 1.0;
				double increment = 0.1;
				double maxprecision = 0;
				double maxrecall = 0;
				int trupos, threshin, fp, fn = 0;
				int noise = 0;
				int maxmacgu = 0;
				int macgu = 0;
				int maxcellnum=1;
				// threshold ranges from 0 to 1 1 is100% .7 = 70% of max etc
				double[] maxpair = { tstart, 0 };
				//for loop goes through every increment of threshold for a specific tfidf annotated table
				for (double i = tstart; i <= tend; i += increment) {
					fp = 0;
					fn = 0;
					trupos = 0;

					double threshold1 = i;
					/*
					 * Here's where the new precision and recall evaluations
					 * need to happen. Set p to precision and r to recall, the
					 * ones are placeholder values.
					 */
                //array list of the sentences and their scores
					ArrayList<Entry<String, Double>> entries = new ArrayList<Entry<String, Double>>();
					entries.addAll(weights.entrySet());
                 // sort the arraylist
					Collections.sort(entries, new Comparator<Entry<String, Double>>() {
						public int compare(Entry<String, Double> a, Entry<String, Double> b) {
							if (a.getValue() > b.getValue()) {
								return 1;

							}
							if (a.getValue() < b.getValue()) {

								return -1;
							} else {
								return 0;
							}
						}
					});

					// print precision and recall for given threshold

					trupos = 0;
					//the index in the arraylist where the threshold starts
					threshin = 0;

					// find true positives
					// the double of the threshold, threshold1 is as the percentage of the maximum score
					threshstart = threshold1 * entries.get(entries.size() - 1).getValue();
					// System.out.println(threshstart);
					//find where the threshold begins
					for (int i1 = 0; i1 < entries.size(); i1++) {
						if (entries.get(i1).getValue() >= threshstart) {
							threshin = i1;
							break;
						}
					}
					//finding missed sentences using a hashmap
					HashMap<String, Boolean> caughtSentences = new HashMap<String, Boolean>();
					for (String s : sentences) {
						caughtSentences.put(s, false);
					}
					//calculates number of trupositives anf falsepositives for a certain threshold
					for (int i1 = threshin; i1 < entries.size(); i1++) {
						String s = findMatch(entries.get(i1).getKey(), sentences);
						if (s != null) {
							trupos++;

							// System.out.println("HELLO" +
							// entries.get(i1).getKey());
							caughtSentences.put(s, true);
						} else {
							fp++;

						}

					}
					// System.out.println(trupos);
					//calculates false negatives
					for (Entry<String, Boolean> e : caughtSentences.entrySet()) {
						if (!e.getValue()) {
							fn++;
						}
					}
					//finds broken encoding sentences and removes them from false negative calculation
					if (threshin == 0) {
						noise = fn;

					}
					fn -= noise;

					/*
					 * macgu = entries.size() - threshin;
					 * System.out.println("Macgu: " + macgu);
					 * System.out.println("True Positives: " + trupos);
					 * System.out.println("False Positives: " + fp);
					 * System.out.println(sentences.size() - trupos - noise);
					 */
					//calculates precision and recall using factors
					precision = (double) trupos / (double) (trupos + fp);
					recall = ((double) trupos) / (double) (trupos + fn);
					macgu = entries.size() - threshin;
					//finds precision/recall/threshold/f1/tp/fp/fn for the maximum f1 score
					if (f1(precision, recall) > maxpair[1]) {
						maxpair[0] = i;
						maxpair[1] = f1(precision, recall);
						maxprecision = precision;
						maxrecall = recall;
						maxtrupos = trupos;
						maxfn = fn;
						maxfp = fp;
						maxmacgu = macgu;
						int cellnum=1;
					   // creates a second sheet which shows the missed sentences for each pmc
					   XSSFRow rowayy= sheet2.createRow(p+1);
					   XSSFCell pmcnom = rowayy.createCell(0);
					   pmcnom.setCellValue(paperid);
						for(Entry<String, Boolean> e :caughtSentences.entrySet()) { 
							if (!e.getValue()) {
						  
						  XSSFCell cellhi = rowayy.createCell(cellnum);
						  cellnum++;
						 cellhi.setCellValue( e.getKey());
						  } }
						 maxcellnum=cellnum;
					}

				}
				// output into txt file
				/*
				 * File outFile = new
				 * File("/annotatortfidfacomparison/test.txt"); FileOutputStream
				 * fos; try { fos = new FileOutputStream(outFile); PrintWriter
				 * pw = new PrintWriter(fos); pw.write("hi"); pw.close();
				 * fos.close(); } catch (IOException e) { e.printStackTrace(); }
				 */


				System.out.println("Max cell number: " + maxcellnum);
				System.out.println("Precision:" + maxprecision);
				System.out.println("Recall:" + maxrecall);
				System.out.println("Maximum at: " + Arrays.toString(maxpair));

				PMCCell.setCellValue(tables[rasTable].getName());
				XSSFCell precisionCell = row.createCell(1);
				precisionCell.setCellValue(maxprecision);
				XSSFCell recallCell = row.createCell(2);
				recallCell.setCellValue(maxrecall);
				XSSFCell cell90 = row.createCell(3);
				cell90.setCellValue(maxtrupos);
				XSSFCell cell30 = row.createCell(4);
				cell30.setCellValue(maxfp);
				XSSFCell cell40 = row.createCell(5);
				cell40.setCellValue(maxfn);
				XSSFCell cell50 = row.createCell(6);
				cell50.setCellValue(maxpair[0]);
				XSSFCell cell60 = row.createCell(7);
				cell60.setCellValue(maxpair[1]);
				XSSFCell cell600 = row.createCell(9);
				cell600.setCellValue(maxmacgu);
				if (unform == true) {
					cell70 = row.createCell(8);
					cell70.setCellValue("unusual formatting");
				}

				/*
				 * for(int i =0;i<entries.size();i++){
				 * 
				 * //System.out.println(entries.get(i).getKey()); }
				 */
				// System.out.println("________________________________________________________");
				for (String s : sentences) {
					// System.out.println(s);

				}
				
				// System.out.print(trupos);

			} catch (IOException e) {

				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		File precisionFile = new File("precisionResults2.xlsx");

		try {
			FileOutputStream fos = new FileOutputStream(precisionFile);
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

	private static String findMatch(String s, ArrayList<String> p) {
		Iterator<String> iter = p.iterator();
		while (iter.hasNext()) {
			String sentence = iter.next();
			if (s.toLowerCase().replaceAll("\\W", "").replace(".", "")
					.equals(sentence.toLowerCase().replaceAll("\\W", "").replace(".", ""))) {
				return sentence;
			}
		}
		return null;
	}

}
