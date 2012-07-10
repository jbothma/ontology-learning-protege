package uk.co.jbothma.protege.protplug.candidate;

public class SubclassRelationCandidate {
	private String domain;
	private String range;

	public SubclassRelationCandidate(String domain, String range) {
		this.domain = domain;
		this.range = range;
	}
	
	public String toString() { return domain + " subclassOf " + range; }		
	public String getDomain() { return domain; }		
	public String getRange() { return range; }
}
