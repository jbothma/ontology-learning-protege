package uk.co.jbothma.protege.protplug;

import java.util.Collection;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
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
	}
	
	public OntoBuilder(OWLModelManager owlModelManager) {
		ontology = owlModelManager.getActiveOntology();
		ontologyManager = ontology.getOWLOntologyManager();
		factory = ontologyManager.getOWLDataFactory();
		ontologyIRI = ontology.getOntologyID().getOntologyIRI();
	}
	
	public void addTerms(Collection<TermCandidate> terms) {
		OWLClass termClass;
		for (TermCandidate term : terms) {
			termClass = factory.getOWLClass(
					IRI.create(ontologyIRI + "#" + makeClass(term.getTerm())));
			
			OWLDeclarationAxiom declarationAxiom = factory.getOWLDeclarationAxiom(termClass);

			AddAxiom addAxiom = new AddAxiom(ontology, declarationAxiom);
			
			ontologyManager.applyChange(addAxiom);
		}
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

	public void addRelations(Collection<RelationCandidate> relations) {
		OWLClass domain, range;
		OWLObjectProperty property;
		OWLObjectPropertyDomainAxiom domainAxiom;
		OWLObjectPropertyRangeAxiom rangeAxiom;
		OWLObjectUnionOf domainUnionClass, rangeUnionClass;
		
		for (RelationCandidate relation : relations) {

			property = factory.getOWLObjectProperty(IRI.create(ontologyIRI + "#" + makeProperty(relation.getLabel())));
			
			if (relation.getDomain() != null) {
				domain = factory.getOWLClass(
						IRI.create(ontologyIRI + "#" + makeClass(relation.getDomain())));
				domainUnionClass = factory.getOWLObjectUnionOf(domain);
				domainAxiom = factory.getOWLObjectPropertyDomainAxiom(property, domainUnionClass);
				ontologyManager.applyChange(new AddAxiom(ontology, domainAxiom));
			}
			
			if (relation.getRange() != null) {
				range = factory.getOWLClass(
						IRI.create(ontologyIRI + "#" + makeClass(relation.getRange())));
				rangeUnionClass = factory.getOWLObjectUnionOf(range);
				rangeAxiom = factory.getOWLObjectPropertyRangeAxiom(property, rangeUnionClass);
				ontologyManager.applyChange(new AddAxiom(ontology, rangeAxiom));
			}
		}
	}
	
	private String makeClass(String term) {
		return spaceLowerToCamel(filterChars(term));
	}
	
	private String makeProperty(String label) {
		return spaceLowerToMixed(filterChars(label));
	}
	
	private String filterChars(String s) {
		// space, hyphen, letters and numbers in unicode blocks "Basic Latin" and "Latin-1 Supplement"
		String regex = "[^\\- 0-9\\x30-\\x39\\x41-\\x5A\\x61-\\x7A\\xC0-\\xD6\\xD8-\\xF6\\xF8-\\xFF]";
		return s.replaceAll(regex, "_");
	}
	
	private static String spaceLowerToMixed(String s) {
		String camelcase = spaceLowerToCamel(s); 
		return camelcase.substring(0, 1).toLowerCase() + camelcase.substring(1);
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
