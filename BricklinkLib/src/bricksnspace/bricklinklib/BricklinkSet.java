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
import java.util.ArrayList;


/**
 * Generic Bricklink set
 * 
 * @author Mario Pascucci
 *
 */
public class BricklinkSet {

	protected int id;
	protected String setid;
	protected String name;
	protected int catid;
	protected String catname;
	protected int year;
	protected float weight; 				// weight in grams
	protected float dimx, dimy, dimz;		// size in cm
	public static final String table = "blsets";
	private static PreparedStatement insertPS = null;
	public final static String fieldsOrder = "setid,name,category,catid,year,weight,dimx,dimy,dimz";
	
	
	@Override
	public String toString() {
		return "BricklinkSet [id=" + id + ", setid=" + setid + ", name=" + name
				+ ", category=" + catname +" ("+ catid + "), year="
				+ year + ", weight (g)=" + weight +
				", dim x,y,z (cm)=" +dimx+","+dimy+","+dimz+
				" ]";
	}


	
	
	
	/**
	 * @return the setid
	 */
	public String getSetid() {
		return setid;
	}





	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}





	/**
	 * @return the catid
	 */
	public int getCatid() {
		return catid;
	}





	/**
	 * @return the catname
	 */
	public String getCatname() {
		return catname;
	}





	/**
	 * @return the year
	 */
	public int getYear() {
		return year;
	}





	/**
	 * Init database functions
	 * @param bdb bdb database connector
	 * @throws SQLException
	 */
	public static void init() throws SQLException {

		// prepared statement
		insertPS = BricklinkLib.db.prepareStatement("INSERT INTO "+table+" ("+fieldsOrder+
				") VALUES (?,?,?,?,?,?,?,?,?)"
				);
	}
	

	/**
	 * Creates Bricklink sets table
	 * remove first full text index, if any
	 * @throws SQLException
	 */
	public static void createTable() throws SQLException {
		
		Statement st;
		
		BricklinkLib.db.deleteFTS(table.toUpperCase());
		st = BricklinkLib.db.createStatement();
		st.execute("DROP TABLE IF EXISTS "+table+"; " +
				"CREATE TABLE "+table+" (" +
				"id INT PRIMARY KEY AUTO_INCREMENT," +
				"setid VARCHAR(64)," +
				"name VARCHAR(255)," +
				"category VARCHAR(255)," +
				"catid INT," +
				"year INT," +
				"weight REAL," +
				"dimx REAL," +
				"dimy REAL," +
				"dimz REAL" +				
				"); COMMIT ");
		
	}


	
	/**
	 * Creates full text search index for sets<br> using Bricklink set name and category<br>
	 * deletes any old index
	 * @throws SQLException
	 */
	public static void createFTS() throws SQLException {
				
		BricklinkLib.db.createFTS(table.toUpperCase(),"NAME,CATEGORY");	
	}


	
	
	/**
	 * Insert a set in table<br>
	 * set attributes must be already defined<br>
	 * category name is added on the fly, querying Bricklink categories table 
	 * @throws SQLException
	 */
	public void insert() throws SQLException {
		
		insertPS.setString(1,setid);
		insertPS.setString(2,name);
		insertPS.setString(3,BricklinkCategory.getNameById(catid));
		insertPS.setInt(4,catid);
		insertPS.setInt(5,year);
		insertPS.setFloat(6, weight);
		insertPS.setFloat(7, dimx);
		insertPS.setFloat(8, dimy);
		insertPS.setFloat(9, dimz);
		insertPS.executeUpdate();
		
	}

	
	/**
	 * Retrieve sets using a prepared statement<br>
	 * @see bricksnspace.bricklinklib.BricklinkSet#prepareSelect(String) 
	 * @see bricksnspace.bricklinklib.BricklinkSet#prepareFTS(String)
	 * @param ps prepared statement for query 
	 * @return list of Bricklink set
	 * @throws SQLException
	 */
	public static ArrayList<BricklinkSet> getPS(PreparedStatement ps) throws SQLException {

		ArrayList<BricklinkSet> blset = new ArrayList<BricklinkSet>();
		BricklinkSet bs;
		ResultSet rs;
		
		rs = ps.executeQuery();
		while (rs.next()) {
			// fetch and assign rows to an Array list
			bs = new BricklinkSet();
			bs.setid = rs.getString("setid");
			bs.name = rs.getString("name");
			bs.catname = rs.getString("category");
			bs.catid = rs.getInt("catid");
			bs.year = rs.getInt("year");
			bs.weight = rs.getFloat("weight");
			bs.dimx = rs.getFloat("dimx");
			bs.dimy = rs.getFloat("dimy");
			bs.dimz = rs.getFloat("dimz");
			blset.add(bs);
		}
		return blset;
	}

	
	
	/**
	 * Prepare a SELECT statement for Bricklink set table
	 * @see bricksnspace.bricklinklib.BricklinkSet#getPS(PreparedStatement)
	 * @param query string for WHERE clause, or <code>null</code> for SELECT all
	 * @return a prepared statement
	 * @throws SQLException
	 */
	public static PreparedStatement prepareSelect(String query) throws SQLException {
		
		if (query != null && query.length() > 0) {
			return BricklinkLib.db.prepareStatement("SELECT "+fieldsOrder+" FROM "+table+" WHERE "+query);
		}
		else {
			return BricklinkLib.db.prepareStatement("SELECT "+fieldsOrder+" FROM "+table);
		}
	}
	
	
	
	/**
	 * Prepare a Full Text Search for Bricklink set table<br>
	 * Returned PreparedStatement needs a full text search query string in parameter index 1<br>
	 * supplied outside this function. 
	 * @see bricksnspace.bricklinklib.BricklinkSet#getPS(PreparedStatement)
	 * @param query string for WHERE clause to refine Full Text Search, or <code>null</code> for all results
	 * @return a prepared statement
	 * @throws SQLException
	 */	
	public static PreparedStatement prepareFTS(String query) throws SQLException {
		
		if (query != null && query.length() > 0) {
			return BricklinkLib.db.prepareStatement("SELECT id,"+fieldsOrder+" FROM FTL_SEARCH_DATA(?,0,0) f " +
						"LEFT JOIN "+table+" b on (f.keys[0]=b.id) WHERE f.table='BLSETS' "+query);
		}
		else {
			return BricklinkLib.db.prepareStatement("SELECT id,"+fieldsOrder+" FROM FTL_SEARCH_DATA(?,0,0) f " +
					"LEFT JOIN "+table+" b on (f.keys[0]=b.id) WHERE f.table='BLSETS'");
		}
	}
	

	
	/**
	 * Search a set where id starting with <code>setid</code> in a LIKE type query<br>
	 * a '%' is added after <code>setid</code> string 
	 * @param setid set id to search for
	 * @return
	 * @throws SQLException
	 */
	public static ArrayList<BricklinkSet> getById(String setid) throws SQLException {
		
		PreparedStatement ps;
		
		ps = BricklinkLib.db.prepareStatement("SELECT "+fieldsOrder+" FROM "+table+" WHERE setid like ?");
		setid += "%";
		ps.setString(1, setid);
		return getPS(ps);
	}

	
	
}
