package uk.co.jbothma.protege.protplug.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class PopulateFromDirDialog extends JDialog {
	private static final long serialVersionUID = -7977902798588120848L;
	private Boolean ok = false;
	private final JPanel contentPanel = new JPanel();
	private JTextField txtDir;
	private JTextField txtExtension;
	private JCheckBox chkRecurse;
	

	/**
	 * Create the dialog.
	 */
	public PopulateFromDirDialog() {
		setTitle("Populate corpus from directory");
		setBounds(100, 100, 457, 179);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		{
			JLabel lblDirectory = new JLabel("Directory");
			contentPanel.add(lblDirectory, "2, 2, right, default");
		}
		{
			txtDir = new JTextField();
			contentPanel.add(txtDir, "4, 2, fill, default");
			txtDir.setColumns(10);
		}
		{
			JButton btnSelectDir = new JButton("Select directory");
			btnSelectDir.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
				}
			});
			btnSelectDir.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent arg0) {
					// Select directory code from http://www.rgagnon.com/javadetails/java-0370.html
					JFileChooser chooser;
					String choosertitle = "Select a directory from which to import documents";
					
					chooser = new JFileChooser();
					chooser.setCurrentDirectory(new java.io.File("."));
					chooser.setDialogTitle(choosertitle);
					chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					// disable the "All files" option.
					chooser.setAcceptAllFileFilterUsed(false);
					if (chooser.showOpenDialog(PopulateFromDirDialog.this) == JFileChooser.APPROVE_OPTION) {
						txtDir.setText(chooser.getSelectedFile().getAbsolutePath());						
					} else {
						
					}
				}
			});
			contentPanel.add(btnSelectDir, "6, 2");
		}
		{
			JLabel lblRecurseDirectories = new JLabel("Recurse directories");
			contentPanel.add(lblRecurseDirectories, "2, 4");
		}
		{
			chkRecurse = new JCheckBox("");
			contentPanel.add(chkRecurse, "4, 4");
		}
		{
			JLabel lblFileExtension = new JLabel("File extension");
			contentPanel.add(lblFileExtension, "2, 6, right, default");
		}
		{
			txtExtension = new JTextField();
			contentPanel.add(txtExtension, "4, 6, fill, default");
			txtExtension.setColumns(10);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						ok = true;
						setVisible(false);
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						setVisible(false);
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

	public String getExtension() {
		return txtExtension.getText();
	}
	
	public Boolean getRecurse() {
		return chkRecurse.isSelected();
	}
	
	public String getDirectory() {
		return txtDir.getText();
	}
	
	public Boolean getOk() {
		return ok;
	}
	
	public void showDialog() {
		this.setModal(true);
		this.setVisible(true);
	}
}
