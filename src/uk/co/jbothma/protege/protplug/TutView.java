package uk.co.jbothma.protege.protplug;

import gate.Gate;
import gate.util.GateException;

import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.io.File;
import java.net.MalformedURLException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;

public class TutView extends AbstractOWLViewComponent {
	BobPanel bobPanel;
	
	private static final long serialVersionUID = 866185983362791061L;

	@Override
	protected void disposeOWLView() {
		
	}

	@Override
	protected void initialiseOWLView() throws Exception {
		this.setLayout(new FlowLayout());
		bobPanel = new BobPanel();
		this.add(bobPanel);
		
		SwingWorker worker = new SwingWorker<Void, Void>() {
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
