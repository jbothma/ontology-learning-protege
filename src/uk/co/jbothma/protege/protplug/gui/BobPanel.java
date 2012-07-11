package uk.co.jbothma.protege.protplug.gui;

import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.persist.PersistenceException;
import gate.security.SecurityException;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;

import uk.co.jbothma.protege.protplug.Project;
import uk.co.jbothma.protege.protplug.RelationEventListener;
import uk.co.jbothma.protege.protplug.SubclassEventListener;
import uk.co.jbothma.protege.protplug.TermEventListener;
import uk.co.jbothma.protege.protplug.gui.RelationCandidateTableModel;
import uk.co.jbothma.protege.protplug.gui.SubclassRelationCandidateTableModel;
import uk.co.jbothma.protege.protplug.gui.TermCandidateTableModel;
import javax.swing.JTabbedPane;

public class BobPanel extends JPanel {
	private static final long serialVersionUID = -7832128279921728175L;
	private Project project = null;
	private JButton btnPreprocess, btnNewOntologyLearning, btnPopulateFromDirectory, btnExtractCandidates;
	private JTextPane textPane;
	private JTable termCandTable, subclassCandTable, relationCandTable;
	private JButton btnExportTerms;
	private JTabbedPane candidateTabbedPane;

	/**
	 * Create the panel.
	 */
	public BobPanel() {
		btnNewOntologyLearning = new JButton("New ontology learning project");
		btnNewOntologyLearning.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				newProject();
			}
		});
		setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		add(btnNewOntologyLearning);
		
		btnPopulateFromDirectory = new JButton("Populate from directory");
		btnPopulateFromDirectory.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				populateFromDir();
			}
		});
		add(btnPopulateFromDirectory);
		
		btnPreprocess = new JButton("Preprocess");
		btnPreprocess.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
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
		btnExtractCandidates.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

					@Override
				    public Void doInBackground() {				    	
						project.extractElements();
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
				    }
				};
				setButtonsEnabled(false);
		        textPane.setText(textPane.getText()+"\nextraction started.");
				worker.execute();
			}
		});
		add(btnExtractCandidates);
		
		btnExportTerms = new JButton("Export terms");
		btnExportTerms.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser chooser = new JFileChooser();
				chooser.setCurrentDirectory(new java.io.File("."));
				chooser.setDialogTitle("Export terms to file");
				FileFilter filter = new FileFilter() {
				    public boolean accept(File file) {
				        String filename = file.getName();
				        return filename.endsWith(".csv");
				    }
				    public String getDescription() {
				        return "*.csv";
				    }
				};
				chooser.addChoosableFileFilter(filter);
				chooser.setAcceptAllFileFilterUsed(false);
				if (chooser.showSaveDialog(BobPanel.this) == JFileChooser.APPROVE_OPTION) {
					final File exportToFile = chooser.getSelectedFile();
					SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
					    @Override
					    public Void doInBackground() {
							try {
								project.exportTermsToCSV(exportToFile);
							} catch (IOException e) {
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
					    }
					};
					setButtonsEnabled(false);
					worker.execute();
				}
			}
		});
		add(btnExportTerms);
		
		textPane = new JTextPane();
		textPane.setEditable(false);
		add(textPane);

		termCandTable = new JTable(new TermCandidateTableModel());
        subclassCandTable = new JTable(new SubclassRelationCandidateTableModel());        
        relationCandTable = new JTable(new RelationCandidateTableModel());
		
		candidateTabbedPane = new JTabbedPane(JTabbedPane.TOP);
		candidateTabbedPane.addTab("Term", new JScrollPane(termCandTable));
		candidateTabbedPane.addTab("Subclass", new JScrollPane(subclassCandTable));
		candidateTabbedPane.addTab("Relation", new JScrollPane(relationCandTable));
		add(candidateTabbedPane);
				
		setButtonsEnabled(false);
	}
	
	public void initialized() {
		setButtonsEnabled(true);
	}
	
	private void newProject() {
		final File projDir;
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

			SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			    @Override
			    public Void doInBackground() {
					try {
						project = new Project(projDir);
				        textPane.setText(textPane.getText()+"Created project in " + projDir);
					} catch (PersistenceException | UnsupportedOperationException
							| ResourceInstantiationException | SecurityException | MalformedURLException e) {
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
			    	((SubclassRelationCandidateTableModel) subclassCandTable.getModel()).setProject(project);
			    	((RelationCandidateTableModel) relationCandTable.getModel()).setProject(project);
			    	((TermCandidateTableModel) termCandTable.getModel()).setProject(project);
			    	project.addSubclassListener((SubclassEventListener) subclassCandTable.getModel());
			    	project.addRelationListener((RelationEventListener) relationCandTable.getModel());
			    	project.addTermListener((TermEventListener) termCandTable.getModel());
			    	setButtonsEnabled(true);
			    }
			};
			setButtonsEnabled(false);
			worker.execute();
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
			
			SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
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
			btnExportTerms,
		};
		for (JButton button : buttons) {
			button.setEnabled(value);
		}
	}

	public void cleanup() {
		try {
			if (project != null)
				project.close();
		} catch (PersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
