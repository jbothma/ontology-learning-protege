package uk.co.jbothma.protege.protplug;

import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.persist.PersistenceException;
import gate.security.SecurityException;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.SwingWorker;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextPane;

import uk.co.jbothma.protege.protplug.Project.TermCandidate;

public class BobPanel extends JPanel {
	private static final long serialVersionUID = -7832128279921728175L;
	Project project = null;
	JButton btnPreprocess, btnNewOntologyLearning, btnPopulateFromDirectory, btnExtractCandidates;
	private JTextPane textPane;

	/**
	 * Create the panel.
	 */
	public BobPanel() {
		btnNewOntologyLearning = new JButton("New ontology learning project");
		btnNewOntologyLearning.setEnabled(false);
		btnNewOntologyLearning.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				newProject();
			}
		});
		add(btnNewOntologyLearning);
		
		btnPopulateFromDirectory = new JButton("Populate from directory");
		btnPopulateFromDirectory.setEnabled(false);
		btnPopulateFromDirectory.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				populateFromDir();
			}
		});
		add(btnPopulateFromDirectory);
		
		btnPreprocess = new JButton("Preprocess");
		btnPreprocess.setEnabled(false);
		btnPreprocess.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				SwingWorker worker = new SwingWorker<Void, Void>() {
				    @Override
				    public Void doInBackground() {
				    	try {
							project.preprocess();
						} catch (PersistenceException | ResourceInstantiationException
								| IOException | SecurityException | ExecutionException e) {
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
				    	setButtonsEnabled(true);
				        textPane.setText(textPane.getText()+"\npreprocessing finished.");
				    }
				};
		        textPane.setText(textPane.getText()+"\npreprocessing started.");
				setButtonsEnabled(false);
				worker.execute();
				
			}
		});
		add(btnPreprocess);
		
		btnExtractCandidates = new JButton("Extract candidates");
		btnExtractCandidates.setEnabled(false);
		btnExtractCandidates.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				SwingWorker worker = new SwingWorker<Void, Void>() {
				    @Override
				    public Void doInBackground() {				    	
						project.extractElements();
						System.out.println("term cands: "+project.getTermCandidates().size());
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
				    	setButtonsEnabled(true);
				        textPane.setText(textPane.getText()+"\nextraction finished.");
				        textPane.setText(textPane.getText()+"\n"+project.getTermCandidates().size()+" terms");
				        String candStr = "";
				        for (TermCandidate cand : project.getTermCandidates()) {
				        	candStr += cand + "\n";
				        }
				        textPane.setText(textPane.getText()+"\n"+candStr);
				    }
				};
				setButtonsEnabled(false);
		        textPane.setText(textPane.getText()+"\nextraction started.");
				worker.execute();
			}
		});
		add(btnExtractCandidates);
		
		textPane = new JTextPane();
		textPane.setEditable(false);
		add(textPane);
	}
	
	public void initialized() {
		setButtonsEnabled(true);
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
		        textPane.setText(textPane.getText()+"Created project in " + projDir);
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
			final String directory = dialog.getDirectory();
			final String extension = dialog.getExtension();
			final Boolean recurse = dialog.getRecurse();
			
			SwingWorker worker = new SwingWorker<Void, Void>() {
			    @Override
			    public Void doInBackground() {
			    	try {
						project.populateFromDir(directory, extension, recurse);
					} catch (ResourceInstantiationException | PersistenceException | IOException e) {
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
			    	setButtonsEnabled(true);
			        textPane.setText(textPane.getText()+"\n"+"populated from "+directory);
			    }
			};
			setButtonsEnabled(false);
			worker.execute();
		}
	}
	
	private void setButtonsEnabled(Boolean value) {
		JButton[] buttons = new JButton[] {
			btnPreprocess, 
			btnNewOntologyLearning, 
			btnPopulateFromDirectory,
			btnExtractCandidates,
		};
		for (JButton button : buttons) {
			button.setEnabled(value);
		}
	}
}
