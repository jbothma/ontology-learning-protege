package uk.co.jbothma.protege.protplug;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Utils;

import java.util.List;

public class Util {
	/**
	 * get the lemmas of the tokens that lie within termCandAnnot
	 * @param inputAS
	 * @param termCandAnnot
	 * @return
	 */
	public static String termAsLemmas(AnnotationSet inputAS, Annotation termCandAnnot) {
		String term = "";
		AnnotationSet tokAnnots = gate.Utils.getContainedAnnotations(
				inputAS, termCandAnnot, "w");
		List<Annotation> tokAnnotList = gate.Utils.inDocumentOrder(tokAnnots);
		
		for (Annotation tokAnnot : tokAnnotList) {
			String lemma = tokenLemma(inputAS.getDocument(), tokAnnot);
			term += lemma + " ";
		}
		
		return term.toLowerCase().trim();
	}
	
	public static String tokenLemma(gate.Document doc, Annotation tokAnnot) {
		String lemma;
		String[] lemmas;
		String lemmaAnnotStr = (String)tokAnnot.getFeatures().get("lemma");
		if (lemmaAnnotStr != null) {
			lemmas = lemmaAnnotStr.split("\\|");
			if (lemmas.length > 1) {
				lemma = lemmas[1];
				if (!lemma.equals("")) {
					return lemma;
				}
			}
		}
		// fall back to raw string
		lemma = Utils.stringFor(doc, tokAnnot).toLowerCase();
		return lemma;
	}
}
