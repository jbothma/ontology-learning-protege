package uk.co.jbothma.protege.protplug.preprocess;

import gate.Corpus;
import gate.CorpusController;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.persist.PersistenceException;
import gate.util.persistence.PersistenceManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.osgi.framework.FrameworkUtil;

import uk.co.jbothma.protege.protplug.gui.BobPanel;

public class JAPEPreprocessing {
	public static void doJAPE(Corpus corp, String installDir) throws PersistenceException, ResourceInstantiationException, IOException, ExecutionException {
		URL[] gapps = new URL[] {
			FrameworkUtil.getBundle(BobPanel.class).getResource("/resources/gate/apps/token-string/token-string.gapp"),
			FrameworkUtil.getBundle(BobPanel.class).getResource("/resources/gate/apps/linguistic-filter/linguistic-filter.gapp"),
			FrameworkUtil.getBundle(BobPanel.class).getResource("/resources/gate/apps/subclass_relation_candidates/subclass_relation_candidates.gapp"),
		};
		
		for (URL gapp : gapps) {
			System.out.println("Loading GATE application " + gapp);
			CorpusController application = (CorpusController)
					PersistenceManager.loadObjectFromUrl(gapp);
			application.setCorpus(corp);
			application.execute();
			application.cleanup();
		}
	}
}
