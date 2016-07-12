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
	static double threshstart, precision, recall;
	static XSSFCell cell8;
	static XSSFCell cell70;
    
	public WordComparer(double precision, double recall) {
		super(precision, recall);

	}

	private static double findMatch(HashMap<String, Double> weights, String s) {
		Iterator<String> iter = weights.keySet().iterator();
		while (iter.hasNext()) {
			String sentence = iter.next();
			if (sentence.toLowerCase().replace(" ", "").replace(".", "")
					.contains(s.toLowerCase().replace(" ", "").replace(".", ""))) {
				return weights.get(sentence);
			}
		}
		return -1;
	}

	public static void main(String[] args) {
		int maxtrupos, maxfn, maxfp;
		maxtrupos = 0;
		maxfn = 0;
		maxfp = 0;
		
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
		File pdirectory = new File("formattedPapers");
		File tdirectory = new File("allRasTables");
		File gdirectory = new File("goldStandard");
		File[] papers = pdirectory.listFiles();
		File[] tables = tdirectory.listFiles();
		File[] goldstandard = gdirectory.listFiles();
		System.out.println(Arrays.toString(goldstandard));
		for (int p = 1; p < goldstandard.length; p++) {

			sheet.createRow(p + 1);
			XSSFRow row = sheet.getRow(p + 1);

			WordComparer t = new WordComparer(0, 0);
			File wordDoc = new File(goldstandard[p].toString());
			String pmcnum = goldstandard[p].toString();
			pmcnum = pmcnum.substring(16, 23);
			System.out.println(pmcnum);
			System.out.println(papers.length);
			System.out.println(tables.length);
			String[] papers2 = new String[papers.length];
			for (int i = 0; i < papers.length; i++) {
				papers2[i] = papers[i].getName();
			}

			System.out.println(Arrays.toString(papers2));
			try {

				Document doc = Jsoup.parse(wordDoc, null);
				Elements spans = doc.getElementsByTag("span").select("*[style*='background:yellow']");
				ArrayList<String> sentences = new ArrayList<String>();
				for (Element span : spans) {
					String[] sentenceArray = span.text().split("\\. ");
					for (String s : sentenceArray) {
						sentences.add(s);
					}
				}
				
				String wow = "PMC" + pmcnum;
				String wow2 = wow + ".html";
				String wow3 = wow2;
				System.out.println(wow);
				System.out.println(wow2);
				System.out.println(wow3);
				int rasTable = 0;
				for (int l = 0; l < goldstandard.length; l++) {
					String icecream = tables[l].getName();
					String candycane = goldstandard[p].getName();
					if (icecream.contains(wow)
							&& (icecream.charAt(icecream.length() - 6) == candycane.charAt(candycane.length() - 5)
									|| icecream.substring(icecream.length() - 7, icecream.length() - 5).equals(
											candycane.substring(candycane.length() - 6, candycane.length() - 4)))) {

						rasTable = l;
					}
				}
				System.out.println(rasTable);
				HashMap<String, Double> weights = TfIdfAnalysis.annotatePaper(wow, wow2, tables[rasTable].getName());
				for (String s : sentences) {
					double weight = findMatch(weights, s);
					// System.out.println(weight + " " + s);
				}
				// convert hashmap to arraylist
				ArrayList<String> keys = new ArrayList<String>();
				ArrayList<Double> values = new ArrayList<Double>();
				double maxVal = 0;
				double tstart = 0;
				double tend = 1.0;
				double increment = 0.1;
				double maxprecision = 0;
				double maxrecall = 0;
				int trupos, sen, threshin, fp, fn = 0;
				int noise = 0;
                int maxmacgu=0;
                int macgu=0;
				// threshold ranges from 0 to 1 1 is100% .7 = 70% of max etc
				double[] maxpair = { tstart, 0 };
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

					ArrayList<Entry<String, Double>> entries = new ArrayList<Entry<String, Double>>();
					entries.addAll(weights.entrySet());

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
					threshin = 0;

					sen = values.size();
					// find true positives
					threshstart = threshold1 * entries.get(entries.size() - 1).getValue();
					// System.out.println(threshstart);
					for (int i1 = 0; i1 < entries.size(); i1++) {
						if (entries.get(i1).getValue() >= threshstart) {
							threshin = i1;
							break;
						}
					}
					HashMap<String, Boolean> caughtSentences = new HashMap<String, Boolean>();
					for (String s : sentences) {
						caughtSentences.put(s, false);
					}
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
					for (Entry<String, Boolean> e : caughtSentences.entrySet()) {
						if (!e.getValue()) {
							fn++;
						}
					}
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
					precision = (double) trupos / (double) (trupos + fp);
					recall = ((double) trupos) / (double) (trupos + fn);
                    macgu=entries.size()-threshin;
					if (f1(precision, recall) > maxpair[1]) {
						maxpair[0] = i;
						maxpair[1] = f1(precision, recall);
						maxprecision = precision;
						maxrecall = recall;
						maxtrupos = trupos;
						maxfn = fn;
						maxfp = fp;
						maxmacgu =macgu;
						/*
						 * System.out.println("Sentences missed by TFIDF:"); for
						 * (Entry<String, Boolean> e :
						 * caughtSentences.entrySet()) { if (!e.getValue()) {
						 * 
						 * System.out.println(e.getKey()); } }
						 */
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

				System.out.println(maxfn);
				System.out.println(maxtrupos);
				System.out.println("Precision:" + maxprecision);
				System.out.println("Recall:" + maxrecall);
				System.out.println("Maximum at: " + Arrays.toString(maxpair));
				XSSFCell PMCCell = row.createCell(0);
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
			if (s.toLowerCase().replace(" ", "").replace(".", "")
					.contains(sentence.toLowerCase().replace(" ", "").replace(".", ""))) {
				return sentence;
			}
		}
		return null;
	}

}
