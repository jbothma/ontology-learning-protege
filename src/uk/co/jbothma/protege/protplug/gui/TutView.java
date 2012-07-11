package uk.co.jbothma.protege.protplug.gui;

import gate.Gate;
import gate.util.GateException;

import java.awt.BorderLayout;
import java.io.File;
import java.net.MalformedURLException;

import javax.swing.SwingWorker;

import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;

public class TutView extends AbstractOWLViewComponent {
	public TutView() {
	}
	BobPanel bobPanel;
	
	private static final long serialVersionUID = 866185983362791061L;

	@Override
	protected void disposeOWLView() {
		bobPanel.cleanup();
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
		    public Void doInBackground() {				    	
		    	System.setProperty(
						"gate.home",
						"/home/jdb/thesis/sw_originals/gate-7.0-build4195-ALL/gate-7.0-build4195-ALL/");
				System.setProperty(
						"gate.plugins.home",
						"/home/jdb/thesis/sw_originals/gate-7.0-build4195-ALL/gate-7.0-build4195-ALL/plugins");
				Gate.setSiteConfigFile(new File("/home/jdb/thesis/sw_originals/gate-7.0-build4195-ALL/gate-7.0-build4195-ALL/gate.xml"));
				try {
					if (!Gate.isInitialised())
						Gate.init();
					Gate.getCreoleRegister().registerDirectories(
							new File("/home/jdb/bin/gate-7.0-build4195-ALL/plugins/ANNIE").toURI().toURL());
				} catch (GateException | MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				return null;
		    }

		    @Override
		    public void done() {
		    	try {
		            get();
		        } catch (InterruptedException ignore) {}
		        catch (java.util.concurrent.ExecutionException e) {
		        	e.printStackTrace();
		        }
		    	bobPanel.initialized();
		    }
		};
		worker.execute();		
	}

}
