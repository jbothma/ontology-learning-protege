package uk.co.jbothma.protege.protplug.preprocess;

import gate.Corpus;
import gate.CorpusController;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.persist.PersistenceException;
import gate.util.persistence.PersistenceManager;

import java.io.File;
import java.io.IOException;

public class JAPEPreprocessing {
	public static void doJAPE(Corpus corp, String installDir) throws PersistenceException, ResourceInstantiationException, IOException, ExecutionException {
		String[] gapps = new String[] {
			installDir + "/resources/gate/apps/token-string/token-string.gapp",
			installDir + "/resources/gate/apps/linguistic-filter/linguistic-filter.gapp",
			installDir + "/resources/gate/apps/subclass_relation_candidates/subclass_relation_candidates.gapp",
		};
		
		for (String gapp : gapps) {
			CorpusController application = (CorpusController)
					PersistenceManager.loadObjectFromFile(new File(gapp));
			application.setCorpus(corp);
			application.execute();
		}
	}
}
