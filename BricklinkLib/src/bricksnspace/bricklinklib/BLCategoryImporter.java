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
import java.io.FileReader;
import java.io.LineNumberReader;
import javax.swing.SwingWorker;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;

import bricksnspace.bricklinklib.BricklinkCategory;



/**
 * @author Pascucci
 * Bricklink category import function using SwingWorker
 */
public class BLCategoryImporter extends SwingWorker<Integer, Void> {

	File blcat;

	
	/*
	 * @param dbd Brick DB object
	 * @param blset XML file for BLink set database dump
	 */
	public BLCategoryImporter(File blcat) {

		this.blcat = blcat;
		
	}
	
	
	
	
	
	@Override
	protected Integer doInBackground() throws Exception {
		int i = 0;
		XMLEvent e;
		boolean isDoc,isCatalog,isItem;
		Characters ch;
		String tag;
		BricklinkCategory bc = new BricklinkCategory();
		String line;
		int lineNo;
		LineNumberReader lnr;
		XMLInputFactory xmlFact;
		
		lnr = new LineNumberReader(new FileReader(blcat));
		lineNo = 0;
		while ((line = lnr.readLine()) != null) {
			if (line.toLowerCase().indexOf("/item") > -1)
				lineNo++;
		}
		lnr.close();
		xmlFact = XMLInputFactory.newInstance();
		xmlFact.setProperty(XMLInputFactory.IS_COALESCING,true);
		XMLEventReader xer = xmlFact.createXMLEventReader(blcat.getPath(), new FileInputStream(blcat));
		setProgress(0);
		isDoc = false;
		isCatalog = false;
		isItem = false;
		tag = "";
		bc.name = "";
		bc.catid = 0;
		BricklinkCategory.createTable();
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
					if (bc.name.indexOf("&#") != -1) {
						int idx = 0;
						while ((idx = bc.name.indexOf("&#")) != -1) {
							bc.name = bc.name.replaceFirst("&#\\d\\d;", 
									String.valueOf((char)Integer.parseInt(bc.name.substring(idx+2, idx+4))));
						}
					}
					// end of xxx.
					//////////////////////////////////////////////////
					bc.insert();
					tag = "";
					bc.name = "";
					bc.catid = 0;
					i++;
					setProgress((i*100)/lineNo);
				}
				else if (tag == "CATALOG" && isDoc) {
					isCatalog = false;
				}
				break;
			case XMLEvent.CHARACTERS:
				ch = e.asCharacters();
				if (!ch.isIgnorableWhiteSpace() && !ch.isWhiteSpace()) {
					if (tag == "CATEGORYNAME" && isItem) {
						bc.name = ch.getData().trim();
					}
					else if (tag == "CATEGORY" && isItem) {
						try {
							bc.catid = Integer.parseInt(ch.getData());
						} catch (NumberFormatException e1) {
							bc.catid = 0;
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
		return lineNo;
	}


	

}

