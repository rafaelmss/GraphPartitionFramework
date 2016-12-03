/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.unifei.mestrado.base_spectral;

import br.edu.unifei.mestrado.base_spectral.linears.Eigenvector;
import br.edu.unifei.mestrado.base_spectral.linears.LinearSystem;
import br.edu.unifei.mestrado.commons.algo.AlgorithmObject;
import br.edu.unifei.mestrado.commons.graph.EdgeWrapper;
import br.edu.unifei.mestrado.commons.graph.GraphWrapper;
import br.edu.unifei.mestrado.commons.graph.NodeWrapper;
import br.edu.unifei.mestrado.commons.graph.TransactionControl;
import br.edu.unifei.mestrado.commons.partition.AbstractPartition;
import br.edu.unifei.mestrado.commons.partition.BestPartition;
import br.edu.unifei.mestrado.commons.view.ViewListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rafael
 */
public abstract class Spectral extends AlgorithmObject implements ViewListener {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    public Spectral(GraphWrapper graph) {
        super(graph);
    }
    
    public  Spectral(GraphWrapper graph, ViewListener view){
        super(graph,view);
    }
    
    //Executa o particionamento com o algorítmo específico
    protected abstract BestPartition executePartition(GraphWrapper graph, boolean refine);//update diagram
        
    public Eigenvector calculateEigenvector(GraphWrapper graph){
        
        LinearSystem ls = new LinearSystem(graph.getSizeNodes());
          
        for (NodeWrapper node : graph.getAllNodesStatic()) {
            
            double[] equation = new double[graph.getSizeNodes()];
            //Arrays.fill(equation,0);
                        
            long nodeId = node.getId();
            
            if (nodeId < graph.getSizeNodes()){
                                      
                equation[(int)nodeId] = node.getDegree();
                        
                for (EdgeWrapper edge : node.getEdgesStatic()) {
                    long otherId = edge.getOtherNode(node).getId();
                    if ((int)otherId < equation.length ) {
                        equation[(int)edge.getOtherNode(node).getId()] = 0 - edge.getWeight();
                    } else {
                        System.out.println("id "+otherId);
                    }
                }
                         
                ls.addEquationFactors((int)nodeId, equation);
                
            }
          
        }
        
        return ls.getSecondEigenvectorNorm();
        
    }
        
    public void setPartitionNodes(Eigenvector eigenvector, GraphWrapper graph){
                
        TransactionControl tc = new TransactionControl(graph);
        tc.beginTransaction();
        
        for (NodeWrapper node : graph.getAllNodes()) {
            
            long nodeId = node.getId();
            
            if (nodeId < eigenvector.getValues().length){

                if (eigenvector.getValue((int)nodeId) <= eigenvector.getMiddle()){
                    node.setPartition(AbstractPartition.PART_1);
                } else {
                    node.setPartition(AbstractPartition.PART_2);
                }
                System.out.println("Nó "+node.getId()+" na partição "+node.getPartition()+" com cofator "+(eigenvector.getValue((int)nodeId)));
                tc.intermediateCommit();    
            
            }            
            
        }
        
        tc.commit();
        
    }
       
    public BestPartition executeSpectral() {

        initView(getGraph(), getGraph().getSizeNodes());
        GraphWrapper initialGraph = getGraph();

        // fase de calculo do autovetor espectral
        Eigenvector eigenvector = calculateEigenvector(getGraph());
        eigenvector.print();
               
        // definindo as partições
        setPartitionNodes(eigenvector,getGraph());             
        
        // fase de particionamento
        BestPartition part = executePartition(getGraph(), false);
        logger.warn("Corte após particionamento: " + part.getCutWeight());
        updateView(getGraph(), part.getCutWeight());
	
        return part;
    }
    
    @Override
    public void execute() {
        
        try {
            long time = System.currentTimeMillis();
            executeSpectral();
            time = System.currentTimeMillis() - time;
            logger.warn("Fim do Spectral. Tempo gasto: " + time + " ms File: "
                    + getGraph().getGraphFileName());
        } catch (Throwable e) {
            logger.error("Erro executando Spectral.", e);
        }
    }
    
}
