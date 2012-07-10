package uk.co.jbothma.protege.protplug.candidate;

public class RelationCandidate {
	private String domain;
	private String range;
	private String label;

	public RelationCandidate(String label, String domain, String range) {
		this.label = label;
		this.domain = domain;
		this.range = range;
	}
	
	public String toString() { return domain + " (" + label + ") " + range; }		
	public String getDomain() { return domain; }		
	public String getRange() { return range; }		
	public String getLabel() { return label; }
}