package br.edu.unifei.mestrado.graph.generator;

import java.util.Date;
import java.util.Random;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainGenerator {

	private static Logger logger = LoggerFactory.getLogger(MainGenerator.class);

	// check: GraphEditorDemo, SimpleGraphDraw
	public static void main(String[] args) {

		try {
                    
                        //Inicializa as variáveis
			boolean exportFile = false;
			boolean exportDB = false;
			Integer numNodes = null;
			Integer typeOfGraph = null;
                        
                        args = new String[] {"100","50","true","true"};
                        
                        //Tenta setar o valor das variáveis com os argumentos
			if (args.length > 0) {
				numNodes = Integer.parseInt(args[0]);
				if (args.length > 1) {
					typeOfGraph = Integer.parseInt(args[1]);
					if (args.length > 2) {
						exportFile = args[2].equals("true");
						if (args.length > 3) {
							exportDB = args[3].equals("true");
						}
					} else {
						logger.info("Especifique as opÃ§Ãµes exportFile e exportDB");
						System.exit(3);
					}
				}
			}
			if (numNodes == null || typeOfGraph == null) {
				logger.info("Entre com a quantidade de vertices, o tipo do grafo: ");
				Scanner sc = new Scanner(System.in);
				numNodes = sc.nextInt();
				typeOfGraph = sc.nextInt();
			}
			if (!exportFile && !exportDB) {
				logger.info("Especifique as opÃ§Ãµes exportFile ou exportDB");
				System.exit(4);
			}
                        
                        //Inicializa o random
			long seed = Integer.MAX_VALUE;
			long delta = System.currentTimeMillis();
                        
                        //Exibe mensagem inicial
			logger.info("Inicio: " + new Date());
			logger.info("Gerando um grafo com " + numNodes + " nós...");
			
                        //Inicializa a variável para exportar o grafo
			GraphExporter exporter = new GraphExporter(numNodes, typeOfGraph, new Random(seed));
                        
                        //Gera o grafo
			String fileName = "grafo_v" + numNodes;
			exporter.generateGraph(fileName, exportFile, exportDB);

                        //Determina e exibe o tempo de execução
			delta = System.currentTimeMillis() - delta;
			logger.info("Termino - Tempo gasto: " + delta + "ms Hora: " + new Date());
                        
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
