package uk.co.jbothma.protege.protplug;

import gate.Corpus;
import gate.Factory;
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

public class Project {
	File projDir = null;
	String dsDir, projName;
	SerialDataStore sds;
	Corpus corp, persistCorp;

	public Project(File projDir)
			throws PersistenceException, UnsupportedOperationException, ResourceInstantiationException,
			SecurityException {
		this.projDir = projDir;
		dsDir = "file://" + projDir.getAbsolutePath() + "/GATESerialDatastore";
		projName = projDir.getName();

		sds = (SerialDataStore) Factory.createDataStore(
				"gate.persist.SerialDataStore", dsDir);
		corp = Factory.newCorpus("ProtegeOLProj_" + projName);
		persistCorp = (Corpus) sds.adopt(corp, null);
		sds.sync(persistCorp);
	}

	public void populateFromDir(String populateDir, String extensionFilter,	Boolean recurseDirectories)
			throws ResourceInstantiationException, MalformedURLException, IOException, PersistenceException {
		FileFilter filter;
		
		filter = new ExtensionFileFilter("Files ending " + extensionFilter, extensionFilter);
		corp.populate(new URL(populateDir), filter, "utf-8", recurseDirectories);
		sds.sync(corp);
	}
}
