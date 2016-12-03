package br.edu.unifei.mestrado.lo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.unifei.mestrado.commons.graph.db.GraphDB;
import br.edu.unifei.mestrado.commons.partition.BestPartition;

public class MainLouvainNeo4J {

    private static Logger logger = LoggerFactory.getLogger(MainLouvainNeo4J.class);

    public static void main(String[] args) {
        
        String graphFileName = null;

        // executar assim: main dbFileName
        if (args.length == 1) {
            graphFileName = args[0];
        } else {
            //System.out.println("Uso: MainLouvainNeo4J graphFileName");
            //System.exit(2);
            graphFileName = "teste_10";
        }
        
        System.out.println("Iniciando grafo com Neo4J... file: " + graphFileName);
        long delta = System.currentTimeMillis();

        try {

            GraphDB graph = new GraphDB(graphFileName);
            Louvain lo = new LouvainNeoDB(graph);            
            
            logger.debug("Inicir particionamento");
            BestPartition resultPartition = lo.executeLouvain();
            delta = System.currentTimeMillis() - delta;
            resultPartition.exportPartition("lo-db.out", graphFileName, delta);

        } catch (Throwable e) {
            logger.error("Erro no Louvain com Neo4J with file: " + graphFileName, e);
        } finally {
            logger.warn("Finalizando Louvain com Neo4J - Tempo gasto: " + delta + " ms");
        }
    }
}
