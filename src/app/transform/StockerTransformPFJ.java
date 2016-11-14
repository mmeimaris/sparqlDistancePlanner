package app.transform;

import stocker.core.BasicPatternGraph;
import stocker.heuristic.ProbabilisticFrameworkJoin;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.TransformBase;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpProject;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.tdb.TDB;

/**
 * 
 * @author E. Guzel Kalayci
 * 
 */
public class StockerTransformPFJ extends TransformBase {

	@Override
	public Op transform(OpProject opp, Op subOp) {
		OpBGP opb = new OpBGP(reorderTriples(((OpBGP) subOp).getPattern()));
		return new OpProject(opb, opp.getVars());
	}

	private BasicPattern reorderTriples(BasicPattern pattern) {
		BasicPatternGraph basicPatternGraph = new BasicPatternGraph(pattern,
				new ProbabilisticFrameworkJoin(
						ARQ.getContext()));
		// Optimize the abstracted graph and return the optimized BasicPattern
		return basicPatternGraph.optimize();
	}
}