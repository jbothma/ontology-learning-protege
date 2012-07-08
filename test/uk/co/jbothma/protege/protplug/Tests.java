package uk.co.jbothma.protege.protplug;

import static org.junit.Assert.*;

import gate.Gate;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.persist.PersistenceException;
import gate.security.SecurityException;
import gate.util.GateException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

public class Tests {
	Project project;
	File projDir = null;
	
	private File makeProjDir() throws IOException {
		File dir = File.createTempFile(
				"ProtegeOLTests",
				"_" + new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss").format(new Date()));
		assertTrue(dir.delete());
		assertTrue(dir.mkdirs());
		return dir;
	}
	
	@Before
	public void setup() throws IOException, UnsupportedOperationException, GateException {
		System.setProperty(
				"gate.home",
				"/home/jdb/thesis/sw_originals/gate-7.0-build4195-ALL/gate-7.0-build4195-ALL/");
		System.setProperty(
				"gate.plugins.home",
				"/home/jdb/thesis/sw_originals/gate-7.0-build4195-ALL/gate-7.0-build4195-ALL/plugins");
		Gate.setSiteConfigFile(new File("/home/jdb/thesis/sw_originals/gate-7.0-build4195-ALL/gate-7.0-build4195-ALL/gate.xml"));
		Gate.init();
		Gate.getCreoleRegister().registerDirectories(
				new File("/home/jdb/bin/gate-7.0-build4195-ALL/plugins/ANNIE").toURI().toURL());
		projDir = makeProjDir();
	}
	
	@Test
	public void testTypicalFlow() throws PersistenceException, ResourceInstantiationException, SecurityException, IOException, ExecutionException {
		project = new Project(projDir);
		project.populateFromDir("/home/jdb/protplug/1", "pdf", false);
		project.preprocess();
		project.extractElements();
		System.out.println(project.getTermCandidates());
	}

}
