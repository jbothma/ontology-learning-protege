package uk.co.jbothma.protege.protplug;

import gate.Corpus;
import gate.CorpusController;
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
import java.util.HashSet;

import javax.swing.JFileChooser;
import javax.swing.JPanel;

public class Project {
	File projDir, korpWorkingDir = null;
	String dsDir, projName, installDir;
	SerialDataStore sds;
	SerialCorpusImpl persistCorp;

	public Project(File projDir)
			throws PersistenceException, UnsupportedOperationException, ResourceInstantiationException,
			SecurityException {
		this.projDir = projDir;
		this.installDir = this.installedDir().getAbsolutePath();
		dsDir = "file://" + projDir.getAbsolutePath() + "/GATESerialDatastore";
		projName = projDir.getName();
		
		sds = (SerialDataStore) Factory.createDataStore(
				"gate.persist.SerialDataStore", dsDir);

		Corpus corp = Factory.newCorpus("ProtegeOLProj_" + projName);
		persistCorp = (SerialCorpusImpl) sds.adopt(corp, null);
		sds.sync(persistCorp);
	}

	public void populateFromDir(String populateDir, String extensionFilter,	Boolean recurseDirectories)
			throws ResourceInstantiationException, MalformedURLException, IOException, PersistenceException {
		FileFilter filter = new ExtensionFileFilter("Files ending " + extensionFilter, extensionFilter);
		persistCorp.populate(new URL("file://" + populateDir + "/"), filter, "utf-8", recurseDirectories);
		sds.sync(persistCorp);
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
	
	public void preprocess() throws PersistenceException, ResourceInstantiationException, IOException, SecurityException, ExecutionException {
		doKorpPipeline();
		doJAPE();
	}

	private void doJAPE() throws PersistenceException, ResourceInstantiationException, IOException, ExecutionException {
		File gappFile = new File(installDir + "/resources/gate/apps/linguistic-filter/linguistic-filter.gapp");
		// load the saved application
		CorpusController application = (CorpusController)
				PersistenceManager.loadObjectFromFile(gappFile);
		application.setCorpus(persistCorp);
		application.execute();
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
	
	/**
	 * http://www.onyxbits.de/content/wherami-locating-installation-directory-your-java-application
	 */
	private File installedDir() {
		URL url = this.getClass().getProtectionDomain().getCodeSource()
				.getLocation();
		File file = null;
		try {
			file = new File(url.toURI());
		} catch (URISyntaxException e) {
			// Let's trust the JDK to get it rigth.
		}

		if (file.isDirectory()) {
			// Application consists of loose class files
			return file.getParentFile();
		} else {
			// Application is packaged in a JAR file
			return file.getParentFile().getParentFile();
		}
	}
}
