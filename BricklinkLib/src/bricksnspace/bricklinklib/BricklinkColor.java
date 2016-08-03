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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;




/**
 * Generic Bricklink color
 * 
 * @author Mario Pascucci
 *
 */
public class BricklinkColor {
	
	/** id as assigned by Bricklink */
	protected int id;
	/** color name assigned by Bricklink */
	protected String name;
	/** RGB as "#rrggbb" hex values. A '#' is added by program */
	protected String rgb;
	/** type string as defined by Bricklink */
	protected String type;	
	/** how many parts exists in this color */
	protected int inpart;
	/** how many sets contains part in this color */
	protected int inset;
	/** how many wanted list contains parts in this color */
	protected int wanted;
	/** how many parts are in sell in this color */
	protected int sell;
	/** which year color was introduced first */
	protected int fromy;
	/** which year color was used last */ 
	protected int toy;

	protected static final String fieldsOrder = "id,name,rgb,type,inpart,inset,wanted,sell,fromy,toy";
	public static final String table = "blcolors";
	private static PreparedStatement insertPS = null;
	private static HashMap<Integer,BricklinkColor>colorMap;
	

	
	
	/**
	 * Empty constructor
	 */
	public BricklinkColor() {
		;
	}
	
	
	
	/**
	 * Init Bricklink colors functions<br>
	 * Fills internal cache with all color definitions
	 * @param bdb database connector
	 * @throws SQLException
	 */
	public static void init() throws SQLException {

		// prepared statements
		insertPS = BricklinkLib.db.prepareStatement("INSERT INTO " + table +
				"("+fieldsOrder+") VALUES " +
				"(?,?,?,?,?,?,?,?,?,?)" +
				";");
		colorMap = getAllColor();
	}
	
	
	
	/**
	 * Creates Bricklink color table<br>
	 * Drops any existing table, before.
	 * @throws SQLException
	 */
	public static void createTable() throws SQLException {
		
		Statement st;
		
		st = BricklinkLib.db.createStatement();
		st.execute("DROP TABLE IF EXISTS "+table+"; " +
				"CREATE TABLE "+table+" (" +
				"id INT PRIMARY KEY AUTO_INCREMENT," +
				"name VARCHAR(255)," +
				"rgb VARCHAR(16)," +
				"type VARCHAR(32)," +
				"inpart INT," +
				"inset INT," +
				"wanted INT," +
				"sell INT," +
				"fromy INT," +
				"toy INT" +
				"); COMMIT ");
	}
	
	
	
	/**
	 * Inserts current object in Bricklink color table
	 * @throws SQLException
	 */
	public void insert() throws SQLException {
		
		insertPS.setInt(1, id);
		insertPS.setString(2, name);
		insertPS.setString(3, rgb);
		insertPS.setString(4, type);
		insertPS.setInt(5,inpart);
		insertPS.setInt(6,inset);
		insertPS.setInt(7,wanted);
		insertPS.setInt(8,sell);
		insertPS.setInt(9, fromy);
		insertPS.setInt(10, toy);
		
		insertPS.executeUpdate();
		
	}

	
	
	/**
	 * Fetch full list of Bricklink color in database
	 * @return Bricklink color list
	 * @throws SQLException
	 */
	public static HashMap<Integer,BricklinkColor> getAllColor() throws SQLException {
		
		HashMap<Integer,BricklinkColor> allColor = new HashMap<Integer,BricklinkColor>();
		ArrayList<BricklinkColor> bc;
		PreparedStatement ps;
		
		ps = BricklinkLib.db.prepareStatement("SELECT "+fieldsOrder+" FROM "+table);
		bc = getPS(ps);
		for (BricklinkColor b : bc) {
			allColor.put(b.id, b);
		}
		return allColor;
	}
	
	
	
	/**
	 * Gets a Bricklink color list based on user-supplied prepared statement
	 * @param ps statement to execute
	 * @return selected color list
	 * @throws SQLException
	 */
	public static ArrayList<BricklinkColor> getPS(PreparedStatement ps) throws SQLException {
		
		ArrayList<BricklinkColor> brc = new ArrayList<BricklinkColor>();
		BricklinkColor bc;
		ResultSet rs;
		
		rs = ps.executeQuery();
		while (rs.next()) {
			// fetch and assign rows to an Array list
			//id,name,rgb,type,fromy,toy
			bc = new BricklinkColor();
			bc.id = rs.getInt("id");
			bc.name = rs.getString("name");
			bc.rgb = rs.getString("rgb");
			bc.type = rs.getString("type");
			bc.inpart = rs.getInt("inpart");
			bc.inset = rs.getInt("inset");
			bc.wanted = rs.getInt("wanted");
			bc.sell = rs.getInt("sell");
			bc.fromy = rs.getInt("fromy");
			bc.toy = rs.getInt("toy");
			brc.add(bc);
		}
		return brc;
	}
	

	
	/** 
	 * Fetch a Bricklink color from cache<br>
	 * If blid refers to non-existent color, return "black" (index 0)<br>
	 * Warning: it is original object, not a clone!
	 * @param blid index of color to get
	 * @return Bricklink color
	 */
	public static BricklinkColor getColor(int blid) {
		
		BricklinkColor bc;
		
		bc = colorMap.get(blid);
		if (bc == null) 
			return colorMap.get(0);
		else 
			return bc;
	}
	
	
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}



	/**
	 * Imports Bricklink colors from XML color dump<br>
	 * Clear color table first, but doesn't refresh color cache<br>
	 * Uses file right from Bricklink catalog at http://www.bricklink.com/catalogDownload.asp 
	 * @param fname file to read
	 * @return number of color imported
	 * @throws IOException
	 * @throws XMLStreamException
	 * @throws SQLException
	 */
	public int doImport(File fname) throws IOException, XMLStreamException, SQLException {
		
		int i = 0;
		XMLEvent e;
		boolean isDoc,isCatalog,isItem;
		Characters ch;
		String tag;
		//LineNumberReader lnr;
		XMLInputFactory xmlFact;
		BricklinkColor blc = new BricklinkColor();
		XMLEventReader xer;
		
		xmlFact = XMLInputFactory.newInstance();
		xmlFact.setProperty(XMLInputFactory.IS_COALESCING,true);
		xer = xmlFact.createXMLEventReader(fname.getPath(), new FileInputStream(fname));
		createTable();
		isDoc = false;
		isCatalog = false;
		isItem = false;
		tag = "";
		blc.id = 0;
		blc.name = "";
		blc.rgb = "";
		blc.type = "";
		blc.inpart = 0;
		blc.inset = 0;
		blc.wanted = 0;
		blc.sell = 0;
		blc.fromy = 0;
		blc.toy = 0;
		while (xer.hasNext()) {
			e = xer.nextEvent();
			switch (e.getEventType()) {
			case XMLEvent.START_DOCUMENT:
				isDoc = true;
				break;
			case XMLEvent.START_ELEMENT:
				tag = e.asStartElement().getName().getLocalPart();
				if (tag == "ITEM" && isCatalog) {
					isItem = true;
				}
				else if (tag == "CATALOG" && isDoc) {
					isCatalog = true;
				}
				break;
			case XMLEvent.END_ELEMENT:
				tag = e.asEndElement().getName().getLocalPart();
				if (tag == "ITEM" && isItem) {
					isItem = false;
					//////////////////////////////////////////////////
					// XXX: remove when BrickLink fixes XML export for special char (entities)
					// Descr: char like '(' or '"' are coded in file as: &amp;#40; &amp;#34;
					// XMLInputFactory handles "&amp;" -> '&' but leave "#40;"
					// these lines will convert the &#NN; to right entity
					if (blc.name.indexOf("&#") != -1) {
						int idx = 0;
						while ((idx = blc.name.indexOf("&#")) != -1) {
							blc.name = blc.name.replaceFirst("&#\\d\\d;", 
									String.valueOf((char)Integer.parseInt(blc.name.substring(idx+2, idx+4))));
						}
					}
					// end of xxx.
					//////////////////////////////////////////////////
					if (blc.rgb.length() == 0) {
						blc.rgb = "#000000";
					}
					else {
						blc.rgb = "#" + blc.rgb;
					}
					blc.insert();
					tag = "";
					blc.id = 0;
					blc.name = "";
					blc.rgb = "";
					blc.type = "";
					blc.inpart = 0;
					blc.inset = 0;
					blc.wanted = 0;
					blc.sell = 0;
					blc.fromy = 0;
					blc.toy = 0;
					i++;
				}
				else if (tag == "CATALOG" && isDoc) {
					isCatalog = false;
				}
				break;
			case XMLEvent.CHARACTERS:
				ch = e.asCharacters();
				if (!ch.isIgnorableWhiteSpace() && !ch.isWhiteSpace()) {
					if (tag == "COLOR" && isItem) {
						try {
							blc.id = Integer.parseInt(ch.getData());
						} catch (NumberFormatException e1) {
							blc.id = 0;
						}
					}
					else if (tag == "COLORTYPE" && isItem) {
						blc.type = ch.getData().trim();
					}
					else if (tag == "COLORNAME" && isItem) {
						blc.name = ch.getData().trim();
					}
					else if (tag == "COLORRGB" && isItem) {
						blc.rgb = ch.getData().trim();
					}
					else if (tag == "COLORCNTPARTS" && isItem) {
						try {
							blc.inpart = Integer.parseInt(ch.getData());
						} catch (NumberFormatException e1) {
							blc.inpart = 0;
						}
					}
					else if (tag == "COLORCNTSETS" && isItem) {
						try {
							blc.inset = Integer.parseInt(ch.getData());
						} catch (NumberFormatException e1) {
							blc.inset = 0;
						}
					}
					else if (tag == "COLORCNTWANTED" && isItem) {
						try {
							blc.wanted = Integer.parseInt(ch.getData());
						} catch (NumberFormatException e1) {
							blc.wanted = 0;
						}
					}
					else if (tag == "COLORCNTINV" && isItem) {
						try {
							blc.sell = Integer.parseInt(ch.getData());
						} catch (NumberFormatException e1) {
							blc.sell = 0;
						}
					}
					else if (tag == "COLORYEARFROM" && isItem) {
						try {
							blc.fromy = Integer.parseInt(ch.getData());
						} catch (NumberFormatException e1) {
							blc.fromy = 0;
						}
					}
					else if (tag == "COLORYEARTO" && isItem) {
						try {
							blc.toy = Integer.parseInt(ch.getData());
						} catch (NumberFormatException e1) {
							blc.toy = 0;
						}
					}
				}
			}
		}
		try {
			xer.close();
		} catch (XMLStreamException ex) {
			;
		}
		return i;
	}

}
