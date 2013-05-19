package uk.co.jbothma.protege.protplug.extraction;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Utils;
import gate.corpora.SerialCorpusImpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import uk.co.jbothma.protege.protplug.Project;
import uk.co.jbothma.protege.protplug.Util;
import uk.co.jbothma.protege.protplug.candidate.RelationCandidate;
import uk.co.jbothma.protege.protplug.candidate.TermCandidate;

public class SubcategorisationFrames {
	public static void run(SerialCorpusImpl corp,  Project project) {
		Iterator<Document> docIter = corp.iterator();
		Document doc;
		while (docIter.hasNext()) {
			doc = docIter.next();
			
			String inputASName = "Original markups";
			AnnotationSet inputAS = doc.getAnnotations(inputASName);

			String sentASType = "sentence";
			Iterator<Annotation> sentIter = inputAS.get(sentASType).iterator();
			
			AnnotationSet termAS = inputAS.get("TermCandidate");
			
			while (sentIter.hasNext()) {
				Annotation sentAnnot = (Annotation) sentIter.next();
				AnnotationSet wordAS = inputAS.get("w");
				AnnotationSet containedWordsAS = Utils.getContainedAnnotations(wordAS, sentAnnot);
				List<Annotation> wordAnnList = Utils.inDocumentOrder(containedWordsAS);
				Annotation rootAnn = null;
				for (Annotation ann : wordAnnList) {
					FeatureMap features = ann.getFeatures();
					if (features.get("deprel").equals("ROOT") && features.get("dephead").equals("")) {
						rootAnn = ann;
					}
					if (!features.get("dephead").equals("")){
						try {
							int depHeadRef = Integer.parseInt((String)features.get("dephead"));
							String depRel = (String)features.get("deprel");
							Annotation depHead = wordAnnList.get(depHeadRef-1);
							addBranch(depHead, ann);
						} catch(NumberFormatException e) {}
					}
				}
				if (rootAnn != null) {
					if (rootAnn.getFeatures().get("pos").equals("VB")) {
						Set<Annotation> branches = (Set<Annotation>)rootAnn.getFeatures().get("branches");
						String subjStr = null;
						String objStr = null;
						String vgStr = Utils.stringFor(doc, rootAnn);
						if (branches != null) {
							for (Annotation branch : branches) {
								String depRel = (String)branch.getFeatures().get("deprel");
								if (depRel.equals("SS")) {
									subjStr = getContainingTermString(inputAS, termAS, branch);
								}
								if (depRel.equals("OO")) {
									objStr = getContainingTermString(inputAS, termAS, branch);
								}
								if (depRel.equals("VG")) {
									vgStr += " " + Utils.stringFor(doc, branch);
								}
							}
						}
						if (subjStr != null || objStr != null) {
							float subjConf = termCandConfidence(project.getTermCandidates(), subjStr);
							float objConf = termCandConfidence(project.getTermCandidates(), subjStr);
							// confidence in this relation's accuracy and relevance to the domain
							float conf = subjConf + objConf;
					        RelationCandidate relCand = new RelationCandidate(vgStr, subjStr, objStr, conf);
					        System.out.println(relCand.toString() + Float.toString(conf));
							project.getRelationCandidates().add(relCand);
						}
					}
				}
			}
			
			corp.unloadDocument(doc, false);
			Factory.deleteResource(doc);
		}
	}

	private static void addBranch(Annotation trunk, Annotation branch) {
		Set<Annotation> branches = (Set<Annotation>) trunk.getFeatures().get("branches");
		if (branches == null) {
			branches = new HashSet<Annotation>();
			trunk.getFeatures().put("branches", branches);
		}
		branches.add(branch);
	}

	private static String getContainingTermString(AnnotationSet inputAS, AnnotationSet termAS, Annotation branch) {
		AnnotationSet containingTermAS = Utils.getContainedAnnotations(termAS, branch);
		if (containingTermAS.size() > 0) {
			return Util.termAsLemmas(inputAS, containingTermAS.iterator().next());
		}
		return null;
	}
	
	private static float termCandConfidence(ArrayList<TermCandidate> termCands, String termLabel) {
		for (TermCandidate termCand : termCands) {
			if (termCand.getTerm().equals(termLabel))
				//System.out.println("conf of " + termLabel + " is " + Float.toString(termCand.getConfidence()));
				return termCand.getConfidence();
		}
		//System.err.println("Didn't find " + termLabel);
		return 0;
	}
}
