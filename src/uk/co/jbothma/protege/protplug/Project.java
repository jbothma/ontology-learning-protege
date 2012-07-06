package uk.co.jbothma.protege.protplug;

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
import gate.util.ExtensionFileFilter;

import java.awt.Component;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JFileChooser;
import javax.swing.JPanel;

public class Project {
	File projDir = null;
	String dsDir, projName;
	SerialDataStore sds;
	SerialCorpusImpl persistCorp;

	public Project(File projDir)
			throws PersistenceException, UnsupportedOperationException, ResourceInstantiationException,
			SecurityException {
		this.projDir = projDir;
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
		System.out.println(populateDir + " " + extensionFilter + " " + recurseDirectories);
		persistCorp.populate(new URL("file://" + populateDir + "/"), filter, "utf-8", recurseDirectories);
		sds.sync(persistCorp);
		dumpXML();
	}
	
	public void dumpXML() throws PersistenceException, ResourceInstantiationException {
		FeatureMap docFeatures;
		Document doc;
		for (Object docID : sds.getLrIds("gate.corpora.DocumentImpl")) {
			// OPEN DOCUMENT
			docFeatures = Factory.newFeatureMap();
			docFeatures.put(DataStore.LR_ID_FEATURE_NAME, docID);
			docFeatures.put(DataStore.DATASTORE_FEATURE_NAME, sds);
			doc = (gate.Document)
					Factory.createResource("gate.corpora.DocumentImpl", docFeatures);
			
			System.out.println(doc.toXml(null, true));
			
			// CLOSE DOCUMENT
			persistCorp.unloadDocument(doc, false);
			Factory.deleteResource(doc);
		}
	}
}
