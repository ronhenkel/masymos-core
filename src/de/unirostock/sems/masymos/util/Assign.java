package de.unirostock.sems.masymos.util;

/**
 *
 * Copyright 2016 Ron Henkel (GPL v3)
 * @author ronhenkel
 */
public class Assign {
	String name = null;
	String n1 = null;
	String n2 = null;
	double sim = Double.NaN;
	
	public String getN1() {
		return n1;
	}

	public String getN2() {
		return n2;
	}

	public double getSim() {
		return sim;
	}
	
	
	public Assign(String concept1, String concept2, double similarity){
		this.n1 = concept1;
		this.n2 = concept2;
		this.sim = similarity;
	}
	
	public void print(){
		System.out.print(n1);
		System.out.print(" x ");
		System.out.print(n2);
		System.out.print(" -> ");
		System.out.print(sim);
	}
	
	public void printCSV(){
		System.out.print(n1);
		System.out.print(";");
		System.out.print(n2);
		System.out.print(";");
		System.out.print(sim);
	}

}
