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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import uk.co.jbothma.protege.protplug.Project.RelationCandidate;
import uk.co.jbothma.taxonomy.AggHierarchClust;
import uk.co.jbothma.taxonomy.Cluster;
import uk.co.jbothma.taxonomy.Term;
import uk.co.jbothma.terms.CValueComparator;
import uk.co.jbothma.terms.CValueSess;
import uk.co.jbothma.terms.Candidate;

public class Project {
	private File projDir, dsDir, korpWorkingDir = null;
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
		doKorpPipeline();
		doJAPE();
	}
	
	public void extractElements() {
		doCValue();
		doSubclassAnnCands();
		doSubclassClustering();
		doSubcategorisationFrames();
	}
	
	private void doSubcategorisationFrames() {
		Iterator<Document> docIter = persistCorp.iterator();
		Document doc;
		while (docIter.hasNext()) {
			doc = docIter.next();
			
			String inputASName = "Original markups";
			AnnotationSet inputAS = doc.getAnnotations(inputASName);

			String sentASType = "sentence";
			Iterator<Annotation> sentIter = inputAS.get(sentASType).iterator();
			
			AnnotationSet termAS = inputAS.get("TermCandidate");
			
			while (sentIter.hasNext()) {
				Annotation sentAnnot = (Annotation) sentIter.next();
				AnnotationSet wordAS = inputAS.get("w");
				AnnotationSet containedWordsAS = Utils.getContainedAnnotations(wordAS, sentAnnot);
				List<Annotation> wordAnnList = Utils.inDocumentOrder(containedWordsAS);
				Annotation rootAnn = null;
				for (Annotation ann : wordAnnList) {
					FeatureMap features = ann.getFeatures();
					if (features.get("deprel").equals("ROOT") && features.get("dephead").equals("")) {
						rootAnn = ann;
					}
					if (!features.get("dephead").equals("")){
						try {
							int depHeadRef = Integer.parseInt((String)features.get("dephead"));
							String depRel = (String)features.get("deprel");
							Annotation depHead = wordAnnList.get(depHeadRef-1);
							addBranch(depHead, ann);
						} catch(NumberFormatException e) {}
					}
				}
				if (rootAnn != null) {
					if (rootAnn.getFeatures().get("pos").equals("VB")) {
						Set<Annotation> branches = (Set<Annotation>)rootAnn.getFeatures().get("branches");
						String subjStr = null;
						String objStr = null;
						String vgStr = Utils.stringFor(doc, rootAnn);
						if (branches != null) {
							for (Annotation branch : branches) {
								String depRel = (String)branch.getFeatures().get("deprel");
								if (depRel.equals("SS")) {
									subjStr = getContainingTermString(doc, termAS, branch);
								}
								if (depRel.equals("OO")) {
									objStr = getContainingTermString(doc, termAS, branch);
								}
								if (depRel.equals("VG")) {
									vgStr += " " + Utils.stringFor(doc, branch);
								}
							}
						}
						if (subjStr != null || objStr != null) {
							System.out.println("REL: ("+subjStr+")  " + vgStr + "  ("+objStr+")");
							relationCandidates.add(new RelationCandidate(vgStr, subjStr, objStr));
						}
					}
				}
			}
			
			persistCorp.unloadDocument(doc, false);
			Factory.deleteResource(doc);
		}
	}
	
	private static void addBranch(Annotation trunk, Annotation branch) {
		Set<Annotation> branches = (Set<Annotation>) trunk.getFeatures().get("branches");
		if (branches == null) {
			branches = new HashSet<Annotation>();
			trunk.getFeatures().put("branches", branches);
		}
		branches.add(branch);
	}

	public static String getContainingTermString(Document doc, AnnotationSet termAS, Annotation branch) {
		AnnotationSet containingTermAS = Utils.getContainedAnnotations(termAS, branch);
		if (containingTermAS.size() > 0) {
			return Utils.stringFor(doc, containingTermAS);
		}
		return null;
	}

	private void doSubclassClustering() {
		Set<String[]> terms = new HashSet<String[]>();
		int startIdx = 0;
		int endIdx = termCandidates.size()-1;
		if (endIdx>500) {
			startIdx = endIdx-500;
		}
		// select 500 terms with highest confidence
		for (TermCandidate term : termCandidates.subList(startIdx, endIdx)) {
			terms.add(term.getTerm().split(" "));
		}
		
		AggHierarchClust clustering = new AggHierarchClust(terms.toArray(new String[][]{}));
		clustering.cluster();
		Set<Cluster> clusters = clustering.getClusters();
		Set<Term> clusterTerms;
		SubclassRelationCandidate cand;
		for (Cluster cluster : clusters) {
			if (cluster.getTerms().size() > 1) {
				clusterTerms = cluster.getTerms();
				for (Term term : clusterTerms) {
					String domain = StringUtils.join(term.getParts(), " ");
					cand = new SubclassRelationCandidate(domain, term.getHead());
					subclassRelCandidates.add(cand);
				}
			}
		}
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

	private void doSubclassAnnCands() {
		Iterator<Document> docIter = persistCorp.iterator();
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
			
			persistCorp.unloadDocument(doc, false);
			Factory.deleteResource(doc);
		}
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
		String[] gapps = new String[] {
			installDir + "/resources/gate/apps/token-string/token-string.gapp",
			installDir + "/resources/gate/apps/linguistic-filter/linguistic-filter.gapp",
			installDir + "/resources/gate/apps/subclass_relation_candidates/subclass_relation_candidates.gapp",
		};
		
		for (String gapp : gapps) {
			CorpusController application = (CorpusController)
					PersistenceManager.loadObjectFromFile(new File(gapp));
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
}
