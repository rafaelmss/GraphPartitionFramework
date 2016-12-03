package br.edu.unifei.mestrado.sp;

import br.edu.unifei.mestrado.base_spectral.Spectral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.unifei.mestrado.commons.graph.db.GraphDB;
import br.edu.unifei.mestrado.commons.partition.BestPartition;

public class MainSpectralNeo4J {

    private static Logger logger = LoggerFactory.getLogger(MainSpectralNeo4J.class);

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
            Spectral sp = new SpectralNeoDB(graph);            
            
            logger.debug("Inicir particionamento");
            BestPartition resultPartition = sp.executeSpectral();
            delta = System.currentTimeMillis() - delta;
            resultPartition.exportPartition("sp-db.out", graphFileName, delta);

        } catch (Throwable e) {
            logger.error("Erro no Spectral com Neo4J with file: " + graphFileName, e);
        } finally {
            logger.warn("Finalizando Spectral com Neo4J - Tempo gasto: " + delta + " ms");
        }
    }
}
