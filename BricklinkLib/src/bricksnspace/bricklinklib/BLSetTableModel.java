/**
	Copyright 2016 Mario Pascucci <mpascucci@gmail.com>
	This file is part of BricklinkLib

	BricklinkLib is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	BricklinkLib is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with BricklinkLib.  If not, see <http://www.gnu.org/licenses/>.
 
 */
package bricksnspace.bricklinklib;


import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import bricksnspace.bricklinklib.BricklinkSet;

/*
 * table model to display bricklink part search results
 */
public class BLSetTableModel extends AbstractTableModel {

	private static final long serialVersionUID = -31673453457511258L;
	private ArrayList<BricklinkSet> parts;

	private String[] columnNames = {
			"Set #",
			"Description",
			"Category",
			"Year",
			};
	
	/* 
	 * sets whole data model for table
	 */
	public void setParts(ArrayList<BricklinkSet> parts) {
		this.parts = parts;
		fireTableDataChanged();
	}

	
	@Override
	public String getColumnName(int col) {
        return columnNames[col];
    }

	

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}
	

	@Override
	public int getRowCount() {
		if (parts != null)
			return parts.size();
		else
			return 0;
	}
	
	@Override
	public boolean isCellEditable(int row, int col) {
        return false;
    }
	
	public BricklinkSet getPart(int idx) {
		return parts.get(idx);
	}
	
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Class getColumnClass(int c) {
		switch (c) {
		case 3:
			return Integer.class;
		}
		return String.class;
    }


	
	@SuppressWarnings("boxing")
	@Override
	public Object getValueAt(int arg0, int arg1) {
		if (getRowCount() == 0)
			return "";
		//id,blid,name,catid
		switch (arg1) {
		case 0:
			return parts.get(arg0).setid;
		case 1:
			return parts.get(arg0).name;
		case 2:
			return parts.get(arg0).catname;
		case 3:
			return parts.get(arg0).year;
		}
		return null;
	}

}
