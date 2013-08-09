package uk.co.jbothma.protege.protplug.gui;

import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.apache.commons.lang.NotImplementedException;

import uk.co.jbothma.protege.protplug.Project;
import uk.co.jbothma.protege.protplug.RelationEvent;
import uk.co.jbothma.protege.protplug.RelationEventListener;

public class RelationCandidateTableModel implements TableModel, RelationEventListener {
	private EventListenerList tableModelListenerList;
	private Project project = null;

	public RelationCandidateTableModel() {
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
			return String.class;
		else if (col == 2)
			return String.class;
		else if (col == 3)
			return Float.class;
		else
			throw new IndexOutOfBoundsException();
	}

	@Override
	public int getColumnCount() {
		return 4;
	}

	@Override
	public String getColumnName(int col) {
		if (col == 0)
			return "Domain";
		else if (col == 1)
			return "Label";
		else if (col == 2)
			return "Range";
		else if (col == 3)
			return "Importance";
		else
			throw new IndexOutOfBoundsException();
	}

	@Override
	public int getRowCount() {
		if (project == null)
			return 0;
		else
			return project.getRelationCandidates().size();
	}

	@Override
	public Object getValueAt(int rowIndex, int col) {
		if (col == 0)
			return project.getRelationCandidates().get(rowIndex).getDomain();
		else if (col == 1)
			return project.getRelationCandidates().get(rowIndex).getLabel();
		else if (col == 2)
			return project.getRelationCandidates().get(rowIndex).getRange();
		else if (col == 3) {
			float conf = project.getRelationCandidates().get(rowIndex).getConfidence();
			Float confOb = new Float(conf);
			System.out.println(Integer.toString(rowIndex) + ",  " + Float.toString(conf) + ",  " + confOb);
			return confOb;
		} else
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
	public void myEventOccurred(RelationEvent evt) {
		Object[] listeners = tableModelListenerList.getListenerList();
		for (int i = listeners.length-2; i>=0; i-=2) {
	         if (listeners[i]==TableModelListener.class) {
	        	 ((TableModelListener)listeners[i+1]).tableChanged(new TableModelEvent(this));
	         }
	     }
	}
}
