package uk.co.jbothma.protege.protplug.gui;

import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.apache.commons.lang.NotImplementedException;

import uk.co.jbothma.protege.protplug.Project;
import uk.co.jbothma.protege.protplug.TermEvent;
import uk.co.jbothma.protege.protplug.TermEventListener;

public class TermCandidateTableModel implements TableModel, TermEventListener {
	private Project project = null;
	private EventListenerList tableModelListenerList;

	public TermCandidateTableModel() {
		tableModelListenerList = new EventListenerList();
	}
	
	public void setProject(Project project) {
		this.project = project;
	}

	@Override
	public void addTableModelListener(TableModelListener listener) {
		tableModelListenerList.add(TableModelListener.class, listener);
	}

	@Override
	public void removeTableModelListener(TableModelListener listener) {
		tableModelListenerList.remove(TableModelListener.class, listener);
	}

	@Override
	public Class<?> getColumnClass(int col) {
		if (col == 0)
			return String.class;
		else if (col == 1)
			return Float.class;
		else
			throw new IndexOutOfBoundsException();
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public String getColumnName(int col) {
		if (col == 0)
			return "Term";
		else if (col == 1)
			return "Confidence";
		else
			throw new IndexOutOfBoundsException();
	}

	@Override
	public int getRowCount() {
		if (project == null)
			return 0;
		else
			return project.getTermCandidates().size();
	}

	@Override
	public Object getValueAt(int rowIndex, int col) {
		if (col == 0)
			return project.getTermCandidates().get(rowIndex).getTerm();
		else if (col == 1)
			return project.getTermCandidates().get(rowIndex).getConfidence();
		else
			throw new IndexOutOfBoundsException();
	}

	@Override
	public boolean isCellEditable(int arg0, int arg1) {
		return false;
	}

	@Override
	public void setValueAt(Object arg0, int arg1, int arg2) {
		throw new NotImplementedException();
	}

	@Override
	public void myEventOccurred(TermEvent evt) {
		Object[] listeners = tableModelListenerList.getListenerList();
		for (int i = listeners.length-2; i>=0; i-=2) {
	         if (listeners[i]==TableModelListener.class) {
	        	 ((TableModelListener)listeners[i+1]).tableChanged(new TableModelEvent(this));
	         }
	     }
	}

}
