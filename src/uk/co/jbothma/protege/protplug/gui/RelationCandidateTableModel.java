package uk.co.jbothma.protege.protplug.gui;

import java.util.ArrayList;

import javax.swing.event.TableModelListener;

import org.apache.commons.lang.NotImplementedException;

import uk.co.jbothma.protege.protplug.candidate.RelationCandidate;

public class RelationCandidateTableModel implements javax.swing.table.TableModel {
	private ArrayList<RelationCandidate> relationCands;

	public RelationCandidateTableModel(ArrayList<RelationCandidate> relationCands) {
		this.relationCands = relationCands;
	}

	@Override
	public void addTableModelListener(TableModelListener arg0) {
		// throw new NotImplementedException();
	}

	@Override
	public Class<?> getColumnClass(int col) {
		if (col == 0)
			return String.class;
		else if (col == 1)
			return String.class;
		else if (col == 2)
			return String.class;
		else
			throw new IndexOutOfBoundsException();
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public String getColumnName(int col) {
		if (col == 0)
			return "Domain";
		else if (col == 1)
			return "Label";
		else if (col == 2)
			return "Range";
		else
			throw new IndexOutOfBoundsException();
	}

	@Override
	public int getRowCount() {
		return relationCands.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int col) {
		if (col == 0)
			return relationCands.get(rowIndex).getDomain();
		else if (col == 1)
			return relationCands.get(rowIndex).getLabel();
		else if (col == 2)
			return relationCands.get(rowIndex).getRange();
		else
			throw new IndexOutOfBoundsException();
	}

	@Override
	public boolean isCellEditable(int arg0, int arg1) {
		return false;
	}

	@Override
	public void removeTableModelListener(TableModelListener arg0) {
		// throw new NotImplementedException();
	}

	@Override
	public void setValueAt(Object arg0, int arg1, int arg2) {
		throw new NotImplementedException();
	}
}
