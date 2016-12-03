package br.edu.unifei.mestrado.base_spectral.linears;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rafael
 */
public class LinearSystem {
    
    private Matrix factors;

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    public LinearSystem(int order) {       
        factors = new Matrix(order,order);
        logger.debug("Sistema Linear de ordem "+order);        
    }
    
    public void addEquationFactors(int position, double[]eqFactors) {
        double[][] vals = {eqFactors}; 
        Matrix elements = new Matrix(vals);
        this.factors.setMatrix(position,position,0, eqFactors.length-1, elements);
    }
    
    public Eigenvalue getSecondEigenvalue() {
        EigenvalueDecomposition eigenvalues = factors.eig();
        double[] result = eigenvalues.getRealEigenvalues();
        Arrays.sort(result);        
        Eigenvalue e = new Eigenvalue(result[1]);       
        return e;        
    }
    
    public Eigenvector getSecondEigenvector() {
    
        EigenvalueDecomposition eigenvalues = factors.eig();
        Matrix eigenvectors = eigenvalues.getV();
        
        double[][] vectors = eigenvectors.getArray();
        double[] result = new double[vectors.length];
        for (int i = 0; i < vectors.length; i++) {            
            result[i] = vectors[i][1];
        }
        
        Eigenvector v = new Eigenvector(result);
        
        return v;
               
    }
    
    public Eigenvector getSecondEigenvectorNorm() {
    
        EigenvalueDecomposition eigenvalues = factors.eig();
        Matrix eigenvectors = eigenvalues.getV();
        
        
        for (int i = 0; i < eigenvectors.getRowDimension(); i++) {
            System.out.print(" ");
            for (int j = 0; j < eigenvectors.getColumnDimension(); j++) {
                System.out.print(" "+eigenvectors.get(i, j)+" ");                
            }
            System.out.println(" ");
        }
        
        double[][] vectors = eigenvectors.getArray();
        double[] result = new double[vectors.length];
        for (int i = 0; i < vectors.length; i++) {            
            result[i] = vectors[i][1]*Math.sqrt(vectors.length);
        }
        
        
        
        Eigenvector v = new Eigenvector(result);
        
        return v;
               
    }
        
    public void printSystem() {
        for (int i = 0; i < factors.getRowDimension(); i++) {
            System.out.print(" ");
            for (int j = 0; j < factors.getColumnDimension(); j++) {
                System.out.print(" "+factors.get(i, j)+" ");                
            }
            System.out.println(" ");
        }
    }
            
}
