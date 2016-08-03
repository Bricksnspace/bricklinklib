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

import java.sql.SQLException;
import java.sql.Statement;

import bricksnspace.dbconnector.DBConnector;

/**
 * @author mario
 *
 */
public class BricklinkLib {
	
	protected static DBConnector db;
	private static final String DBVAR = "MPBLVERSION";
	private static final int DBVERSION = 1;
	
	
	private BricklinkLib() {
		// private unreachable constructor
	}
	
	
	private static void upgradeFromMinus1() throws SQLException {
		
		Statement st;
		
		st = db.createStatement();
		st.execute("ALTER TABLE "+BricklinkSet.table+" ADD COLUMN (weight REAL, dimx REAL, dimy REAL, dimz REAL)");
		st.execute("ALTER TABLE "+BricklinkColor.table+" ADD COLUMN (inpart INT, inset INT, wanted INT, sell INT)");
		db.setDbVersion(DBVAR, DBVERSION);
	}
	
	
	public static void Init(DBConnector dbc) throws SQLException {
		
		if (dbc == null)
			throw new IllegalArgumentException("[BricklinkLib.Init] undefined DBConnector");
		db = dbc;
		// checks for database upgrade
		if (db.needsUpgrade(DBVAR, DBVERSION)) {
			switch (db.getDbVersion(DBVAR)) {
			case -1:
				upgradeFromMinus1();
				break;
			}
		}
		BricklinkPart.init();
		BricklinkSet.init();
		BricklinkColor.init();
		BricklinkCategory.init();
	}

}
