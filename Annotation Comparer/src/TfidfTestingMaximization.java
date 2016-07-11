/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.Arrays;
/**
 *
 * @author leorf_000
 */
public class TfidfTestingMaximization extends TfIdfAnalysis {
    public static double[] maximize(TfidfTestingMaximization t, double tstart, double tend, double increment){
        double[] maxpair={tstart, f1(t.precision, t.recall)};
        for(double i=tstart; i<=tend; i+=increment){
            t.threshold=i;
            /*Here's where the new precision and recall evaluations need to happen.
            Set p to precision and r to recall, the ones are placeholder values.
            */
           
            int p=1;
            int r=1;
            t.setPrecision(p);
            t.setRecall(r);
            if(f1(t.precision, t.recall)>maxpair[1]){
                maxpair[0]=i;
                maxpair[1]=f1(t.precision, t.recall);
            }
        }
        return maxpair;
    }
    public TfidfTestingMaximization(double precision, double recall){
        this.precision=precision;
        this.recall=recall;
    }
    public void setPrecision(double p){
        this.precision=p;
    }
    public void setRecall(double r){
        this.recall=r;
    }
    public double precision;
    public double recall;
    public double threshold;
    public static double harmonicmean(double a, double b){
        double hmean= 2*(a*b)/(a+b);
        return hmean;
    }
    //Calculate f1 for equal weighting
    public static double f1(double precision, double recall){
        return harmonicmean(precision, recall);
    }
    public static void main(String[] args) {
        TfidfTestingMaximization test= new TfidfTestingMaximization(1, .05);
        
        System.out.println(Arrays.toString(maximize(test, 0.1, 1.0, 0.3)));
    }
    
}
