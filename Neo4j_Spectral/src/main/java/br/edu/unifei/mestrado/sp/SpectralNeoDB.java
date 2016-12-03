package br.edu.unifei.mestrado.sp;

import br.edu.unifei.mestrado.base_spectral.Spectral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.unifei.mestrado.commons.graph.GraphWrapper;
import br.edu.unifei.mestrado.commons.partition.BestPartition;
import br.edu.unifei.mestrado.commons.partition.TwoWayPartition;
import br.edu.unifei.mestrado.kl.KL;
import br.edu.unifei.mestrado.view.GraphView;

public class SpectralNeoDB extends Spectral {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private String dbFileName;

	public SpectralNeoDB(GraphWrapper graph) {
		super(graph, new GraphView(true));
		this.dbFileName = graph.getGraphFileName();
	}

	@Override
	protected BestPartition executePartition(GraphWrapper graph, boolean refine) {
		
                if (refine) {
                    
                        long delta = System.currentTimeMillis();
                        KL kl = new KL(graph);
                        BestPartition result = kl.executeKL();
                        result.printSets();
                        delta = System.currentTimeMillis() - delta;
                        logger.warn("Tempo gasto no KL: " + delta + " ms");
                        return result;
                
                } else {
                    
                        long delta = System.currentTimeMillis();                
                        TwoWayPartition partition = new TwoWayPartition(graph.getLevel(), graph.getCurrentPartitionIndex(), graph.getCurrentCutIndex());
                        partition.initialSamePartition(graph);
                        logger.warn("Tempo gasto no mapeamento da partição: " + delta + " ms");                
                        return partition.createBestPartition(graph, graph);
                    
                }
                       
	}
}
