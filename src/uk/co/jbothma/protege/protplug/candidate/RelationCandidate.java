package uk.co.jbothma.protege.protplug.candidate;

public class RelationCandidate {
	private String domain;
	private String range;
	private String label;
	private float confidence;
	private int occurrences;

	public RelationCandidate(String label, String domain, String range, float confidence) {
		this.label = label;
		this.domain = domain;
		this.range = range;
		this.confidence = confidence;
		this.occurrences = 1;
	}
	
	public String toString() { return domain + " (" + label + ") " + range; }		
	public String getDomain() { return domain; }		
	public String getRange() { return range; }		
	public String getLabel() { return label; }
	public float getConfidence() { return confidence; }
	public int getOccurrences() { return occurrences; }

	public void addOccurrence(float occurrenceConfidence) {
		this.occurrences++;
		this.confidence += occurrenceConfidence;
	}

	public boolean isEquivalent(String vgStr, String subjStr, String objStr) {
		return label.equals(vgStr) && domain.equals(subjStr) && range.equals(objStr);
	}
}