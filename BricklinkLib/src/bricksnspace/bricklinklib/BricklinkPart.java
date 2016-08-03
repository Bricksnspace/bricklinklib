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
import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * Generic Bricklink part
 * 
 * @author Mario Pascucci
 *
 */
public class BricklinkPart {
	
	protected int id;
	/** Category id */
	protected int catId;
	/** category name */
	protected String catname;
	/** Bricklink part id */
	protected String blid;
	/** Part name, as defined by Bricklink */
	protected String name;
	/** part weight in grams */
	protected float weight;
	/** part size in "bricks" (plates are 0.33 units high) */
	protected float x,y,z;
	/** true if part was deleted by Bricklink */
	protected boolean deleted;
	/** last modified time */
	protected Timestamp lastmod;
	/** table name in database */
	public static final String table = "blparts";
	private static PreparedStatement insertPS = null;
	private static PreparedStatement updatePS = null;
	protected final static String fieldsOrder = "blid,name,catid,category,weight,dimx,dimy,dimz,deleted,lastmod";
	
	
	
	public BricklinkPart() {
		;
	}

	
	@Override
	public String toString() {
		return "BrickLinkPart [id=" + id + ", category(ID)=" + catname+"("+catId + "), blid="
				+ blid + ", name=" + name + ", weight=" + weight + ", x=" + x
				+ ", y=" + y + ", z=" + z + ", deleted="+deleted+", lastmod="+lastmod+"]";
	}
	
	
	/*
	 * get and set
	 */
	
	
	/**
	 * @return the catId
	 */
	public int getCatId() {
		return catId;
	}


	/**
	 * @return the catname
	 */
	public String getCatname() {
		return catname;
	}


	/**
	 * @return the blid
	 */
	public String getBlid() {
		return blid;
	}


	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}


	/**
	 * @return the deleted
	 */
	public boolean isDeleted() {
		return deleted;
	}


	/**
	 * @return the lastmod
	 */
	public Timestamp getLastmod() {
		return lastmod;
	}


	/**
	 * Init database functions
	 * @param bdb database connector
	 * @throws SQLException
	 */
	public static void init() throws SQLException {

		// creates index if not exists
		Statement st = BricklinkLib.db.createStatement();
		st.executeUpdate("CREATE INDEX IF NOT EXISTS blp_blid ON "+BricklinkPart.table+"(blid)");		
		// prepared statements
		insertPS = BricklinkLib.db.prepareStatement("INSERT INTO "+table+" " +
				"("+fieldsOrder+") VALUES " +
				"(?,?,?,?,?,?,?,?,?,NOW())" +
				";",Statement.RETURN_GENERATED_KEYS);
		// no update time, part is "lastmod" when inserted first time
		updatePS = BricklinkLib.db.prepareStatement("UPDATE "+table+" SET " +
				"blid=?," +
				"name=?," +
				"catid=?," +
				"category=?," +
				"weight=?," +
				"dimx=?," +
				"dimy=?," +
				"dimz=?," +
				"deleted=?" +
				"WHERE id=? " +
				";");

	}
	

	/**
	 * Creates Bricklink parts table
	 * remove first full text index, if any
	 * @throws SQLException
	 */
	public static void createTable() throws SQLException {
		
		Statement st;
		
		BricklinkLib.db.deleteFTS(table.toUpperCase());
		st = BricklinkLib.db.createStatement();
		st.execute("DROP TABLE IF EXISTS "+table+"; " +
				"CREATE TABLE "+table+" (" +
				"id INT PRIMARY KEY AUTO_INCREMENT, " +
				"blid VARCHAR(64)," +
				"name VARCHAR(255)," +
				"catid INT," +
				"category VARCHAR(64)," +
				"weight REAL," +
				"dimx REAL," +
				"dimy REAL," +
				"dimz REAL," +
				"deleted BOOL," +
				"lastmod TIMESTAMP" +
				"); COMMIT ");
		
	}

	
	
	/**
	 * Set part category by id and sets according category name
	 * @param id Bricklink category
	 * @throws SQLException
	 */
	public void setCategory(int id) throws SQLException {
		
		catname = BricklinkCategory.getNameById(id);
		catId = id;
	}
	
	
	
	/**
	 * update Bricklink part
	 * @throws SQLException
	 */
	public void update() throws SQLException{
		
		updatePS.setString(1, blid);
		updatePS.setString(2, name);
		updatePS.setInt(3, catId);
		updatePS.setString(4, BricklinkCategory.getNameById(catId));
		updatePS.setFloat(5, weight);
		updatePS.setFloat(6, x);
		updatePS.setFloat(7, y);
		updatePS.setFloat(8, z);
		updatePS.setBoolean(9, deleted);
		updatePS.setInt(10, id);
		
		updatePS.executeUpdate();
		return;
		
	}
	
	
	/**
	 * Prepare for part list update
	 * @throws SQLException
	 */
	public static void beginUpdate() throws SQLException {

		Statement st;
		
		
		BricklinkLib.db.deleteFTS(table.toUpperCase());
		BricklinkLib.db.autocommitDisable();
		st = BricklinkLib.db.createStatement();
		st.execute("UPDATE "+table+" SET deleted=TRUE");
	}

	
	/**
	 * Restore previous list if an update fails
	 * @throws SQLException
	 */
	public static void abortUpdate() throws SQLException {

		BricklinkLib.db.rollback();
		BricklinkLib.db.autocommitEnable();
		// restore full text index
		createFTS();
	}

	
	
	/**
	 * Completes an update
	 * @throws SQLException
	 */
	public static void endUpdate() throws SQLException {

		BricklinkLib.db.commit();
		BricklinkLib.db.autocommitEnable();
	}

	

	
	/**
	 * Creates full text search index for parts<br> using Bricklink part id, part name and part category<br>
	 * deletes any old index
	 * @throws SQLException
	 */
	public static void createFTS() throws SQLException {
		
		BricklinkLib.db.createFTS(table.toUpperCase(), "BLID,NAME,CATEGORY");
	}
	
	

	/**
	 * Insert a part in part table<br>
	 * part attributes must be already defined<br>
	 * category name is added on the fly, querying Bricklink categories table 
	 * @throws SQLException
	 */
	public void insert() throws SQLException {

		insertPS.setString(1, blid);
		insertPS.setString(2, name);
		insertPS.setInt(3, catId);
		insertPS.setString(4, BricklinkCategory.getNameById(catId));
		insertPS.setFloat(5, weight);
		insertPS.setFloat(6, x);
		insertPS.setFloat(7, y);
		insertPS.setFloat(8, z);
		insertPS.setBoolean(9, deleted);
		
		insertPS.executeUpdate();
	}

	
	
	private static BricklinkPart getPart(ResultSet rs) throws SQLException {
		
		BricklinkPart bp = new BricklinkPart();
		bp.id = rs.getInt("id");
		bp.blid = rs.getString("blid");
		bp.name = rs.getString("name");
		bp.catId = rs.getInt("catid");
		bp.catname = rs.getString("category");
		bp.weight = rs.getFloat("weight");
		bp.x = rs.getFloat("dimx");
		bp.y = rs.getFloat("dimy");
		bp.z = rs.getFloat("dimz");
		bp.deleted = rs.getBoolean("deleted");
		bp.lastmod = rs.getTimestamp("lastmod");
		return bp;
	}
	
	
	
	/**
	 * Retrieve a part using Bricklink part id<br>
	 * May return more than one part, because there can be duplicates (it is an error)
	 * @param blid Bricklink ID to retrieve
	 * @return
	 * @throws SQLException
	 */
	public static ArrayList<BricklinkPart> getById(String blid) throws SQLException {
		
		PreparedStatement ps;
		
		ps = BricklinkLib.db.prepareStatement("SELECT id,"+fieldsOrder+" FROM "+table+" where blid=?");
		ps.setString(1, blid);
		ArrayList<BricklinkPart> parts = getPS(ps);
		if (parts.size() > 1) 
			throw new SQLException("[BricklinkPart] Duplicated part definition in '"+table+"' table: "+blid);
		return parts;
	}
	

	
	/**
	 * Retrieve a list of parts, defined by user supplied prepared statement
	 * @param ps prepared statement to execute
	 * @return list of parts
	 * @throws SQLException
	 */
	public static ArrayList<BricklinkPart> getPS(PreparedStatement ps) throws SQLException {
		
		ArrayList<BricklinkPart> blp = new ArrayList<BricklinkPart>();
		ResultSet rs;
		
		rs = ps.executeQuery();
		while (rs.next()) {
			// fetch and assign rows to an Array list
			blp.add(getPart(rs));
		}
		return blp;

	}
	
	
	
	/**
	 * Retrieve parts by user supplied WHERE filterExpr<br>
	 * if filterExpr is null, returns all parts 
	 * @param filterExpr WHERE condition
	 * @return list of parts or all parts if filterExpr is null
	 * @throws SQLException
	 */
	public static ArrayList<BricklinkPart> get(String filterExpr) throws SQLException {

		ArrayList<BricklinkPart> blp = new ArrayList<BricklinkPart>();
		Statement st;
		ResultSet rs;
		
		if (filterExpr == null) {
			st = BricklinkLib.db.createStatement();
			rs = st.executeQuery("SELECT id,"+fieldsOrder+" FROM "+table+"");
		}
		else {
			st = BricklinkLib.db.createStatement();
			rs = st.executeQuery("SELECT id," + fieldsOrder +
				" FROM "+table+" WHERE " + filterExpr);
		}
		while (rs.next()) {
			// fetch and assign rows to an Array list
			blp.add(getPart(rs));
		}
		return blp;
	}
	
	
	
	/**
	 * Retrieve parts by full text search query, optionally with "refine search" filter<br>
	 * if <em>filter</em> is null, returns all results. 
	 * @param filterExpr full text search filter (syntax is H2 database and Lucene http://h2database.com/html/tutorial.html#fulltext)
	 * @param filter WHERE clause to refine search
	 * @return list of Bricklink parts
	 * @throws SQLException
	 */
	public static ArrayList<BricklinkPart> getFTS(String filterExpr,String filter) throws SQLException {
		
		ArrayList<BricklinkPart> blp = new ArrayList<BricklinkPart>();
		Statement st;
		ResultSet rs;

		//select b.*,f.score from FTL_SEARCH_DATA('words', 0, 0) f left join blparts b on(f.keys[0]=b.id) 
		//                 where f.table='BLPARTS';

		if (filterExpr != null) {
			st = BricklinkLib.db.createStatement();
			if (filter == null) {
				rs = st.executeQuery("SELECT id,"+fieldsOrder+" FROM FTL_SEARCH_DATA('"+filterExpr+"',0,0) f " +
						"LEFT JOIN "+table+" b on (f.keys[0]=b.id) WHERE f.table='BLPARTS'");
			}
			else {
				rs = st.executeQuery("SELECT id,"+fieldsOrder+" FROM FTL_SEARCH_DATA('"+filterExpr+"',0,0) f " +
						"LEFT JOIN "+table+" b on (f.keys[0]=b.id) WHERE f.table='BLPARTS' AND "+filter);
			}
				
			while (rs.next()) {
				// fetch and assign rows to an Array list
				blp.add(getPart(rs));
			}
		}
		return blp;
	}

	
	
	/**
	 * Returns a list of most recents parts<br>
	 * Select parts updated/changed within 15 minutes  
	 * @return
	 * @throws SQLException
	 */
	public static ArrayList<BricklinkPart> getNew() throws SQLException {
		
		PreparedStatement ps;
		
		ps = BricklinkLib.db.prepareStatement("SELECT id,"+fieldsOrder+
				" FROM "+table+" where lastmod > TIMESTAMPADD(MINUTE,-15,SELECT MAX(lastmod) from "+table+")");
		return getPS(ps);
	}
	

}
