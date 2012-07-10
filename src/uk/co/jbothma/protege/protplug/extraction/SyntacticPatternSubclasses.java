package uk.co.jbothma.protege.protplug.extraction;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.Factory;
import gate.Utils;
import gate.corpora.SerialCorpusImpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import uk.co.jbothma.protege.protplug.candidate.SubclassRelationCandidate;

public class SyntacticPatternSubclasses {
	public static void run(SerialCorpusImpl corp, ArrayList<SubclassRelationCandidate> subclassRelCandidates) {
		Iterator<Document> docIter = corp.iterator();
		Document doc;
		while (docIter.hasNext()) {
			doc = docIter.next();
			
			String inputASName = "Original markups";
			AnnotationSet inputAS = doc.getAnnotations(inputASName);
			
			String relationASType = "SubclassOf";
			Iterator<Annotation> relationIter = inputAS.get(relationASType).iterator();
			
			while (relationIter.hasNext()) {
				Annotation relationAnnot = (Annotation) relationIter.next();
				String superclass = superclass(inputAS, relationAnnot);
				Set<String> subclasses = subclasses(inputAS, relationAnnot);
				for (String subclass : subclasses) {
					subclassRelCandidates.add(new SubclassRelationCandidate(subclass, superclass));
				}
			}
			
			corp.unloadDocument(doc, false);
			Factory.deleteResource(doc);
		}
	}
	
	private static String superclass(AnnotationSet inputAS, Annotation relation) {
		AnnotationSet superclassAS = inputAS.get("Range");
		AnnotationSet containedSuperclassAS = Utils.getContainedAnnotations(superclassAS, relation);
		return Utils.stringFor(inputAS.getDocument(), containedSuperclassAS);
	}

	private static Set<String> subclasses(AnnotationSet inputAS, Annotation relation) {
		AnnotationSet subclassAS = inputAS.get("Domain");
		AnnotationSet termAS = inputAS.get("TermCandidate");
		AnnotationSet containedSubclassPartAS = Utils.getContainedAnnotations(subclassAS, relation);
		AnnotationSet subclassTermsAS = Utils.getContainedAnnotations(termAS, containedSubclassPartAS);
		Set<String> subclassStrings = new HashSet<String>();
		Iterator<Annotation> iter = subclassTermsAS.iterator();
		while (iter.hasNext()) {
			subclassStrings.add(Utils.stringFor(inputAS.getDocument(), iter.next()));
		}
		return subclassStrings;
	}
}
