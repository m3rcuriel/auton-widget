package com.mvrt.smartdashboard.autonselector;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import edu.wpi.first.smartdashboard.gui.StaticWidget;
import edu.wpi.first.smartdashboard.properties.Property;
import edu.wpi.first.smartdashboard.robot.Robot;

@SuppressWarnings("serial")
public class ArrayBox extends StaticWidget {
	private ArrayList<String> values;
	private AutonTableModel model;
	JTable table;
	private JButton add;
	private JButton remove;
	private JButton save;

	public static final String NAME = "Auton Scripting";

	@Override
	public void init() {
		add = new JButton("Add");
		add.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				NewAutonEntryDialog dialog = new NewAutonEntryDialog();
				dialog.show(remove.getLocationOnScreen());
				if (!dialog.isCanceled()) {
					model.add(dialog.getScript());
					model.fireTableDataChanged();
				}
			}
		});

		remove = new JButton("Remove");
		remove.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (table.isEditing()) {
					table.getCellEditor().cancelCellEditing();
				}
				model.delete(table.getSelectedRow());
				model.fireTableDataChanged();
			}
		});

		save = new JButton("Save");
		save.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				Robot.getTable("AUTONOMOUS").putString("Auton Array",
						values.toString());
			}
		});

		values = new ArrayList<String>();

		model = new AutonTableModel();

		table = new JTable(model);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getTableHeader().setReorderingAllowed(true);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(0, 2));
		buttonPanel.add(add);
		buttonPanel.add(remove);

		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BorderLayout());
		controlPanel.add(buttonPanel, BorderLayout.NORTH);

		JButton load = new JButton("Load");
		load.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				InputStream is = this.getClass().getClassLoader()
						.getResourceAsStream("presets.txt");
				NewPresetLoadDialog dialog = new NewPresetLoadDialog(is);
				dialog.show(remove.getLocationOnScreen());
				if (!dialog.isCanceled()) {
					for (String line : dialog.getPreset())
						model.add(line);
					model.fireTableDataChanged();
				}
			}

		});
		JPanel savePanel = new JPanel();
		savePanel.setLayout(new GridLayout(2, 0));
		savePanel.add(load);
		savePanel.add(save);
		controlPanel.add(savePanel, BorderLayout.SOUTH);

		setLayout(new BorderLayout());
		JScrollPane tableScroll = new JScrollPane(table,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(tableScroll, BorderLayout.CENTER);
		add(controlPanel, BorderLayout.SOUTH);

		setPreferredSize(new Dimension(300, 200));
	}

	private class AutonTableModel extends AbstractTableModel {

		@Override
		public String getColumnName(int i) {
			if (i == 0) {
				return "Script";
			}
			return "ERROR";
		}

		@Override
		public int getRowCount() {
			return values.size();
		}

		@Override
		public int getColumnCount() {
			return 1;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return values.get(rowIndex);
		}

		@Override
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			values.set(rowIndex, value.toString());
		}

		public void delete(int index) {
			values.remove(index);
		}

		public void add(String script) {
			values.add(script);
		}
	}

	private class NewAutonEntryDialog extends JDialog {
		private JTextField script;
		private JButton addButton;
		private JButton cancelButton;

		boolean canceled = true;

		public NewAutonEntryDialog() {
			setTitle("New Autonomous Entry");
			setModal(true);
			setResizable(false);

			((JComponent) getContentPane()).setBorder(BorderFactory
					.createEmptyBorder(7, 7, 7, 7));

			setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();

			add(new JLabel("Command: "), c);
			c.gridx = 1;
			add(script = new JTextField(20), c);

			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new GridLayout(0, 2));
			buttonPanel.add(addButton = new JButton("Add"), c);
			addButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					canceled = false;
					dispose();
				}
			});
			getRootPane().setDefaultButton(addButton);

			buttonPanel.add(cancelButton = new JButton("Cancel"));
			cancelButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});
			c.gridx = 0;
			c.gridy = 1;
			c.gridwidth = 2;
			add(buttonPanel, c);

			pack();
		}

		public void show(Point center) {
			setLocation((int) (center.getX() - getWidth() / 2),
					(int) (center.getY() - getHeight() / 2));
			setVisible(true);
		}

		public boolean isCanceled() {
			return canceled;
		}

		public String getScript() {
			return script.getText();
		}
	}

	private class NewPresetLoadDialog extends JDialog {
		JList<String> list;
		private JButton addButton;
		private JButton cancelButton;
		private boolean canceled = true;
		Map<String, ArrayList<String>> presets;

		public NewPresetLoadDialog(InputStream is) {
			setTitle("New Autonomous Entry");
			setModal(true);
			setResizable(false);

			((JComponent) getContentPane()).setBorder(BorderFactory
					.createEmptyBorder(7, 7, 7, 7));

			InputStreamReader ir = new InputStreamReader(is);
			BufferedReader read = new BufferedReader(ir);
			String line;
			presets = new HashMap<String, ArrayList<String>>();
			try {
				line = read.readLine();
				while (line != null) {
					String name = line;
					line = read.readLine();
					ArrayList<String> temp = new ArrayList<String>();
					while (line != null && line.charAt(0) == '\t') {
						temp.add(line);
						line = read.readLine();
					}
					presets.put(name, temp);
				}
			} catch (IOException io) {
				ArrayList<String> temp = new ArrayList<String>();
				temp.add("Test");
				presets.put("ERROR", temp);
			}
			String[] listarr = presets.keySet().toArray(
					new String[presets.size()]);

			setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();

			list = new JList<String>(listarr);
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
			list.setVisibleRowCount(-1);

			JScrollPane listScroll = new JScrollPane(list);
			listScroll.setPreferredSize(new Dimension(250, 80));

			c.gridx = 1;
			add(listScroll);

			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new GridLayout(0, 2));
			buttonPanel.add(addButton = new JButton("Add"), c);
			addButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					canceled = false;
					dispose();
				}
			});
			getRootPane().setDefaultButton(addButton);

			buttonPanel.add(cancelButton = new JButton("Cancel"));
			cancelButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});
			c.gridx = 0;
			c.gridy = 1;
			c.gridwidth = 2;
			add(buttonPanel, c);

			pack();
		}

		public void show(Point center) {
			setLocation((int) (center.getX() - getWidth() / 2),
					(int) (center.getY() - getHeight() / 2));
			setVisible(true);
		}

		public boolean isCanceled() {
			return canceled;
		}

		public String[] getPreset() {
			ArrayList<String> arrList = presets.get(list.getSelectedValue());
			return arrList.toArray(new String[arrList.size()]);
		}
	}

	@Override
	public void propertyChanged(Property property) {
	}
}
