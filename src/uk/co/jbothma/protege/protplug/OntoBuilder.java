package uk.co.jbothma.protege.protplug;

import java.util.Collection;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.co.jbothma.protege.protplug.candidate.RelationCandidate;
import uk.co.jbothma.protege.protplug.candidate.SubclassRelationCandidate;
import uk.co.jbothma.protege.protplug.candidate.TermCandidate;

public class OntoBuilder {

	private OWLOntology ontology;
	
	private OWLOntologyManager ontologyManager;
	
	private OWLDataFactory factory;
	
	private IRI ontologyIRI;
	
	public static void example(OWLModelManager owlModelManager) {
		OWLOntology ontology = owlModelManager.getActiveOntology();
		
		OWLOntologyManager ontologyManager = ontology.getOWLOntologyManager();
		
		OWLDataFactory factory = ontologyManager.getOWLDataFactory();
		
		IRI ontologyIRI = ontology.getOntologyID().getOntologyIRI();
		
		OWLClass clsA = factory.getOWLClass(IRI.create(ontologyIRI + "#A"));
		OWLClass clsB = factory.getOWLClass(IRI.create(ontologyIRI + "#B"));
		
		OWLAxiom axiom = factory.getOWLSubClassOfAxiom(clsA, clsB);
		
		AddAxiom addAxiom = new AddAxiom(ontology, axiom);
		
		ontologyManager.applyChange(addAxiom);
		
		for (OWLClass cls : ontology.getClassesInSignature()) {
			System.out.println("Referenced class: " + cls);
		}
	}
	
	public OntoBuilder(OWLModelManager owlModelManager) {
		ontology = owlModelManager.getActiveOntology();
		ontologyManager = ontology.getOWLOntologyManager();
		factory = ontologyManager.getOWLDataFactory();
		ontologyIRI = ontology.getOntologyID().getOntologyIRI();
	}
	
	public void addSubclassRelations(Collection<SubclassRelationCandidate> relations) {
		OWLClass domain, range;
		for (SubclassRelationCandidate relation : relations) {
			domain = factory.getOWLClass(
					IRI.create(ontologyIRI + "#" + makeClass(relation.getDomain())));
			range = factory.getOWLClass(
					IRI.create(ontologyIRI + "#" + makeClass(relation.getRange())));

			OWLAxiom axiom = factory.getOWLSubClassOfAxiom(domain, range);
			
			AddAxiom addAxiom = new AddAxiom(ontology, axiom);

			ontologyManager.applyChange(addAxiom);
		}
	}
	
	private String makeClass(String term) {
		String regex = "[^\\- 0-9\\x30-\\x39\\x41-\\x5A\\x61-\\x7A\\xC0-\\xD6\\xD8-\\xF6\\xF8-\\xFF]";
		return spaceLowerToCamel(term.replaceAll(regex, "_"));
	}
	
	private static String spaceLowerToMixed(String s) {
		return spaceLowerToCamel(s).substring(0, 1).toUpperCase() + s.substring(1);
	}
	
	private static String spaceLowerToCamel(String s) {
		String[] parts = s.split(" ");
		String camelCaseString = "";
		for (String part : parts) {
			camelCaseString = camelCaseString + toProperCase(part);
		}
		return camelCaseString;
	}
	
	private static String toProperCase(String s) {
	    return s.substring(0, 1).toUpperCase() +
	               s.substring(1).toLowerCase();
	}
}
