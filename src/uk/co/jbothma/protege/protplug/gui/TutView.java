package uk.co.jbothma.protege.protplug.gui;

import gate.Gate;
import gate.creole.ANNIEConstants;
import gate.persist.PersistenceException;
import gate.util.GateException;

import java.awt.BorderLayout;
import java.io.File;
import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import org.protege.editor.core.ProtegeApplication;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;

public class TutView extends AbstractOWLViewComponent {
	public TutView() {
	}
	BobPanel bobPanel;
	
	private static final long serialVersionUID = 866185983362791061L;

	@Override
	protected void disposeOWLView() {
		try {
			bobPanel.cleanup();
		} catch (PersistenceException e) {
			ProtegeApplication.getErrorLog().logError(e);
		}
	}

	@Override
	protected void initialiseOWLView() throws Exception {
		this.setLayout(new BorderLayout());
		bobPanel = new BobPanel(this.getOWLModelManager());
        add(bobPanel);
        bobPanel.setMaximumSize(this.getSize());
		
        // initialise GATE in the background while drawing the GUI
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
		    @Override
		    public Void doInBackground() throws Exception {
		    	String gateHome = System.getenv("GATE_HOME");
		    	System.setProperty("gate.home", gateHome);
				if (!Gate.isInitialised())
					Gate.init();
				Gate.getCreoleRegister().registerDirectories(new File( 
						Gate.getPluginsHome(), ANNIEConstants.PLUGIN_DIR).toURI().toURL());
				
				return null;
		    }

		    @Override
		    public void done() {
		            try {
						get();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						ProtegeApplication.getErrorLog().logError(e);
					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						ProtegeApplication.getErrorLog().logError(e);
					}
		    	bobPanel.initialized();
		    }
		};
		worker.execute();		
	}

}
