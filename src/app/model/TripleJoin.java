package app.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.hp.hpl.jena.graph.Triple;

public class TripleJoin {

	Triple a;
	Triple b;
	Double cost;
	public String joinType;
	
	public TripleJoin(Triple a, Triple b){
		this.a = a;
		this.b = b;
		if(a.getSubject().equals(b.getSubject()))
			joinType = "ss";
		else if(a.getSubject().equals(b.getObject()))
			joinType = "so";
		else if(a.getObject().equals(b.getSubject()))
			joinType = "os";
		else if(a.getObject().equals(b.getObject()))
			joinType = "oo";
		
	}
	
	public void setCost(Double cost){
		this.cost = cost;
	}
	
	 @Override
	    public int hashCode() {
	        return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
	            // if deriving: appendSuper(super.hashCode()).
	            append(a).
	            append(b).
	            toHashCode();
	    }
	 
	 @Override
	    public boolean equals(Object obj) {
	       if (!(obj instanceof TripleJoin))
	            return false;
	        if (obj == this)
	            return true;

	        TripleJoin rhs = (TripleJoin) obj;
	        return new EqualsBuilder().
	            // if deriving: appendSuper(super.equals(obj)).
	            append(a, rhs.a).
	            append(b, rhs.b).
	            isEquals();
	    }
	
}
