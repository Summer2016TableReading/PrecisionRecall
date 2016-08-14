import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Kappa {
public static void main(String[] args) {
File File1 = new File("Copy of PMC2474852 table 2 Valentine.htm");
File File2 = new File("PMC2474852 Leor.html");
double k = kappa(File1, File2);
System.out.println("Kappa for these two files is: " + k);
}

private static String findMatch(ArrayList<String> p, String s) {
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

private static double kappa(File doc1, File doc2) {
double kappa = 0.0;
double tp = 0.0;
double tn = 0.0;
double fp = 0.0;
double fn = 0.0;
ArrayList<String> UH1 = getUnhighlighted(doc1);
ArrayList<String> H1 = getHighlighted(doc1);
ArrayList<String> UH2 = getUnhighlighted(doc2);
ArrayList<String> H2 = getHighlighted(doc2);
Iterator<String> iterUh = UH1.iterator();
while (iterUh.hasNext()) {
String sentence = iterUh.next();
if (findMatch(UH2, sentence) != null) {
tn += 1.0;
} else {
fn += 1.0;
}
}
Iterator<String> iterH = H1.iterator();
while (iterH.hasNext()) {
String sentence = iterH.next();
if (findMatch(H2, sentence) != null) {
tp += 1.0;
} else {
fp += 1.0;
}
}
double total1 = (double) UH1.size() + H1.size();
double total2 = (double) UH2.size() + H2.size();
if (total1 != total2) {
System.out.println(total1 - total2);
}
System.out.println(tp + " " + fp + " " + fn + " " + tn);
double IAA= (tp+tn)/(tp+fp+fn+tn);
System.out.println("IAA: "+ IAA);
double H1s = (double) H1.size();
double UH1s = (double) UH1.size();
double H2s = (double) H2.size();
double UH2s = (double) UH2.size();
double ObsAgreement = (tp + tn) / (total1);
double probYes = H1s / total1 * H2s / total2;
double probNo = UH1s / total1 * UH2s / total2;
double expectedprob = probYes + probNo;
kappa = (ObsAgreement - expectedprob) / (1 - expectedprob);
return kappa;
}

private static ArrayList<String> getHighlighted(File wordDoc) {
try {
/*
* searches for highlighted sentences in goldstandard and adds them
* to the arraylist of highlighted sentences
*/
Document doc = Jsoup.parse(wordDoc, null);
Elements span1=doc.getElementsByTag("p");
Elements spans = span1.select("*[style*='background:yellow']");
ArrayList<String> sentences = new ArrayList<String>();
for (Element span : spans) {
String[] sentenceArray = span.text().split("\\. ");
for (String s : sentenceArray) {
sentences.add(s);

}
}
//Iterator <String> iter=sentences.iterator();
//System.out.println("Highlighted for " + wordDoc.getName()+ ":");
/*while(iter.hasNext()){
String s=iter.next();
System.out.println(s);
}*/
return sentences;
} catch (IOException e) {
e.printStackTrace();
ArrayList<String> sentences = new ArrayList<String>();
return sentences;
}
}

private static ArrayList<String> getUnhighlighted(File wordDoc) {
try {
/*
* searches for highlighted sentences in goldstandard and adds them
* to the arraylist of highlighted sentences
*/
Document doc = Jsoup.parse(wordDoc, null);

ArrayList<String> sentences = new ArrayList<String>();
Elements span1=doc.getElementsByTag("p");
String[] sentenceArray = span1.text().split("\\. ");
for (String s : sentenceArray) {
sentences.add(s);

}
sentences.removeAll(getHighlighted(wordDoc));
//Iterator <String> iter=sentences.iterator();
//System.out.println("Unhighlighted for " + wordDoc.getName()+ ":");
/*while(iter.hasNext()){
String s=iter.next();
System.out.println(s);
}*/
return sentences;
} catch (IOException e) {
e.printStackTrace();
ArrayList<String> sentences = new ArrayList<String>();
return sentences;
}
}
}
