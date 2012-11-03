package uk.co.jbothma.protege.protplug.preprocess;

import gate.Corpus;
import gate.DataStore;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.corpora.SerialCorpusImpl;
import gate.creole.ResourceInstantiationException;
import gate.persist.PersistenceException;
import gate.persist.SerialDataStore;
import gate.security.SecurityException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import uk.co.jbothma.protege.protplug.Project;

public class KorpPipeline {
	private File korpWorkingDir;
	private File korpOriginalsDir;
	private SerialDataStore sds;
	private SerialCorpusImpl corp;
	private String installDir;
	private File projDir;
	private Project project;

	public KorpPipeline(
			File projDir, 
			Project project, 
			SerialDataStore sds, 
			String installDir, 
			SerialCorpusImpl corp) throws IOException {
		this.projDir = projDir;
		this.project = project;
		this.sds = sds;
		this.installDir = installDir;
		this.corp = corp;
		
		korpWorkingDir = new File(projDir.getAbsolutePath() + "/preprocess/korp/");
		korpWorkingDir.mkdirs();
		korpOriginalsDir = new File(korpWorkingDir.getAbsolutePath() + "/original");
		korpOriginalsDir.mkdirs();
		
		installKorpMakefile();
	}

	public void run() throws PersistenceException, ResourceInstantiationException, IOException, SecurityException {
		dumpXML();
		fixHyphenNewlines();
		runKorp();
		project.emptyCorpus();
		importKorpOutput();
	}

	private void fixHyphenNewlines() throws IOException {
		for (File child : korpOriginalsDir.listFiles()) {
			if (child.getName().endsWith(".xml")) {
				fixFileHyphenNewlines(child);
			}
		}
	}

	private void fixFileHyphenNewlines(File child) throws IOException {
		String file = new String(Files.readAllBytes(Paths.get(child.toURI())));
		file = file.replaceAll("\\-\n", "");
		Files.write(Paths.get(child.toURI()), file.getBytes());
	}

	private void importKorpOutput() throws ResourceInstantiationException, MalformedURLException, PersistenceException, IOException {
		String populateDir = korpWorkingDir.getAbsolutePath() + "/export";
		project.populateFromDir(populateDir, ".xml", false);
		sds.sync(corp);
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
			//if (line.contains("Exported"))
				System.out.println("korp: " + line);
		}

		reader.close();
		
		if (korpProcess.exitValue() != 0) {
			System.err.println("korpProcess.exitValue() = " + korpProcess.exitValue());
		}
		korpProcess.destroy();
	}

	private void installKorpMakefile() throws IOException {
		Path source = Paths.get(installDir + "/resources/korp/Makefile");
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
			String path = korpOriginalsDir.getAbsolutePath() + "/" + doc.getName() + ".xml";
			
			OutputStreamWriter oswriter = new OutputStreamWriter(new FileOutputStream(path), "UTF-8");
			BufferedWriter out = new BufferedWriter(oswriter);
			
			out.write(doc.toXml(null, true));
			out.close();
			
			// CLOSE DOCUMENT
			corp.unloadDocument(doc, false);
			Factory.deleteResource(doc);
		}
	}
}
