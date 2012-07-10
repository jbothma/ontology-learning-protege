package uk.co.jbothma.protege.protplug.candidate;

public class TermCandidate {
	private String term;
	private float confidence;
	
	public TermCandidate(String term, float confidence) {
		this.term = term;
		this.confidence = confidence;
	}
	
	public String toString() { return term + " " + confidence; }		
	public String getTerm() { return term; }
	public float getConfidence() { return confidence; }
}