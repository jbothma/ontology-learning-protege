package uk.co.jbothma.protege.protplug.gui;

import java.util.ArrayList;

import javax.swing.event.TableModelListener;

import org.apache.commons.lang.NotImplementedException;

import uk.co.jbothma.protege.protplug.candidate.SubclassRelationCandidate;

public class SubclassRelationCandidateTableModel implements javax.swing.table.TableModel {
	private ArrayList<SubclassRelationCandidate> subclassCands;

	public SubclassRelationCandidateTableModel(ArrayList<SubclassRelationCandidate> subclassCands) {
		this.subclassCands = subclassCands;
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
			return "Domain";
		else if (col == 1)
			return "Range";
		else
			throw new IndexOutOfBoundsException();
	}

	@Override
	public int getRowCount() {
		return subclassCands.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int col) {
		if (col == 0)
			return subclassCands.get(rowIndex).getDomain();
		else if (col == 1)
			return subclassCands.get(rowIndex).getRange();
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
