package br.edu.unifei.mestrado.lo;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.unifei.mestrado.commons.graph.GraphWrapper;
import br.edu.unifei.mestrado.commons.graph.NodeWrapper;
import br.edu.unifei.mestrado.commons.graph.TransactionControl;

public class MergeHelper {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    // Faz a contraÁ„o do grafo oldGraph no novo grafo newGraph
    public boolean mergeOneLevel(final int level, GraphWrapper oldGraph, GraphWrapper newGraph) {
        logger.warn("Inicio do merge NIVEL: " + level);
        long delta = System.currentTimeMillis();

        mergeGraphPartition(oldGraph, newGraph);
        delta = System.currentTimeMillis() - delta;

        logger.warn("Qtd de nodes do nivel " + level + ": " + newGraph.getSizeNodes()
                + " tempo de contraÁ„o: " + delta + " ms");

        // UTIL: indica se diminuiu a quantidade de vertices
        return oldGraph.getSizeNodes() > newGraph.getSizeNodes();
    }

    //Faz a contra√ß√£o usando emparelhamento por ganho de modularidade
    protected void mergeGraphPartition(GraphWrapper oldGraph, GraphWrapper newGraph) {

        // UTIL: Esse treeMap √© usado para saber se o n√≥ j√° foi ou n√£o locked.
        Map<NodeWrapper, Boolean> nodes = new TreeMap<NodeWrapper, Boolean>(new Comparator<NodeWrapper>() {
            @Override
            public int compare(NodeWrapper o1, NodeWrapper o2) {
                int diff = o1.getWeight() - o2.getWeight();
                if (diff == 0) {
                    return (int) (o1.getId() - o2.getId());
                }
                return diff;
            }
        });

        TransactionControl tc = new TransactionControl(oldGraph);
        tc.beginTransaction();

        for (NodeWrapper node : oldGraph.getAllNodes()) { //TODO: isso È um problema grave de uso de memÛria
            nodes.put(node, false);
            node.resetInsideOf();
            tc.intermediateCommit();
        }
        tc.commit();

        mergeGraphWithNodes(newGraph, oldGraph, nodes);

        nodes.clear();

    }

    //TODO: se usar muita mem√≥ria, esse m√©todo pode ser feito usando lock direto no node.
    private void mergeGraphWithNodes(GraphWrapper newGraph, final GraphWrapper oldGraph, Map<NodeWrapper, Boolean> nodes) { //update digram

        Matching mathcing = new Matching();
        LouvainModularity modularity = new LouvainModularity(oldGraph);

        TransactionControl tcNew = new TransactionControl(newGraph);
        TransactionControl tcOld = new TransactionControl(oldGraph);

        try {

            tcNew.beginTransaction();
            tcOld.beginTransaction();

            for (NodeWrapper node : nodes.keySet()) {

                if (!nodes.get(node)) {
                    
                    NodeWrapper otherNode = modularity.nodeWithMoreModularityGain(node);
                   
                    if (otherNode != null) {
                        
                        logger.debug("Para o nÛ "+node.getId()+" o ganho foi "+otherNode.getId());
                    
                        mathcing.mergeNodes(newGraph, node, otherNode);

                        //Lock v1 e v2
                        nodes.put(node, true);
                        nodes.put(otherNode, true);
                        tcNew.intermediateCommit();
                        tcOld.intermediateCommit();

                    } else {
                        
                        logger.debug("Para o nÛ "+node.getId()+" n„o houve ganhador");
                        
                    }
                }

            }
        } catch (Throwable e) {
            logger.error("Erro contraindo grafo nivel: " + newGraph.getLevel(), e);
        } finally {
            tcNew.commit();
            tcOld.commit();
        }

        mathcing.processRemainingNodes(nodes, newGraph);//processa os nodes brancos
    }

}
