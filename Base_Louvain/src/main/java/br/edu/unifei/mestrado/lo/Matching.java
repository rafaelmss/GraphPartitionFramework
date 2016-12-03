package br.edu.unifei.mestrado.lo;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.unifei.mestrado.commons.graph.EdgeWrapper;
import br.edu.unifei.mestrado.commons.graph.GraphWrapper;
import br.edu.unifei.mestrado.commons.graph.NodeWrapper;
import br.edu.unifei.mestrado.commons.graph.TransactionControl;
import br.edu.unifei.mestrado.commons.lo.EdgesMerged;
import br.edu.unifei.mestrado.commons.lo.TempEdge;
import java.util.TreeMap;

public class Matching {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected EdgesMerged edgesMerged = new EdgesMerged();
    int count = 0;

    //***************************************************************************************************
    //MÈtodos para tratamento de nÛs contraidos
    //***************************************************************************************************
    
    public void mergeNodes(GraphWrapper newGraph, NodeWrapper v1, NodeWrapper v2) {
        // UTIL: cria o novo vertice, com um id novo para o grafo novo. Esse id vem do startNode
        
        NodeWrapper mergedVertex = null;
        
        if (v1.hasInsideOf()){
            mergedVertex = newGraph.getNode(v1.getInsideOf());
        } else if (v2.hasInsideOf()){
            mergedVertex = newGraph.getNode(v2.getInsideOf());
        } else {
            mergedVertex = newGraph.createNode(v1.getId(), v1.getWeight() + v2.getWeight());     
            mergedVertex.setCoarsed(true); // indica que ele foi contraido
            mergedVertex.setPartition(v2.getPartition()); 
        }
	// seta aqui, no fim do m√©todo, porque o addArestasFromVertex pega o insideOf se j√° existir, e da√≠
        // sempre vai existir... N√ÇO: sempre n√£o. pq ele verifica o insideOf do other ;)
        // sen√£o vai gerar uma auto aresta para o coarsedVertex
        // TODO: precisa voltar para o come√ßo do m√©todo, pq o m√©todo addArestasFromRemainingVertex usa ele
        // por causa do hasInsideOf do node contraido
        count++;
        v1.setInsideOf(mergedVertex.getId());
        v2.setInsideOf(mergedVertex.getId());
        v1.setPartition(v2.getPartition());

        addEdgesFromNode(v1, v2.getId(), mergedVertex);
        edgesMerged.imprimeArestasTmp();

        addEdgesFromNode(v2, v1.getId(), mergedVertex);
        edgesMerged.imprimeArestasTmp();

        for (TempEdge edge : edgesMerged) {    
            
            
            EdgeWrapper existEgde = newGraph.getEdgeLinking(edge.getStartNode(), edge.getEndNode());
            if (existEgde == null) {
                existEgde = newGraph.getEdgeLinking(edge.getEndNode(), edge.getStartNode());
            }
            
            if (existEgde == null) {
            
                // adiciona a aresta no grafo
                newGraph.createEdge(edge.getId(), edge.getWeight(), edge.getStartNode(), edge.getEndNode());
                logger.debug("Novo Edge criado com id "+edge.getId());
                
                
            } else {
            
                //Edita a aresta do grafo
                existEgde.sumWeight(edge.getWeight());
                logger.debug("Edge j· existente com id "+existEgde.getId());
                
            }
        }

        edgesMerged.clear();
    }

    //Adiciona as arestas ligadas no nÛ antigo com o novo nÛ
    private void addEdgesFromNode(NodeWrapper oldNode, long oldNodeOtherId, NodeWrapper mergedNode) {

        //Caminhamento para adicionar as arestas de oldNode
        for (EdgeWrapper edge : oldNode.getEdges()) {
            NodeWrapper otherOld = edge.getOtherNode(oldNode);
            
            // UTIL: esse if evita que arestas entre os dois nÛs, criem auto-arestas dos vertices contraidos.
            if (otherOld.getId() != oldNodeOtherId) {
                
                // UTIL: So adiciona uma nova aresta se o outro lado j· estiver contraido
                if (otherOld.hasInsideOf()) {
                    
                    //Pega o vertice correspondente
                    long otherCoarsed = otherOld.getInsideOf();
                    
                    //Evita auto-aresta com nÛs da mesma partiÁ„o
                    if (otherOld.getPartition() != oldNode.getPartition())
                    {

                        // Repassa o peso da aresta contraida para a aresta nova.
                        int weight = edge.getWeight();
                    
                        // Cria uma nova aresta com o novo node contra√≠do e o outro node contraido anteriormente.
                        TempEdge newEdge = new TempEdge(edge.getId(), weight, mergedNode.getId(), otherCoarsed);

                        // add aresta
                        edgesMerged.addEdge(mergedNode.getId(), otherCoarsed, newEdge);
                
                    }
                }
                
            }
        }
    }

    //***************************************************************************************************
    //MÈtodos para tratamento de nÛs n„o contraidos
    //***************************************************************************************************

    //Processa os nÛs n„o contraidos
    public void processRemainingNodes(Map<NodeWrapper, Boolean> nodes, GraphWrapper newGraph) {

        TransactionControl transaction = new TransactionControl(newGraph);
        try {
            transaction.beginTransaction();
            int qtdRepassados = 0;
            for (NodeWrapper oldNode : nodes.keySet()) {
                
                if (!oldNode.hasInsideOf())
                {
                    logger.debug("Movendo nÛ "+oldNode.getId());
                    // cria um novo, para n√£o usar a mesma instancia do grafo anterior
                    NodeWrapper newNode = newGraph.createNode(oldNode.getId(), oldNode.getWeight());

                    qtdRepassados++;
                    transaction.intermediateCommit();
                }
                    
            }
            
            qtdRepassados = 0;
            
            
            Map<Long, Boolean> edges = new TreeMap<Long, Boolean>();            
            
            
            for (NodeWrapper oldNode : nodes.keySet()) {
                
                if (!oldNode.hasInsideOf())
                {
                
                    for (EdgeWrapper edge : oldNode.getEdges()) {
                        
                        if (!edges.containsKey(edge.getId())){

                            edges.put(edge.getId(), true);
                            
                            NodeWrapper otherNode = edge.getOtherNode(oldNode);
                            long otherId = otherNode.getId();

                            if (otherNode.hasInsideOf()){
                                otherId = otherNode.getInsideOf();
                            }

                            EdgeWrapper existEgde = newGraph.getEdgeLinking(oldNode.getId(), otherId);
                            if (existEgde == null) {
                                existEgde = newGraph.getEdgeLinking(otherId, oldNode.getId());
                            }

                            if (existEgde == null) {

                                // adiciona a aresta no grafo
                                newGraph.createEdge(edge.getId(), edge.getWeight(), oldNode.getId(), otherId);
                                logger.debug("Novo Edge criado com id "+edge.getId()+ " ("+edge.getWeight()+")");


                            } else {

                                //Edita a aresta do grafo
                                existEgde.sumWeight(edge.getWeight());
                                logger.debug("Edge j· existente com id "+existEgde.getId()+ " ("+existEgde.getWeight()+")");

                            }

                            qtdRepassados++;
                            transaction.intermediateCommit();

                        }
                        
                    }
                    
                }
                    
            }
            
        } finally {
            transaction.commit();
        }
    }

}
