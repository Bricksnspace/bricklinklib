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

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableColumnModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

public class BlSetSearch extends JDialog implements ActionListener {


	private static final long serialVersionUID = 14901934952344972L;
	private final JPanel contentPanel = new JPanel();
	private JTable table;
	private BLSetTableModel tableModel;
	private JTextField searchText;
	private JPanel userPane;
	private JButton button;
	private JButton okButton;
	private JButton cancelButton;
	private int userChoice = JOptionPane.CLOSED_OPTION;
	private JScrollPane scrollPane;
	private GridBagConstraints gbc;
	private JComboBox<String> yearCombo;
	private JTextField year;
	private BlCategoryCombo catCombo;
	private JCheckBox catSel;
	private JComboBox<String> searchBy;


	
	public BlSetSearch(JFrame owner, String title, boolean modal) {
		
		super(owner,title,modal);
		initialize();
	}
	
	
	public BlSetSearch(JDialog owner, String title, boolean modal) {
		
		super(owner,title,modal);
		initialize();
	}
	
	
	
	
	
	private void initialize() {
		
		setLocationByPlatform(true);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(4, 4));
		
		scrollPane = new JScrollPane();
		
		contentPanel.add(scrollPane, BorderLayout.CENTER);

		tableModel = new BLSetTableModel();
		table = new JTable(tableModel);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setAutoCreateRowSorter(true);
		TableColumnModel tcm = table.getColumnModel();
		tcm.getColumn(0).setPreferredWidth(25);
		tcm.getColumn(1).setPreferredWidth(250);
		tcm.getColumn(3).setPreferredWidth(20);
		
		userPane = new JPanel();
		contentPanel.add(userPane, BorderLayout.SOUTH);
		GridBagLayout gbl = new GridBagLayout();
		userPane.setLayout(gbl);

		searchBy = new JComboBox<String>();
		searchBy.addItem("By set #");
		searchBy.addItem("By generic text");
		gbc = new GridBagConstraints();
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.ipady = 2;
		gbc.ipadx = 2;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 2;
		userPane.add(searchBy, gbc);
		
		searchText = new JTextField();
		searchText.setColumns(15);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridy = 0;
		userPane.add(searchText, gbc);
		searchText.addActionListener(this);
		
		button = new JButton("Search...");
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.gridheight = 2;
		userPane.add(button, gbc);
		button.addActionListener(this);

		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridheight = 1;
		yearCombo = new JComboBox<String>();
		yearCombo.addItem("Year is exactly");
		yearCombo.addItem("Year is before");
		yearCombo.addItem("Year is after");
		userPane.add(yearCombo,gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 2;
		year = new JTextField();
		userPane.add(year,gbc);

		gbc.gridx = 0;
		gbc.gridy = 3;
		catSel = new JCheckBox(" By Category ");
		catSel.setSelected(false);
		userPane.add(catSel,gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 3;
		catCombo = new BlCategoryCombo(BlCategoryCombo.BL_CAT_SET);
		userPane.add(catCombo,gbc);		

		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);

		okButton = new JButton("OK");
		buttonPane.add(okButton);
		okButton.addActionListener(this);
		getRootPane().setDefaultButton(okButton);

		cancelButton = new JButton("Cancel");
		buttonPane.add(cancelButton);
		cancelButton.addActionListener(this);
		
		scrollPane.setPreferredSize(new Dimension(600,200));
		scrollPane.setViewportView(table);
		
		pack();
		
		searchText.requestFocusInWindow();
		
	}

	@Override
	public void actionPerformed(ActionEvent ev) {
		//System.out.println(ev.toString());
		if (ev.getSource() == button || ev.getSource() == searchText) {
			doSearch();
		}
		else if (ev.getSource() == okButton) {
			userChoice = JOptionPane.OK_OPTION;
			setVisible(false);
		}
		else if (ev.getSource() == cancelButton) {
			userChoice = JOptionPane.CANCEL_OPTION;
			setVisible(false);
		}
		
	}

	
	public int getResponse() {
		return userChoice;
	}
	
	
	public BricklinkSet getSelected() {
		if (table.getSelectedRow() >= 0) {
			return tableModel.getPart(table.convertRowIndexToModel(table.getSelectedRow()));
		}
		return null;
	}
	
	
	private void doSearch() {
		
		String query;
		
		int y = 0;
		if (year.getText().length() > 0) {
			try {
				y = Integer.parseInt(year.getText());
			}
			catch (NumberFormatException e) {
				y = 0;
			}
		}
		String yearCompare = "";
		switch (yearCombo.getSelectedIndex()) {
		case 0:
			yearCompare = "=";
			break;
		case 1:
			yearCompare = "<=";
			break;
		case 2:
			yearCompare = ">=";
			break;
		}
		query = "";
		if (searchBy.getSelectedIndex() == 0 && searchText.getText().length() > 0) {
			query = " setid like ? ";
		}
		else if (searchBy.getSelectedIndex() == 1 && searchText.getText().length() > 0) {
			query = " ";
		}
		if (catSel.isSelected()) {
			if (query.length() > 0) {
				query += " AND ";
			}
			query += " catid="+((BricklinkCategory)catCombo.getSelectedItem()).getCatid()+" ";
		}
		if (y > 0) {
			if (query.length() > 0) {
				query += " AND ";
			}
			query += " year"+yearCompare+Integer.toString(y)+" ";
		}
		try {
			PreparedStatement ps;
			if (searchBy.getSelectedIndex() == 0 && searchText.getText().length() > 0) {
				ps = BricklinkSet.prepareSelect(query);
				ps.setString(1, "%"+searchText.getText()+"%");
			}
			else if (searchBy.getSelectedIndex() == 1 && searchText.getText().length() > 0) {
				ps = BricklinkSet.prepareFTS(query);
				ps.setString(1, searchText.getText());
			}
			else if (query.length() > 0){
				ps = BricklinkSet.prepareSelect(query);
			}
			else 
				return;
			ArrayList<BricklinkSet> pl = BricklinkSet.getPS(ps);
			tableModel.setParts(pl);
			scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMinimum());
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(this, 
					"Error retrieving Bricklink set from database.\n"+e.getLocalizedMessage(), 
					"Database error", JOptionPane.ERROR_MESSAGE);
		}
	}

	
}

