package br.edu.unifei.mestrado.lo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.unifei.mestrado.commons.algo.AlgorithmObject;
import br.edu.unifei.mestrado.commons.graph.GraphWrapper;
import br.edu.unifei.mestrado.commons.graph.NodeWrapper;
import br.edu.unifei.mestrado.commons.graph.TransactionControl;
import br.edu.unifei.mestrado.commons.lo.LevelInformation;
import br.edu.unifei.mestrado.commons.partition.BestPartition;
import br.edu.unifei.mestrado.commons.partition.NWayPartition;
import br.edu.unifei.mestrado.commons.view.ViewListener;

public abstract class Louvain extends AlgorithmObject implements ViewListener {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    public static final int NUMBER_OF_NODES_TO_MERGE = 2;

    private MergeHelper mergeHelper;

    private LevelInformation levelInfo = new LevelInformation();
    
    //==========================================================================
    //Contrutores
    //==========================================================================

    public Louvain(GraphWrapper graph) {//update diagram
        super(graph);
        mergeHelper = new MergeHelper();
    }

    public Louvain(GraphWrapper graph, ViewListener view) {//update digram
        super(graph, view);
        mergeHelper = new MergeHelper();
    }
    
    //==========================================================================
    //Métodos abstratos para a implementação do algorítmo
    //==========================================================================
    
    protected abstract GraphWrapper createNewGraph(int level);

    protected abstract NWayPartition createNewPartition(final GraphWrapper graph, int level);

    //Refina a partição conforme o algorítmo específico
    protected abstract BestPartition refinePartition(GraphWrapper graph, NWayPartition partition); //update diagram

    //Executa o particionamento com o algorítmo específico
    protected abstract BestPartition executePartition(GraphWrapper graph);//update diagram
    
    //==========================================================================
    //Métodos do algoritmo
    //==========================================================================
    
    // Pre-processamento inicial do grafo
    protected void preprocess(GraphWrapper initialGraph) {
        // método stub pode ser usado pela sub class.
        
        TransactionControl transaction = new TransactionControl(initialGraph);
        transaction.beginTransaction();
        
        int i = 0;
        
        for (NodeWrapper node : initialGraph.getAllNodes()) {
                           
            i++;
            node.setPartition(i);
            transaction.intermediateCommit();
            
            logger.debug("Inicializando nó " + node.getId()+" com a partição "+node.getPartition());
            
        }
            
        transaction.commit();
        updateView(initialGraph, -1);
        
    }

    // Funde o grafo em N níveis
    private void executeMerging() {
        logger.warn("Contraindo o grafo...");
        
        levelInfo.addGraph(getGraph());

        logger.warn("Qtd de nodes do nivel " + levelInfo.getCurrentLevel() + ": " + levelInfo.getSizeNodesOfGraph());

        // UTIL: usado quando o grafo não diminuir mais na contração 
        boolean canMerge = true;
        
        while (canMerge && levelInfo.getSizeNodesOfGraph() > NUMBER_OF_NODES_TO_MERGE) {

            GraphWrapper newGraph = createNewGraph(levelInfo.getNextLevel());
            levelInfo.addGraph(newGraph);
            canMerge = mergeHelper.mergeOneLevel(levelInfo.getCurrentLevel(), levelInfo.getPreviousGraph(), levelInfo.getCurrentGraph());
             
            if (!canMerge) {
          //      break;
            } else {
                updateView(levelInfo.getPreviousGraph(), -1);
                updateViewCoarsed(levelInfo.getCurrentGraph(), -1);
            }
            
            //Descobrir o erro deste comando
            //levelInfo.getPreviousGraph().passivate();
              
                       
        }
        
    }

    // Expande o grafo em um nível
    private BestPartition unmergeOneLevel() {
        logger.warn("Iniciando unmerge do NIVEL: " + levelInfo.getCurrentLevel());

        //levelInfo.getPreviousGraph().activate();

        //TODO_OK: Mudar! para projetar, precisa da partiÃ§Ã£o do nÃ­vel anterior, e nÃ£o do grafo, talvez. No grafo os nodes jÃ¡ tem suas partiÃ§Ãµes.
        projectPartitionBack(levelInfo.getPreviousGraph(), levelInfo.getCurrentGraph(), levelInfo.getPreviousLevel());

        // so para mostrar na tela
        updateView(levelInfo.getPreviousGraph(), -1);
        updateViewCoarsed(levelInfo.getCurrentGraph(), -1);
 
        BestPartition improvedPart = improvePartition(levelInfo.getPreviousGraph(), levelInfo.getPreviousLevel());//, newPart);
       
   
        logger.warn("Qtd de vertices do nivel " + levelInfo.getCurrentLevel() + ": " + levelInfo.getSizeNodesOfGraph()
                + ", Peso do Corte: " + improvedPart.getCutWeight());

        logger.debug("Partition of level {}", (levelInfo.getCurrentLevel() - 1));
        improvedPart.printSets();

        // so para mostrar na tela
        if (levelInfo.getCurrentLevel() > 0) { // para nÃ£o dar erro no ultimo nivel
            updateView(levelInfo.getPreviousGraph(), -1);//levelInfo.getPreviousPartition().getCutWeight());
        }
        updateViewCoarsed(levelInfo.getCurrentGraph(), improvedPart.getCutWeight());
          
        return improvedPart;
    }

    /**
     * Repassa o particionamento do grafo mais contraÃ­do para o grafo mais
     * fino.
     *
     * @param fineGraph
     * @param coarsedGraph
     * @param previousLevel
     * @return
     */
    protected void projectPartitionBack(GraphWrapper fineGraph, GraphWrapper mergedGraph, int previousLevel) {//update diagram
        long delta = System.currentTimeMillis();

        //TODO_OK: rever isso: UTIL: a partição nova para a projeção de volta é feita em memória. Não. Cada mem/db faz o seu.
        TransactionControl transaction = new TransactionControl(fineGraph);
        try {
            transaction.beginTransaction();
            for (NodeWrapper node : fineGraph.getAllNodesStatic()) {

                int partitionId = -1;
                
                // UTIL: repassa o particionamento para o grafo mais fino
                if (node.hasInsideOf()) {
                    
                    long insideOf = node.getInsideOf();
                    NodeWrapper merged = mergedGraph.getNode(insideOf);
                    partitionId = merged.getPartition();
                
                } else {

                    // copia a partiÃ§Ã£o vertice do grafo mais contraido para o
                    // mais fino
                    NodeWrapper v = mergedGraph.getNode(node.getId());
                    partitionId = v.getPartition();
                }
                node.setPartition(partitionId);
                
                logger.debug("Para o nó "+node.getId()+ " a partição é "+partitionId);

                transaction.intermediateCommit();
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro repassando particionamento do grafo "
                    + mergedGraph.getLevel() + " para " + fineGraph.getLevel(), e);
        } finally {
            transaction.commit();
        }

        delta = System.currentTimeMillis() - delta;
        logger.debug("Tempo gasto para repassar o particionamento do grafo {} para {}: {} ms", new Object[]{
            mergedGraph.getLevel(), fineGraph.getLevel(), delta});
    }

    /**
     * Performs the refinement phase using the abstract method. The subclass
     * must handle its own transaction.
     *
     *
     * @param graph
     * @param level
     * @param partitionToRefine used to ease take the edges on cut.
     * @return
     */
    private BestPartition improvePartition(GraphWrapper graph, int level) {//update diagram
        BestPartition bestPart = null;
        try {
            NWayPartition partition = createNewPartition(graph, level);

            long deltaGenPart = System.currentTimeMillis();
            
            //UTIL: o objeto partitionToRefine serve para obter facilmente as arestas que jÃ¡ estÃ£o no corte.
            //TODO: testar esse generateBoundaryPartition para ver se nÃ£o dÃ¡ efeito colateral em outro lugar. Parece que nÃ£o.
            partition.generateBoundaryPartition(graph.getAllEdgesStatic(), graph);//partitionToRefine.queryEdgesOnCut());

            deltaGenPart = System.currentTimeMillis() - deltaGenPart;
            logger.debug("Tempo gasto para Louvain gerar boundary partition: " + deltaGenPart + " ms");

            bestPart = refinePartition(graph, partition);
            //bestPart = partition.createBestPartition(graph, graph);
        } catch (Exception e) {
            logger.error("Erro no refinamento. Verificar subclasse.", e);
        }
        return bestPart;
    }

    private BestPartition executeUnmerge(BestPartition originalPartition) {
        BestPartition part = originalPartition;
        logger.warn("Expandindo o grafo...");

        // enquanto existir um level
        while (levelInfo.hasMoreLevels()) {
            //TODO: liberar a memória da variável part, para os níveis intermediários

            part = unmergeOneLevel();

            if (levelInfo.hasMoreLevels()) {
                //levelInfo.getCurrentGraph().shutdown(true);
                levelInfo.removeGraph();
            }
        }

        logger.warn("Fim da fase de unmerging...");
        return part;
    }

    public BestPartition executeLouvain() {

        initView(getGraph(), getGraph().getSizeNodes());
        GraphWrapper initialGraph = getGraph();

        // fase de inicialização do grafo
        preprocess(initialGraph);
        
        // fase de merge
        executeMerging();
                
        // fase de particionamento
        BestPartition part = executePartition(levelInfo.getCurrentGraph());
        logger.warn("Corte após particionamento: " + part.getCutWeight());
        updateView(levelInfo.getCurrentGraph(), part.getCutWeight());
	
        // fase de uncoarsening
        part = executeUnmerge(part);
        return part;
    }

    @Override
    public void execute() {
        
        //Acho que se repete sem necessidade
        //initView(getGraph(), AbstractPartition.TWO_WAY);

        try {
            long time = System.currentTimeMillis();
            executeLouvain();
            time = System.currentTimeMillis() - time;
            logger.warn("Fim do Louvain. Tempo gasto: " + time + " ms File: "
                    + getGraph().getGraphFileName());
        } catch (Throwable e) {
            logger.error("Erro executando Louvain.", e);
        }
    }
}
