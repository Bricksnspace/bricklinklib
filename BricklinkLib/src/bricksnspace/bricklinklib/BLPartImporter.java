/**
	Copyright 2016-2017 Mario Pascucci <mpascucci@gmail.com>
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

/**
 * @author mario
 *
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.LineNumberReader;
import javax.swing.SwingWorker;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;


/*
 * Imports in background an XML file contains BLink part catalog
 * storing it in db
 * @see javax.swing.SwingWorker
 */
public class BLPartImporter extends SwingWorker<Integer, Void> {

	File blparts;

	/*
	 * @param dbd Brick DB object
	 * @param x an XMLEventReader 
	 * @param lineNo number of parts to process
	 */
	public BLPartImporter(File blparts) {

		this.blparts = blparts;
	}
	
	@Override
	protected Integer doInBackground() throws Exception {
		
		int i = 0;
		int lineNo = 0;
		XMLEvent e;
		boolean isDoc,isCatalog,isItem;
		Characters ch;
		String itemType,tag,line;
		LineNumberReader lnr;
		XMLInputFactory xmlFact;
		BricklinkPart bp = new BricklinkPart();
		XMLEventReader xer;
		
		lnr = new LineNumberReader(new FileReader(blparts));
		while ((line = lnr.readLine()) != null) {
			if (line.toLowerCase().indexOf("/itemid") > -1)
				lineNo++;
		}
		lnr.close();
		xmlFact = XMLInputFactory.newInstance();
		xmlFact.setProperty(XMLInputFactory.IS_COALESCING,true);
		xer = xmlFact.createXMLEventReader(blparts.getPath(), new FileInputStream(blparts));
		setProgress(0);
		isDoc = false;
		isCatalog = false;
		isItem = false;
		itemType = "";
		bp.blid = "";
		tag = "";
		bp.name = "";
		bp.catId = 0;
		bp.x = 0.0f;
		bp.y = 0.0f;
		bp.z = 0.0f;
		bp.weight = 0.0f;
		BricklinkPart.beginUpdate();
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
					bp.deleted = false;
					//////////////////////////////////////////////////
					// XXX: remove when BrickLink fixes XML export for special char (entities)
					// Descr: char like '(' or '"' are coded in file as: &amp;#40; &amp;#34;
					// XMLInputFactory handles "&amp;" -> '&' but leave "#40;"
					// these lines will convert the &#NN; to right entity
					if (bp.name.indexOf("&#") != -1) {
						int idx = 0;
						while ((idx = bp.name.indexOf("&#")) != -1) {
							bp.name = bp.name.replaceFirst("&#\\d\\d;", 
									String.valueOf((char)Integer.parseInt(bp.name.substring(idx+2, idx+4))));
						}
					}
					// end of xxx.
					//////////////////////////////////////////////////
					if (itemType.equals("P")) {
						BricklinkPart dbbp = BricklinkPart.getById(bp.blid);
						if (dbbp != null) {
							bp.id = dbbp.id;
							bp.update();
						}
						else {
							bp.insert();
						}
						i++;
						setProgress((i*100)/lineNo);
					}
					itemType = "";
					bp.blid = "";
					tag = "";
					bp.name = "";
					bp.catId = 0;
					bp.x = 0.0f;
					bp.y = 0.0f;
					bp.z = 0.0f;
					bp.weight = 0.0f;
				}
				else if (tag == "CATALOG" && isDoc) {
					isCatalog = false;
				}
				break;
			case XMLEvent.CHARACTERS:
				ch = e.asCharacters();
				if (!ch.isIgnorableWhiteSpace() && !ch.isWhiteSpace()) {
					if (tag == "ITEMID" && isItem) {
						bp.blid = ch.getData().trim();
					}
					else if (tag == "ITEMTYPE" && isItem) {
						itemType = ch.getData().trim();
					}
					else if (tag == "ITEMNAME" && isItem) {
						bp.name = ch.getData().trim();
					}
					else if (tag == "CATEGORY" && isItem) {
						try {
							bp.catId = Integer.parseInt(ch.getData());
						} catch (NumberFormatException e1) {
							bp.catId = 0;
						}
					}
					else if (tag == "ITEMWEIGHT" && isItem) {
						try {
							bp.weight = Float.parseFloat(ch.getData());
						} catch (NumberFormatException e1) {
							bp.weight = 0.0f;
						}
					}
					else if (tag == "ITEMDIMX" && isItem) {
						try {
							bp.x = Float.parseFloat(ch.getData());
						} catch (NumberFormatException e1) {
							bp.x = 0.0f;
						}
					}
					else if (tag == "ITEMDIMY" && isItem) {
						try {
							bp.y = Float.parseFloat(ch.getData());
						} catch (NumberFormatException e1) {
							bp.y = 0.0f;
						}
					}
					else if (tag == "ITEMDIMZ" && isItem) {
						try {
							bp.z = Float.parseFloat(ch.getData());
						} catch (NumberFormatException e1) {
							bp.z = 0.0f;
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
		if (i == 0) {
			// no parts in update?!?
			BricklinkPart.abortUpdate();
		}
		else {
			BricklinkPart.endUpdate();
		}
		BricklinkPart.createFTS();
		return i;
	}
	
}

