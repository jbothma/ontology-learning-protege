package uk.co.jbothma.protege.protplug;

import gate.Gate;

import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;

public class TutView extends AbstractOWLViewComponent {
	JPanel bobPanel;
	
	private static final long serialVersionUID = 866185983362791061L;

	@Override
	protected void disposeOWLView() {
		
	}

	@Override
	protected void initialiseOWLView() throws Exception {
		this.setLayout(new FlowLayout());
		bobPanel = new BobPanel();
		this.add(bobPanel);
		System.setProperty(
				"gate.home",
				"/home/jdb/thesis/sw_originals/gate-7.0-build4195-ALL/gate-7.0-build4195-ALL/");
		System.setProperty(
				"gate.plugins.home",
				"/home/jdb/thesis/sw_originals/gate-7.0-build4195-ALL/gate-7.0-build4195-ALL/plugins");
		Gate.setSiteConfigFile(new File("/home/jdb/thesis/sw_originals/gate-7.0-build4195-ALL/gate-7.0-build4195-ALL/gate.xml"));
		Gate.init();
	}

}
