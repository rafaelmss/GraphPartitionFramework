/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.unifei.mestrado.base_spectral.linears;

import java.util.Arrays;

/**
 *
 * @author rafael
 */
public class Eigenvector {
    
    private double[] values;
    private double middle;

    public Eigenvector() {
    }

    public Eigenvector(double[] values) {
        this.values = values;        
        double[] elements = new double[values.length];
        
        for (int i=0; i<values.length; i++){
            elements[i]=values[i];
        }
        
        Arrays.sort(elements);
        this.middle = elements[Math.abs(elements.length/2)];
        
    }

    public double[] getValues() {
        return values;
    }

    public void setValues(double[] values) {
        this.values = values;
    }

    public double getMiddle() {
        return middle;
    }

    public void setMiddle(double middle) {
        this.middle = middle;
    }
       
    public double[] selfOrder(){
        Arrays.sort(values);        
        return values;
    }
    
    public double getValue(int position){
        if (position > values.length){
            throw new UnsupportedOperationException("Posição " + position
					+ " inacessível no vetor de " + values.length + " posições");
        } else {
            return values[position];
        }
    }

    public void print() {        
        System.out.println("Lista de Valores:");
        for (int i = 0; i < values.length; i++) {
            System.out.print(" "+values[i]+" ");
        }
        System.out.println("");
        System.out.println("Middle: \n"+this.middle);
    }
    
}
