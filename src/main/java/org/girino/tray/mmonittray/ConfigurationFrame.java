/*******************************************************************************
 * Copyright (c) 2019 by Girino Vey.
 * 
 * Permission to use this software, modify and distribute it, or parts of it, is 
 * granted to everyone who wishes provided that the above copyright notice 
 * is kept or the conditions of the full version of this license are met.
 * 
 * See Full license at: https://girino.org/license/
 ******************************************************************************/
package org.girino.tray.mmonittray;

import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

public class ConfigurationFrame extends JFrame {

	private static final String[] FIELDS_TITLE = new String[] {
		"Field Name", "Field Value"
	};
	private static final String[] TITLE_RULES = new String[] {"Color", "Rule"};
	private JPanel contentPane;
	private JTextField urlField;
	private JLabel lblNewLabel;
	private JLabel authLabel;
	private JComboBox authComboBox;
	private JPanel panel;
	private JButton addRuleButton;
	private JButton delRuleButton;
	private JTable rulesTable;
	private JScrollPane scrollPane;
	private JScrollPane scrollPane_1;
	private JLabel lblNewLabel_1;
	private JTable FieldsTable;
	private JPanel panel_1;
	private JButton addFieldButton;
	private JButton delFieldButton;
	private JLabel PathsLabel;
	private JScrollPane scrollPane_2;
	private JTable pathsTable;
	private JSplitPane splitPane;
	private JButton saveButton;
	private JButton cancelButton;
	private Callback callback;

	public Callback getCallback() {
		return callback;
	}

	public void setCallback(Callback callback) {
		this.callback = callback;
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Properties p = new Properties();
					p.load(App.class.getResourceAsStream("/properties/config.properties"));

					
					Callback c = new Callback() {
						
						@Override
						public void onSave(Properties p) {
							System.out.println(new TreeMap(p));
						}
						
						@Override
						public void onCancel() {
							System.out.println("cancelled");
						}
					};


					ConfigurationFrame frame = new ConfigurationFrame(p, c);
					
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public ConfigurationFrame(Properties p, Callback c) {
		this();
		this.fromProperties(p);
		this.setCallback(c);
	}

	
	/**
	 * Create the frame.
	 */
	public ConfigurationFrame() {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 640, 480);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		setContentPane(contentPane);
		contentPane.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("max(42dlu;default)"),
				FormSpecs.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				RowSpec.decode("max(12dlu;default)"),
				FormSpecs.RELATED_GAP_ROWSPEC,
				RowSpec.decode("60dlu"),
				FormSpecs.RELATED_GAP_ROWSPEC,
				RowSpec.decode("49dlu"),
				FormSpecs.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_ROWSPEC,}));
		
		JLabel urlLabel = new JLabel("URL");
		contentPane.add(urlLabel, "2, 2, right, default");
		
		urlField = new JTextField();
		contentPane.add(urlField, "4, 2, fill, default");
		urlField.setColumns(10);
		
		authLabel = new JLabel("Auth. Type");
		contentPane.add(authLabel, "2, 4, right, default");
		
		authComboBox = new JComboBox();
		authComboBox.setModel(new DefaultComboBoxModel(new String[] {"Form", "Basic"}));
		authComboBox.setSelectedIndex(0);
		contentPane.add(authComboBox, "4, 4, fill, default");
		
		PathsLabel = new JLabel("Paths");
		contentPane.add(PathsLabel, "2, 6");
		
		scrollPane_2 = new JScrollPane();
		scrollPane_2.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		scrollPane_2.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		contentPane.add(scrollPane_2, "4, 6, fill, fill");
		
		pathsTable = new JTable();
		pathsTable.setRowSelectionAllowed(false);
		pathsTable.setModel(new DefaultTableModel(
			new Object[][] {
				{"Init", null},
				{"Login", null},
				{"Logout", null},
				{"API", null},
			},
			new String[] {
				"Action", "Path"
			}
		) {
			Class[] columnTypes = new Class[] {
				String.class, String.class
			};
			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
			boolean[] columnEditables = new boolean[] {
				false, true
			};
			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		});
		pathsTable.getColumnModel().getColumn(0).setResizable(false);
		pathsTable.getColumnModel().getColumn(0).setPreferredWidth(57);
		pathsTable.getColumnModel().getColumn(1).setPreferredWidth(576);
		scrollPane_2.setViewportView(pathsTable);
		
		lblNewLabel_1 = new JLabel("Form Fields");
		contentPane.add(lblNewLabel_1, "2, 8");
		
		scrollPane_1 = new JScrollPane();
		scrollPane_1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		contentPane.add(scrollPane_1, "4, 8, fill, fill");
		
		FieldsTable = new JTable();
		FieldsTable.setModel(new DefaultTableModel(
			new Object[][] {
				{null, null},
				{null, null},
				{null, null},
			},
			FIELDS_TITLE
		));
		FieldsTable.getColumnModel().getColumn(1).setPreferredWidth(546);
		scrollPane_1.setViewportView(FieldsTable);
		
		panel_1 = new JPanel();
		contentPane.add(panel_1, "6, 8, left, center");
		
		addFieldButton = new JButton("Add");
		addFieldButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				DefaultTableModel m = (DefaultTableModel)FieldsTable.getModel();
				m.addRow(new Object[] {null, null});
			}
		});
		panel_1.setLayout(new GridLayout(0, 1, 0, 0));
		panel_1.add(addFieldButton);
		
		delFieldButton = new JButton("Delete");
		delFieldButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
	            if(FieldsTable.getSelectedRow() != -1) {
					DefaultTableModel m = (DefaultTableModel)FieldsTable.getModel();
	                m.removeRow(FieldsTable.getSelectedRow());
	                JOptionPane.showMessageDialog(null, "Selected field deleted successfully");
	             }
			}
		});
		panel_1.add(delFieldButton);
		
		lblNewLabel = new JLabel("Rules");
		contentPane.add(lblNewLabel, "2, 10, right, default");
		
		scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		contentPane.add(scrollPane, "4, 10, fill, fill");
		
		rulesTable = new JTable();
		scrollPane.setViewportView(rulesTable);
		rulesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		rulesTable.setModel(new DefaultTableModel(
			new Object[][] {
				{null, null},
				{null, null},
				{null, null},
				{null, null},
				{null, null},
			},
			TITLE_RULES
		) {
			Class[] columnTypes = new Class[] {
				String.class, String.class
			};
			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
		});
		rulesTable.getColumnModel().getColumn(0).setPreferredWidth(62);
		rulesTable.getColumnModel().getColumn(1).setPreferredWidth(570);
		
		panel = new JPanel();
		contentPane.add(panel, "6, 10, left, center");
		
		addRuleButton = new JButton("Add");
		addRuleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultTableModel m = (DefaultTableModel)rulesTable.getModel();
				m.addRow(new Object[] {null, null});
			}
		});
		panel.setLayout(new GridLayout(0, 1, 0, 0));
		panel.add(addRuleButton);
		
		delRuleButton = new JButton("Delete");
		delRuleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
	            if(rulesTable.getSelectedRow() != -1) {
					DefaultTableModel m = (DefaultTableModel)rulesTable.getModel();
	                m.removeRow(rulesTable.getSelectedRow());
	                JOptionPane.showMessageDialog(null, "Selected rule deleted successfully");
	             }
			}
		});
		panel.add(delRuleButton);
		
		splitPane = new JSplitPane();
		contentPane.add(splitPane, "4, 12, center, fill");
		
		saveButton = new JButton("Save");
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					callback.onSave(toProperties());
					dispose();
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(null, "Error saving: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		splitPane.setLeftComponent(saveButton);
		
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				callback.onCancel();
				dispose();
			}
		});
		splitPane.setRightComponent(cancelButton);
	}
	
	private <A, B> Map<A, B> toMap(List<Pair<A, B>> pages) {
		return pages.stream().collect(Collectors.toMap(Pair<A, B>::getKey, Pair<A, B>::getValue));
	}

	private List<Pair<String, String>> getPropertiesAsList(Properties p, String prefix) {
		
		return p.keySet().stream()
				.filter((key) -> ((String)key).startsWith(prefix))
				.map( (key) -> new Pair<String, String>( ((String)key).substring(prefix.length()), p.getProperty((String)key)) )
				.collect(Collectors.toList());
	}
	
	public void fromProperties(Properties p) {
		
		// server
		String server = p.getProperty("server", "https://mmonit.example.com");
		
		// pages
		Map<String,String> pageMap = toMap(getPropertiesAsList(p, "path."));
		
		// queries
		List<Pair<String, String>> queries = getPropertiesAsList(p, "query.").stream()
				.sorted((a,b) -> a.getKey().compareTo(b.getKey()))
				.map((q) -> new Pair<String, String>(q.getKey().replaceFirst("^\\d+\\.", ""), q.getValue()))
				.collect(Collectors.toList());
		
		// auth type and auth fields
		String authType = p.getProperty("auth.type", "FORM"); 
		List<Pair<String, String>> authStrings = getPropertiesAsList(p, "auth.form.");
		
		setServer(server);
		setQueries(queries);
		setPages(pageMap);
		setAuthType(authType);
		setAuthStrings(authStrings);
	}

	private void setAuthStrings(List<Pair<String, String>> authStrings) {
		DefaultTableModel m = (DefaultTableModel)FieldsTable.getModel();
		// remove all, add in order
		String[][] out = new String[authStrings.size()][2];
		for (int row = 0; row < out.length; row++) {
			out[row][0] = authStrings.get(row).getKey();
			out[row][1] = authStrings.get(row).getValue();
		}
		m.setDataVector(out, FIELDS_TITLE);
	}

	private void setAuthType(String authType) {
		authComboBox.setSelectedIndex(authType.equalsIgnoreCase("form")?0:1);
	}

	private void setQueries(List<Pair<String, String>> queries) {
		DefaultTableModel m = (DefaultTableModel)rulesTable.getModel();
		// remove all, add in order
		String[][] out = new String[queries.size()][2];
		for (int row = 0; row < out.length; row++) {
			out[row][0] = queries.get(row).getKey();
			out[row][1] = queries.get(row).getValue();
		}
		m.setDataVector(out, TITLE_RULES);
	}

	private void setServer(String server) {
		urlField.setText(server);
	}

	private void setPages(Map<String, String> pageMap) {
		DefaultTableModel m = (DefaultTableModel)pathsTable.getModel();
		for (int row = 0; row < m.getRowCount(); row++) {
			String key = m.getValueAt(row, 0).toString().toLowerCase();
			String value = null;
			if (pageMap.containsKey(key)) {
				value = pageMap.get(key);
			}
			m.setValueAt(value, row, 1);
		}
	}
	
	public Properties toProperties() {
		Properties p = new Properties();

		// server
		p.setProperty("server", urlField.getText());
		
		// pages
		TableModel pages = pathsTable.getModel();
		for (int row = 0; row < pages.getRowCount(); row++) {
			String prefix = "path.";
			if (pages.getValueAt(row, 0) != null && pages.getValueAt(row, 1) != null) {
				p.setProperty(prefix + pages.getValueAt(row, 0).toString().toLowerCase(), pages.getValueAt(row, 1).toString());
			}
		}
		
		// rules/queries
		TableModel rules = rulesTable.getModel();
		for (int row = 0; row < rules.getRowCount(); row++) {
			String prefix = "query." + row + ".";
			if (rules.getValueAt(row, 0) != null && rules.getValueAt(row, 1) != null) {
				p.setProperty(prefix + rules.getValueAt(row, 0).toString(), rules.getValueAt(row, 1).toString());
			}
		}
		
		// auth type
		p.setProperty("auth.type", authComboBox.getSelectedIndex() == 0 ? "FORM" : "BASIC");
		
		// fields
		TableModel fields = FieldsTable.getModel();
		for (int row = 0; row < fields.getRowCount(); row++) {
			String prefix = "auth.form.";
			if (fields.getValueAt(row, 0) != null && fields.getValueAt(row, 1) != null) {
				p.setProperty(prefix + fields.getValueAt(row, 0).toString(), fields.getValueAt(row, 1).toString());
			}
		}
		
		return p;
	}
}
