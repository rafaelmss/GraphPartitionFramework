package br.edu.unifei.mestrado.graph.generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import org.graphstream.algorithm.generator.Generator;
import org.graphstream.algorithm.generator.RandomGenerator;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

public class GraphExporter {

    public static final String dirTarget = "gerados/";
    public static final String DB_PATH = "neo4j-";

    private final int numNodes;
    private final long typeOfGraph;
    private final Random random;

    private Map<Long, Map<Long, Long>> edges = new HashMap<Long, Map<Long, Long>>();

    private long edgeCount = 0;

    public GraphExporter(int numNodes, long typeOfGraph, Random random) {
            this.numNodes = numNodes;
            this.typeOfGraph = typeOfGraph;
            this.random = random;
    }

    public void generateGraph(String databaseName, boolean exportFile, boolean exportDB) {

        File target = new File(dirTarget);
        if (!target.exists()) {
                target.mkdirs();
        }
        databaseName = target + "/" + DB_PATH + databaseName;
        String fileName = databaseName + ".txt";
        GrafoNeoDB graphDb = null;

        if (exportDB) {
                graphDb = new GrafoNeoDB(databaseName);
                graphDb.initDB();
        }

        PrintWriter graphFile = null;
        if (exportFile) {
                File file = new File(fileName);
                file.delete();
                FileOutputStream fos = null;
                try {
                        fos = new FileOutputStream(fileName);
                } catch (FileNotFoundException e) {
                        e.printStackTrace();
                }
                graphFile = new PrintWriter(fos);
        }

        if (exportDB) {
                graphDb.beginTransaction();

                for (int i = 0; i < numNodes; i++) {
                        graphDb.createNode(i, 1);
                }
        }





        Graph graph = new SingleGraph("Random");
        Generator gen = new RandomGenerator(2);
        gen.addSink(graph);
        gen.begin();      
        for(int i=0; i<numNodes; i++){
            gen.nextEvents();
        }
        gen.end();




        if (exportFile){ 
                graphFile.println(graph.getEdgeCount());
        }

        Iterator<Edge> edge_list = graph.getEdgeIterator();        
        while (edge_list.hasNext()){
            Edge edge = edge_list.next();

            Node n1 = edge.getNode0();
            Node n2 = edge.getNode1();

            long idx1 = Integer.valueOf(n1.getId());
            long idx2 = Integer.valueOf(n2.getId());

            if (exportFile) {
                graphFile.println(idx1 + " " + idx2);
            }

            if (exportDB) {
                graphDb.createChain(idx1, idx2, 1, edgeCount, 1);
            }                    

        }  

        if (exportFile) {
                graphFile.flush();
        }

        if (exportDB) {
                graphDb.endTransaction();
                graphDb.finish();
        }
    }
}
