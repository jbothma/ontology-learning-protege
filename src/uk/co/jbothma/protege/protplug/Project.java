package uk.co.jbothma.protege.protplug;

import gate.Corpus;
import gate.DataStore;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.corpora.SerialCorpusImpl;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.persist.PersistenceException;
import gate.persist.SerialDataStore;
import gate.security.SecurityException;
import gate.util.ExtensionFileFilter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import uk.co.jbothma.protege.protplug.candidate.RelationCandidate;
import uk.co.jbothma.protege.protplug.candidate.SubclassRelationCandidate;
import uk.co.jbothma.protege.protplug.candidate.TermCandidate;
import uk.co.jbothma.protege.protplug.extraction.CValueTerms;
import uk.co.jbothma.protege.protplug.extraction.HierarchAggClustSubclasses;
import uk.co.jbothma.protege.protplug.extraction.SubcategorisationFrames;
import uk.co.jbothma.protege.protplug.extraction.SyntacticPatternSubclasses;
import uk.co.jbothma.protege.protplug.preprocess.JAPEPreprocessing;
import uk.co.jbothma.protege.protplug.preprocess.KorpPipeline;

public class Project {
	private File projDir, dsDir;
	private String projName, installDir;
	private SerialDataStore sds;
	private SerialCorpusImpl persistCorp;
	private ArrayList<TermCandidate> termCandidates;
	private ArrayList<SubclassRelationCandidate> subclassRelCandidates;
	private ArrayList<RelationCandidate> relationCandidates;
	private String corpName;

	public Project(File projDir)
			throws PersistenceException, UnsupportedOperationException, ResourceInstantiationException,
			SecurityException, MalformedURLException 
	{
		this.installDir = "/home/jdb/thesis/workspace/ProtegePlugin";
	
		if (!projDir.exists()) {
			projDir.mkdirs();
		}
		this.projDir = projDir;
		dsDir = new File(projDir.getAbsolutePath() + "/GATESerialDatastore");
		projName = projDir.getName();
		corpName = "ProtegeOLProj_" + projName;
		
		if (dsDir.exists()) {
			sds = new SerialDataStore("file://"+dsDir);
			sds.open();
			
			FeatureMap corpFeatures = Factory.newFeatureMap();
			corpFeatures.put(DataStore.LR_ID_FEATURE_NAME, sds.getLrIds("gate.corpora.SerialCorpusImpl").get(0));
			corpFeatures.put(DataStore.DATASTORE_FEATURE_NAME, sds);
			persistCorp = (SerialCorpusImpl)Factory.createResource("gate.corpora.SerialCorpusImpl", corpFeatures);
		} else {
			sds = (SerialDataStore) Factory.createDataStore(
				"gate.persist.SerialDataStore", "file://"+dsDir.getAbsolutePath());

			Corpus corp = Factory.newCorpus(corpName);
			persistCorp = (SerialCorpusImpl) sds.adopt(corp, null);
			sds.sync(persistCorp);
		}

		termCandidates = new ArrayList<TermCandidate>();
		subclassRelCandidates = new ArrayList<SubclassRelationCandidate>();
		relationCandidates = new ArrayList<RelationCandidate>();
	}

	public void populateFromDir(String populateDir, String extensionFilter,	Boolean recurseDirectories)
			throws ResourceInstantiationException, MalformedURLException, IOException, PersistenceException {
		FileFilter filter = new ExtensionFileFilter("Files ending " + extensionFilter, extensionFilter);
		persistCorp.populate(new URL("file://" + populateDir + "/"), filter, "utf-8", recurseDirectories);
		sds.sync(persistCorp);
	}
	
	public void preprocess() throws PersistenceException, ResourceInstantiationException, IOException, SecurityException, ExecutionException {
		new KorpPipeline(projDir, this, sds, installDir, persistCorp).run();
		JAPEPreprocessing.doJAPE(persistCorp, installDir);
	}
	
	public void extractElements() {
		CValueTerms.doCValue(persistCorp, termCandidates);
		SyntacticPatternSubclasses.run(persistCorp, subclassRelCandidates);
		HierarchAggClustSubclasses.run(termCandidates, subclassRelCandidates);
		SubcategorisationFrames.run(persistCorp, relationCandidates);
	}

	public ArrayList<TermCandidate> getTermCandidates() {
		return termCandidates;
	}
	
	public ArrayList<SubclassRelationCandidate> getSubclassRelationCandidates() {
		return subclassRelCandidates;
	}

	public ArrayList<RelationCandidate> getRelationCandidates() {
		return relationCandidates;
	}
	
	public void close() throws PersistenceException {
		sds.close();
	}

	public void exportTermsToCSV(File exportToFile) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(exportToFile));
		writer.write("Term,Confidence");
		writer.newLine();
		for (TermCandidate cand : termCandidates) {
			writer.write(cand.getTerm().replaceAll(",", "") + "," + cand.getConfidence());
			writer.newLine();
		}
		writer.close();
	}

	public void emptyCorpus() throws PersistenceException, ResourceInstantiationException, SecurityException {
		persistCorp.clear();
		for (Object docID : sds.getLrIds("gate.corpora.DocumentImpl")) {
			sds.delete("gate.corpora.DocumentImpl", docID);
		}
		sds.sync(persistCorp);
	}
	
	public void printAnnotationSetsNames() {
		Iterator<Document> docIter = persistCorp.iterator();
		Document doc;
		while (docIter.hasNext()) {
			doc = docIter.next();
			
			System.out.println(doc.getName());
			for (String annotationSetName : doc.getAnnotationSetNames()) {
				System.out.println(doc.getAnnotations(annotationSetName).getAllTypes());
			}
			
			persistCorp.unloadDocument(doc, false);
			Factory.deleteResource(doc);
		}
	}
}
