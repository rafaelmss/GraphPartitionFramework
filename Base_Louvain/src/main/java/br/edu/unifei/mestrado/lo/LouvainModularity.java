/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.unifei.mestrado.lo;

import br.edu.unifei.mestrado.commons.graph.EdgeWrapper;
import br.edu.unifei.mestrado.commons.graph.GraphWrapper;
import br.edu.unifei.mestrado.commons.graph.NodeWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rafael
 */
public class LouvainModularity {
    
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private GraphWrapper graph;
    private long numEdges = 0;
    
    public LouvainModularity(GraphWrapper graph){
        this.graph = graph;
        for (EdgeWrapper edge : graph.getAllEdgesStatic()) 
            numEdges++;
    }    

    public double computeGainModularity(NodeWrapper node1, NodeWrapper node2) {

        int originalPartition = node1.getPartition();
        graph.getNode(node1.getId()).setPartition(node2.getPartition());
        double modularity = computeModularity(node1);
        graph.getNode(node1.getId()).setPartition(originalPartition);

        return modularity;

    }

    //Computa a modularidade do grafo
    private double computeModularity(NodeWrapper node) {

        int partition = node.getPartition();

        double sum = 0;

        double m2 = (double) (2 * (numEdges));

        double s_in = 0;    //soma dos pesos das arestas de dentro da particao
        double s_tot = 0;   //soma dos pesos das arestas que incidem na particao

        double k_i = 0;     //soma dos pesos das arestas que incidem no nó I
        double k_iin = 0;   //soma dos pesos das arestas que incidem no nó I originarios da mesma partição

        for (NodeWrapper v : graph.getAllNodesStatic()) {

            if (v.getPartition() == partition) {

                for (EdgeWrapper edge : v.getEdgesStatic()) {

                    NodeWrapper otherNode = edge.getOtherNode(v);
                    
                    //Se os dois estão na mesma partição
                    if (v.getPartition() == otherNode.getPartition()) {
                        s_in = s_in + edge.getWeight();
                    } else {
                        s_tot = s_tot + edge.getWeight();
                    }

                    //Se o nó analisado é o Node                    
                    if (v.getId() == node.getId()) {
                        if (otherNode.getPartition() == partition) {
                            k_iin = k_iin + edge.getWeight();
                            k_i = k_i + edge.getWeight();
                        } else {
                            k_i = k_i + edge.getWeight();
                        }
                    }

                }

            }
        }
        
        s_in = s_in / 2; //na mesma particao é contato em dobro
                
        return ( ((s_in + k_iin) / m2 - Math.pow(((s_tot + k_i) / m2), 2)) - ((s_in / m2) - Math.pow((s_tot / m2), 2) - Math.pow((k_i / m2), 2)) );
    }   
    
    public NodeWrapper nodeWithMoreModularityGain (NodeWrapper node) {

        NodeWrapper otherNode = null;
        double gain = 0;

        for (EdgeWrapper edge : node.getEdges()) {

            double new_gain = computeGainModularity(node, edge.getOtherNode(node));
            
            if (new_gain > gain) {
                gain = new_gain;
                otherNode = edge.getOtherNode(node);
            }

        }
        
        return otherNode;
        
    }
    
}
