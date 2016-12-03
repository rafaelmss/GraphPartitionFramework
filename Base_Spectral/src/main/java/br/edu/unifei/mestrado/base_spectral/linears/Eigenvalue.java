/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.unifei.mestrado.base_spectral.linears;

/**
 *
 * @author rafael
 */
public class Eigenvalue {
    
    private double value;

    public Eigenvalue(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Eigenvalue{" + "value=" + value + '}';
    }
    
}
