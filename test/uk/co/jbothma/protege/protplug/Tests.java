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

import org.junit.Before;
import org.junit.Test;

public class Tests {
	Project project;
	
	private File makeProjDir() throws IOException {
		File dir = File.createTempFile(this.toString(),"");
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
		File projDir = makeProjDir();
		project = new Project(projDir);
		project.populateFromDir("/home/jdb/protplug/1", "pdf", false);
	}
	
	@Test
	public void testPreprocess() throws PersistenceException, ResourceInstantiationException, SecurityException, IOException, ExecutionException {
		project.preprocess();
	}

}
