package app.transform;

import stocker.core.BasicPatternGraph;
import stocker.heuristic.OptimalNoStats;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.TransformBase;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpProject;
import com.hp.hpl.jena.sparql.core.BasicPattern;

public class StockerTransformONS extends TransformBase {

	Graph graph;
	
	public StockerTransformONS(Graph graph) {
		this.graph = graph;
	}

	@Override
	public Op transform(OpProject opp, Op subOp) {
		OpBGP opb = new OpBGP(reorderTriples(((OpBGP) subOp).getPattern(), graph));
		return new OpProject(opb, opp.getVars());
	}

	private BasicPattern reorderTriples(BasicPattern pattern, Graph graph) {
		BasicPatternGraph basicPatternGraph = new BasicPatternGraph(pattern,
				new OptimalNoStats(graph));
		// Optimize the abstracted graph and return the optimized BasicPattern
		return basicPatternGraph.optimize();

	}
}