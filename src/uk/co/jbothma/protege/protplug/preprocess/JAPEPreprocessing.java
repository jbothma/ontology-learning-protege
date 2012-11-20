package uk.co.jbothma.protege.protplug.preprocess;

import gate.Corpus;
import gate.CorpusController;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.persist.PersistenceException;
import gate.util.persistence.PersistenceManager;

import java.io.IOException;
import java.net.URL;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class JAPEPreprocessing {
	public static void doJAPE(Corpus corp) throws PersistenceException, ResourceInstantiationException, IOException, ExecutionException {
		Bundle bundle = FrameworkUtil.getBundle(JAPEPreprocessing.class);
		URL[] gapps = new URL[] {
			bundle.getResource("/resources/gate/apps/token-string/token-string.gapp"),
			bundle.getResource("/resources/gate/apps/linguistic-filter/linguistic-filter.gapp"),
			bundle.getResource("/resources/gate/apps/subclass_relation_candidates/subclass_relation_candidates.gapp"),
		};
		
		for (URL gapp : gapps) {
			CorpusController application = (CorpusController)
					PersistenceManager.loadObjectFromUrl(gapp);
			application.setCorpus(corp);
			application.execute();
			application.cleanup();
		}
	}
}
