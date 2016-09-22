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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;




/**
 * Bricklink categories
 * 
 * @author Mario Pascucci
 *
 */
public class BricklinkCategory {

	protected int id;
	protected int catid;
	protected String name;
	public static final String table = "blcategories";
	private static PreparedStatement insertPS = null;
	
	
	
	/**
	 * Init database functions
	 * @param bdb database connector
	 * @throws SQLException
	 */
	public static void init() throws SQLException {

		insertPS = BricklinkLib.db.prepareStatement("INSERT INTO "+table+" (catid,name) VALUES (?,?)");
	}
	
	

	public BricklinkCategory() {
		id = 0;
		catid = 0;
		name = "no category";
	}
	
	


	/**
	 * @return the catid
	 */
	public int getCatid() {
		return catid;
	}



	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}



	@Override
	public String toString() {
		return name + " (" + catid + ")";
	}

	

	/**
	 * Make a copy from category object
	 * @param blc object to copy
	 */
	public BricklinkCategory(BricklinkCategory blc) {
		// creates a copy of a category
		id = blc.id;
		catid = blc.catid;
		name = blc.name;
	}

	

	/**
	 * creates Bricklink category table
	 * deletes old one, if any
	 * @throws SQLException
	 */
	public static void createTable() throws SQLException {
		
		Statement st;
		
		st = BricklinkLib.db.createStatement();
		st.execute("DROP TABLE IF EXISTS "+table+"; " +
				"CREATE TABLE "+table+" (" +
				"id INT PRIMARY KEY AUTO_INCREMENT," +
				"catid INT UNIQUE, " +
				"name VARCHAR(255)" +
				"); COMMIT ");
	}


	
	/**
	 * insert current category in database
	 * @throws SQLException
	 */
	public void insert() throws SQLException {
		
		insertPS.setInt(1, catid);
		insertPS.setString(2, name);
		insertPS.executeUpdate();
		
	}


	/**
	 * Prepare for category list update
	 * @throws SQLException
	 */
	public void beginUpdate() throws SQLException {
		
		BricklinkLib.db.autocommitDisable();
	}
	
	
	/**
	 * Restore old list if an update fails
	 * @throws SQLException
	 */
	public void abortUpdate() throws SQLException {
		
		BricklinkLib.db.rollback();
		BricklinkLib.db.autocommitEnable();
	}
	
	
	/**
	 * Task to complete list update
	 * @throws SQLException
	 */
	public void endUpdate() throws SQLException {
		
		BricklinkLib.db.commit();
		BricklinkLib.db.autocommitEnable();
	}
	
	
	
	
	// query functions
	
	/**
	 * returns category name by id
	 * @param id category to retrieve
	 * @return category name corresponding to id or empty string if no category
	 * @throws SQLException
	 */
	public static String getNameById(int id) throws SQLException {
		
		Statement st = BricklinkLib.db.createStatement();
		ResultSet rs = st.executeQuery("Select name FROM "+table+" where catid="+id);
		if (rs.next()) 
			return rs.getString("name");
		return "";
	}


	
	/**
	 * return a copy of category object given id
	 * @param id category to retrieve
	 * @return copy of category object
	 * @throws SQLException
	 */
	public static BricklinkCategory getById(int id) throws SQLException {
		
		Statement st = BricklinkLib.db.createStatement();
		ResultSet rs = st.executeQuery("Select id,catid,name FROM "+table+" where catid="+id);
		if (rs.next()) {
			BricklinkCategory bl = new BricklinkCategory();
			bl.id = rs.getInt("id");
			bl.catid = rs.getInt("catid");
			bl.name = rs.getString("name");
			return bl;
		}
		return null;
	}


	/**
	 * returns all category in database
	 * @return Vector of all category objects
	 * @throws SQLException
	 */
	public static Vector<BricklinkCategory> getAll() throws SQLException {
		
		Statement st = BricklinkLib.db.createStatement();
		ResultSet rs = st.executeQuery("SELECT id,catid,name FROM "+table+" ORDER BY name");
		Vector<BricklinkCategory> catlist = new Vector<BricklinkCategory>();
		BricklinkCategory blc;
		while (rs.next()) {
			// fetch and assign rows to an Array list
			blc = new BricklinkCategory();
			blc.id = rs.getInt("id");
			blc.catid = rs.getInt("catid");
			blc.name = rs.getString("name");
			catlist.add(blc);
		}
		return catlist;
	}


	
	/**
	 * returns only categories related to parts
	 * @return list of part categories
	 * @throws SQLException
	 */
	public static Vector<BricklinkCategory> getPartCategories() throws SQLException {
		
		Statement st = BricklinkLib.db.createStatement();
		ResultSet rs = st.executeQuery("SELECT id,catid,name FROM "+table+" WHERE catid IN " +
				"(SELECT DISTINCT catid FROM "+BricklinkPart.table+") ORDER BY name");
		Vector<BricklinkCategory> catlist = new Vector<BricklinkCategory>();
		BricklinkCategory blc;
		while (rs.next()) {
			// fetch and assign rows to an Array list
			blc = new BricklinkCategory();
			blc.id = rs.getInt("id");
			blc.catid = rs.getInt("catid");
			blc.name = rs.getString("name");
			catlist.add(blc);
		}
		return catlist;
	}

	

	/**
	 * returns all categories related to set
	 * @return list of set categories
	 * @throws SQLException
	 */
	public static Vector<BricklinkCategory> getSetCategories() throws SQLException {
		
		Statement st = BricklinkLib.db.createStatement();
		ResultSet rs = st.executeQuery("SELECT id,catid,name FROM "+table+" WHERE catid IN " +
				"(SELECT DISTINCT catid FROM "+BricklinkSet.table+") ORDER BY name");
		Vector<BricklinkCategory> catlist = new Vector<BricklinkCategory>();
		BricklinkCategory blc;
		while (rs.next()) {
			// fetch and assign rows to an Array list
			blc = new BricklinkCategory();
			blc.id = rs.getInt("id");
			blc.catid = rs.getInt("catid");
			blc.name = rs.getString("name");
			catlist.add(blc);
		}
		return catlist;
	}
	
	
	
	
}
