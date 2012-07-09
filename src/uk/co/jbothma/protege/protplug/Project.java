package uk.co.jbothma.protege.protplug;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.CorpusController;
import gate.DataStore;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Utils;
import gate.corpora.SerialCorpusImpl;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.persist.PersistenceException;
import gate.persist.SerialDataStore;
import gate.security.SecurityException;
import gate.util.ExtensionFileFilter;
import gate.util.persistence.PersistenceManager;

import java.awt.Component;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JPanel;

import uk.co.jbothma.terms.CValueComparator;
import uk.co.jbothma.terms.CValueSess;
import uk.co.jbothma.terms.Candidate;

public class Project {
	private File projDir, dsDir, korpWorkingDir = null;
	private String projName, installDir;
	private SerialDataStore sds;
	private SerialCorpusImpl persistCorp;
	private ArrayList<TermCandidate> termCandidates;
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
				"gate.persist.SerialDataStore", dsDir.getAbsolutePath());

			Corpus corp = Factory.newCorpus(corpName);
			persistCorp = (SerialCorpusImpl) sds.adopt(corp, null);
			sds.sync(persistCorp);
		}
		
		termCandidates = new ArrayList<TermCandidate>();
	}

	public void populateFromDir(String populateDir, String extensionFilter,	Boolean recurseDirectories)
			throws ResourceInstantiationException, MalformedURLException, IOException, PersistenceException {
		FileFilter filter = new ExtensionFileFilter("Files ending " + extensionFilter, extensionFilter);
		persistCorp.populate(new URL("file://" + populateDir + "/"), filter, "utf-8", recurseDirectories);
		sds.sync(persistCorp);
	}
	
	public void preprocess() throws PersistenceException, ResourceInstantiationException, IOException, SecurityException, ExecutionException {
		doKorpPipeline();
		doJAPE();
	}
	
	public void extractElements() {
		doCValue();
	}
	
	public ArrayList<TermCandidate> getTermCandidates() {
		return termCandidates;
	}
	
	public void close() throws PersistenceException {
		sds.close();
	}
	
	private void doCValue() {
		CValueSess cvals = new CValueSess();
		Iterator<Document> docIter = persistCorp.iterator();
		Document doc;
		while (docIter.hasNext()) {
			doc = docIter.next();

			String inputASName = "Original markups";
			String inputASType = "TermCandidate";
			
			AnnotationSet inputAS = doc.getAnnotations(inputASName);
			
			Iterator<Annotation> phrasIter = inputAS.get(inputASType).iterator();
			
			while (phrasIter.hasNext()) {
				String phrase = "";
				Annotation phrasAnnot = (Annotation) phrasIter.next();
				AnnotationSet tokAnnots = gate.Utils.getContainedAnnotations(
						inputAS, phrasAnnot, "w");
				List<Annotation> tokAnnotList = gate.Utils.inDocumentOrder(tokAnnots);
				
				for (Annotation tokAnnot : tokAnnotList) {
					String lemma = tokenLemma(doc, tokAnnot);
					// TODO: this is a corpus-specific hack and should be fixed in preprocessing or
					// made customizable for the corpus. Also, it should rather be if the string matches ^|$
					// so that strings that contain characters without whitespace are accepted.
					if (!lemma.contains("|"))
						phrase += lemma + " ";
				}
				cvals.observe(phrase.toLowerCase().trim());
			}

			persistCorp.unloadDocument(doc, false);
			Factory.deleteResource(doc);
		}
		
		cvals.calculate();
		
		ArrayList<Candidate> candList = new ArrayList<Candidate>(cvals.getCandidates());
		Collections.sort(candList, new CValueComparator());
		
		// roughly put confidence between 0 and 1.
		double minCVal = candList.get(0).getCValue();
		double maxCVal = candList.get(candList.size()-1).getCValue();
		double cValInterval = maxCVal-minCVal;
		
		for (Candidate cand : candList) {
			float conf = (float) ((cand.getCValue()+(-minCVal))/cValInterval);
			TermCandidate termCand = new TermCandidate(cand.getString(), conf);
			termCandidates.add(termCand);
		}
	}
	
	private static String tokenLemma(gate.Document doc, Annotation tokAnnot) {
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

	private void doJAPE() throws PersistenceException, ResourceInstantiationException, IOException, ExecutionException {
		File[] gapps = new File[] {
				new File(installDir + "/resources/gate/apps/linguistic-filter/linguistic-filter.gapp"),
				new File(installDir + "/resources/gate/apps/subclass_relation_candidates/subclass_relation_candidates.gapp"),
		};
		
		for (File gapp : gapps) {
			CorpusController application = (CorpusController)
					PersistenceManager.loadObjectFromFile(gapp);
			application.setCorpus(persistCorp);
			application.execute();
		}
	}

	private void emptyCorpus() throws PersistenceException, ResourceInstantiationException, SecurityException {
		persistCorp.clear();
		for (Object docID : sds.getLrIds("gate.corpora.DocumentImpl")) {
			sds.delete("gate.corpora.DocumentImpl", docID);
		}
		sds.sync(persistCorp);
	}

	private void importKorpOutput() throws ResourceInstantiationException, MalformedURLException, PersistenceException, IOException {
		String populateDir = korpWorkingDir.getAbsolutePath() + "/export";
		this.populateFromDir(populateDir, ".xml", false);
		sds.sync(persistCorp);
	}

	private void runKorp() throws IOException {
		ProcessBuilder pb = new ProcessBuilder("make", "TEXT", "export");
		pb.directory(korpWorkingDir);
		pb.redirectErrorStream(true); // redirect errorStream to inputStream
		Process korpProcess = pb.start();

		InputStreamReader tempReader = new InputStreamReader(
                new BufferedInputStream(korpProcess.getInputStream()));
        BufferedReader reader = new BufferedReader(tempReader);
		String line;
		while ((line = reader.readLine()) != null){
			if (line.contains("Exported"))
				System.out.println("korp: " + line);
		}

		reader.close();
		korpProcess.destroy();
	}

	private void installKorpMakefile() throws IOException {
		Path source = Paths.get(this.installDir + "/resources/korp/Makefile");
		Path target = Paths.get(korpWorkingDir.getAbsolutePath() + "/Makefile");
		Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
	}

	
	private void dumpXML() throws PersistenceException, ResourceInstantiationException, IOException {
		FeatureMap docFeatures;
		Document doc;
		
		
		for (Object docID : sds.getLrIds("gate.corpora.DocumentImpl")) {
			// OPEN DOCUMENT
			docFeatures = Factory.newFeatureMap();
			docFeatures.put(DataStore.LR_ID_FEATURE_NAME, docID);
			docFeatures.put(DataStore.DATASTORE_FEATURE_NAME, sds);
			doc = (gate.Document)
					Factory.createResource("gate.corpora.DocumentImpl", docFeatures);
			
			FileWriter fstream = new FileWriter(korpWorkingDir.getAbsolutePath() + "/original/" + doc.getName() + ".xml");
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(doc.toXml(null, true));
			out.close();
			
			// CLOSE DOCUMENT
			persistCorp.unloadDocument(doc, false);
			Factory.deleteResource(doc);
		}
	}
	
	private void doKorpPipeline() throws PersistenceException, ResourceInstantiationException, IOException, SecurityException {
		korpWorkingDir = new File(projDir.getAbsolutePath() + "/preprocess/korp/");
		korpWorkingDir.mkdirs();
		Files.createDirectory(Paths.get(korpWorkingDir.getAbsolutePath()+"/original"));
		dumpXML();
		installKorpMakefile();
		runKorp();
		emptyCorpus();
		importKorpOutput();
	}
	
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
}
