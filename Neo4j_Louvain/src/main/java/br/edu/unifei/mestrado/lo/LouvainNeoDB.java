package br.edu.unifei.mestrado.lo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.unifei.mestrado.commons.graph.GraphWrapper;
import br.edu.unifei.mestrado.commons.graph.NodeWrapper;
import br.edu.unifei.mestrado.commons.graph.db.GraphDB;
import br.edu.unifei.mestrado.commons.partition.BestPartition;
import br.edu.unifei.mestrado.commons.partition.NWayPartition;
import br.edu.unifei.mestrado.commons.partition.index.CutIndex;
import br.edu.unifei.mestrado.commons.partition.index.PartitionIndex;
import br.edu.unifei.mestrado.view.GraphView;
import java.util.HashMap;
import java.util.Map;

public class LouvainNeoDB extends Louvain {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private String dbFileName;

	public LouvainNeoDB(GraphWrapper graph) {
		super(graph, new GraphView(true));
		this.dbFileName = graph.getGraphFileName();
	}

	@Override
	protected GraphWrapper createNewGraph(int level) {
		// UTIL: os grafos de niveis contraidos nao podem reusar o banco
		GraphDB grafo = new GraphDB(dbFileName, level, GraphDB.REUSE_DB_NO);
		return grafo;
	}
	
	@Override
	protected NWayPartition createNewPartition(final GraphWrapper graph, int level) {
		PartitionIndex partitionIdx = graph.getCurrentPartitionIndex();
		CutIndex cutIdx = graph.getCurrentCutIndex();

                Map<Integer, Integer>  partitions = new HashMap<Integer, Integer>();
                for(NodeWrapper node : graph.getAllNodesStatic()){
                    int partition = node.getPartition();
                    if (partitions.containsKey(partition)){
                        partitions.replace(partition, partitions.get(partition)+1);
                    } else {
                        partitions.put(partition, 1);
                    }
                }
                int k = partitions.size();
                
		LouvainPartition partition = new LouvainPartition(level, k, partitionIdx, cutIdx);
		return partition;
	}

	@Override
	protected BestPartition executePartition(GraphWrapper graph) {
		long delta = System.currentTimeMillis();
                
                NWayPartition partition = new NWayPartition(graph.getLevel(), graph.getSizeNodes(), graph.getCurrentPartitionIndex(), graph.getCurrentCutIndex());
                partition.initialSequencialPartition(graph);
                
                return partition.createBestPartition(graph, graph);
                       
	}

	@Override
	protected BestPartition refinePartition(GraphWrapper graph, NWayPartition partition) {
		
                BestPartition part = partition.createBestPartition(graph, graph);
                /*
                BKL bkl = new BKL(graph, (BKLPartition)partition);
		BestPartition part = bkl.executeBKL();
		*/
                return part;
	}
}
