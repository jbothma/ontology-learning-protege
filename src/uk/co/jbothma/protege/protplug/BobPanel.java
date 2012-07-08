package uk.co.jbothma.protege.protplug;

import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.persist.PersistenceException;
import gate.security.SecurityException;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JButton;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class BobPanel extends JPanel {
	private static final long serialVersionUID = -7832128279921728175L;
	Project project = null;

	/**
	 * Create the panel.
	 */
	public BobPanel() {
		//final JPanel panel = this;
		
		JButton btnNewOntologyLearning = new JButton("New ontology learning project");
		btnNewOntologyLearning.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				newProject();
			}
		});
		add(btnNewOntologyLearning);
		
		JButton btnPopulateFromDirectory = new JButton("Populate from directory");
		btnPopulateFromDirectory.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				populateFromDir();
			}
		});
		add(btnPopulateFromDirectory);
		
		JButton btnPreprocess = new JButton("Preprocess");
		btnPreprocess.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					project.preprocess();
				} catch (PersistenceException | ResourceInstantiationException
						| IOException | SecurityException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		add(btnPreprocess);
	}
	
	private void newProject() {
		File projDir;
		// Select directory code from http://www.rgagnon.com/javadetails/java-0370.html
		JFileChooser chooser;
		String choosertitle = "Choose an empty directory for this project.";
		
		chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File("."));
		chooser.setDialogTitle(choosertitle);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		// disable the "All files" option.
		chooser.setAcceptAllFileFilterUsed(false);
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			projDir = chooser.getSelectedFile();
			try {
				project = new Project(projDir);
			} catch (PersistenceException | UnsupportedOperationException
					| ResourceInstantiationException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			
		}		
	}
	
	private void populateFromDir() {
		PopulateFromDirDialog dialog = new PopulateFromDirDialog();
		dialog.showDialog();
		if (dialog.getOk()) {
			String directory = dialog.getDirectory();
			String extension = dialog.getExtension();
			Boolean recurse = dialog.getRecurse();
			try {
				project.populateFromDir(directory, extension, recurse);
			} catch (ResourceInstantiationException | PersistenceException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
