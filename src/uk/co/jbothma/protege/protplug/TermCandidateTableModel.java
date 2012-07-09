package uk.co.jbothma.protege.protplug;

import java.util.ArrayList;

import javax.swing.event.TableModelListener;

import org.apache.commons.lang.NotImplementedException;

import uk.co.jbothma.protege.protplug.Project.TermCandidate;

public class TermCandidateTableModel implements javax.swing.table.TableModel {
	private ArrayList<TermCandidate> termCands;

	public TermCandidateTableModel(ArrayList<TermCandidate> termCands) {
		this.termCands = termCands;
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
		return termCands.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int col) {
		if (col == 0)
			return termCands.get(rowIndex).getTerm();
		else if (col == 1)
			return termCands.get(rowIndex).getConfidence();
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
