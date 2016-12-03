package br.edu.unifei.mestrado.fm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.unifei.mestrado.commons.graph.db.GraphDB;
import br.edu.unifei.mestrado.commons.partition.BestPartition;

public class MainFMNeo4J {

	private static Logger logger = LoggerFactory.getLogger(MainFMNeo4J.class);

	public static void main(String[] args) {
		String graphFileName = null;
		double ratio = -1D;

		// executar assim: main dbFileName
                if (args.length == 1) {
                        graphFileName = args[0];
                } else {
                        //System.out.println("Uso: MainLouvainNeo4J graphFileName");
                        //System.exit(2);
                        graphFileName = "teste_10";
                }

		logger.warn("Iniciando FM com Neo4J... file: " + graphFileName);
		long delta = System.currentTimeMillis();

		try {
			
			GraphDB graph = new GraphDB(graphFileName);
			FM kl = new FM(graph, ratio);
			BestPartition resultPartition = kl.executeFM();
			delta = System.currentTimeMillis() - delta;
			resultPartition.exportPartition("fm-db.out", graphFileName, delta);
			
		} catch(Throwable e) {
			logger.error("Erro no FM com Neo4J with file: " + graphFileName, e);
		} finally {
			logger.warn("Finalizando FM com Neo4J - Tempo gasto: " + delta + " ms");
		}
	}

}
