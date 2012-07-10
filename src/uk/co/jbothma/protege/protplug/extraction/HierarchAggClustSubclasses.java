package uk.co.jbothma.protege.protplug.extraction;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import uk.co.jbothma.protege.protplug.candidate.SubclassRelationCandidate;
import uk.co.jbothma.protege.protplug.candidate.TermCandidate;
import uk.co.jbothma.taxonomy.AggHierarchClust;
import uk.co.jbothma.taxonomy.Cluster;
import uk.co.jbothma.taxonomy.Term;

public class HierarchAggClustSubclasses {
	public static void run(List<TermCandidate> termCandidates, List<SubclassRelationCandidate> subclassRelCandidates) {
		Set<String[]> terms = new HashSet<String[]>();
		int startIdx = 0;
		int endIdx = termCandidates.size()-1;
		if (endIdx>500) {
			startIdx = endIdx-500;
		}
		// select 500 terms with highest confidence
		for (TermCandidate term : termCandidates.subList(startIdx, endIdx)) {
			terms.add(term.getTerm().split(" "));
		}
		
		AggHierarchClust clustering = new AggHierarchClust(terms.toArray(new String[][]{}));
		clustering.cluster();
		Set<Cluster> clusters = clustering.getClusters();
		Set<Term> clusterTerms;
		SubclassRelationCandidate cand;
		for (Cluster cluster : clusters) {
			if (cluster.getTerms().size() > 1) {
				clusterTerms = cluster.getTerms();
				for (Term term : clusterTerms) {
					String domain = StringUtils.join(term.getParts(), " ");
					cand = new SubclassRelationCandidate(domain, term.getHead());
					subclassRelCandidates.add(cand);
				}
			}
		}
	}
}
