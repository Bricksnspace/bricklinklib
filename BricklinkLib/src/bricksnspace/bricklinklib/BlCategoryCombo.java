/*
	Copyright 2013-2016 Mario Pascucci <mpascucci@gmail.com>
	This file is part of BricklinkLib.

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

import java.sql.SQLException;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;

public class BlCategoryCombo extends JComboBox<BricklinkCategory> {


	private static final long serialVersionUID = -9204313519064558853L;
	private Vector<BricklinkCategory> catlist;
//	private JComboBox combo;
	
	// what kind of BLink object you want in combo
	public static final int BL_CAT_ALL = 0;
	public static final int BL_CAT_SET = 1;
	public static final int BL_CAT_PART = 2;
	
	public BlCategoryCombo(int type) {
		
		super();

		try {
			switch (type) {
			case 0:
				catlist = BricklinkCategory.getAll();
				break;
			case 1:
				catlist = BricklinkCategory.getSetCategories();
				break;
			case 2:
				catlist = BricklinkCategory.getPartCategories();
				break;
			}
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(this, 
					"Error retrieving BLink categories from database.\n"+e.getLocalizedMessage(), 
					"Database error", JOptionPane.ERROR_MESSAGE);
			catlist = new Vector<BricklinkCategory>();
			BricklinkCategory blc; 
			// creates empty category
			blc = new BricklinkCategory();
			catlist.add(blc);
		}
		for (BricklinkCategory bc : catlist ) {
			addItem(bc);
		}
		setMaximumRowCount(10);
		setEditable(false);
		
	}

	
	public void selectByCatId(int catid) {

		for (int i=0;i<getItemCount();i++) {
			if (((BricklinkCategory)getItemAt(i)).getCatid() == catid) {
				setSelectedIndex(i);
				break;
			}
		}

	}
	
}
