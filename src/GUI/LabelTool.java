package GUI;

import com.jgoodies.forms.layout.*;
import emrpaser.rule.ruleParserWithStats;
import exceptions.PropertyValueException;
import exceptions.ToothMapException;
import exceptions.ToothModifierException;
import exceptions.rpd.ClaspAssemblyException;
import exceptions.rpd.EdentulousTypeException;
import exceptions.rpd.RuleException;
import exceptions.rpd.ToothPosException;
import misc.PrintTestFile;
import misc.ToothMap;
import ontologies.*;
import ontologies.Descriptions.PropertyDescription;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.XSD;
import org.opencv.core.Mat;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import rpd.RPDPlan;
import rpd.SearchRPDPlan;
import rpd.components.*;
import rpd.conceptions.ClaspMaterial;
import rpd.conceptions.Position;
import rpd.oral.*;
import rpd.oral.Tooth;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static rpd.conceptions.Position.Mesial;

public class LabelTool {


	static {
		System.loadLibrary("RpdDesignLib");
		System.loadLibrary("opencv_java330");
	}

	private OntModel dental_ont = null;
	private OntModel mouth_ont = null;
	private Mouth mouth = null;
	private List<RPDPlan> mandibular_rpd_plans = null;
	private List<RPDPlan> maxillary_rpd_plans = null;
	private Descriptions des = null;
	private File owl_file = null;
	private File choosed_file = null;
	private File current_dir = new File("data");
	private File label_xml_file = null;
	private JTextPane emr_text = null;
	private JFrame frame;
	private JTextField choose_file_path;
	private JDialog value_dialog = null;
	private String value = null;
	private boolean value_valid = false;
	private boolean locate_tooth_map_when_reading = false;
	private Map<DatatypeProperty, JMenuItem> property_menu_map = new HashMap<DatatypeProperty, JMenuItem>();
	private JTable label_table = null;
	private List<PropertyLabel> label_list = new ArrayList<PropertyLabel>();
	private List<ToothMap> tooth_maps = null;
	private JPopupMenu label_menu = null;
	private JMenu tooth_map_menu = null;
	private JMenuItem remove_tooth_map = null;
	private JCheckBox chckbx_show_all_labels = null;
	private List<JMenuItem> tooth_map_items = new ArrayList<JMenuItem>();
	private String[] label_table_headers = {"牙位", "文本", "属性", "属性值", "说明", "标记"};
	private String is_missing_str = null;
	private RPDPlan current_rpd_plan = null;
	private JTree rpd_plan_tree = null;
	private JTree mandibular_plan_tree = null;
	private JTree maxillary_plan_tree = null;
	private JComboBox<Integer> plan_choice = null;
	private JPopupMenu rpd_plan_menu = null;
	private JPopupMenu mandibular_plan_menu = null;
	private JPopupMenu maxillary_plan_menu = null;
	private JFrame modifyFrame = new JFrame("可摘局部义齿设计方案");
	private boolean flagCreateModifyPlan = false;
	private boolean flagOriginalMaxilaryPlan = true;
	private boolean flagOriginalMandibulayPlan = true;
	public List<RPDPlan> maxillaryPlanBackup = new ArrayList<>();
	public List<RPDPlan> mandibularPlanBackup = new ArrayList<>();
//	private List<RPDPlan> maxillaryPlanBackup = new ArrayList<>();
//	private List<RPDPlan> mandibularPlanBackup = new ArrayList<>();
	//private ToothMap is_missing_map = null;
	private JPopupMenu tooth_menu = null;
	private JPopupMenu component_menu = null;
	/**
	 * Create the application.
	 *
	 * @throws PropertyValueException
	 * @throws IOException
	 */
	public LabelTool(File owl_file, File modifier_file)
			throws PropertyValueException, IOException,
			javax.xml.parsers.ParserConfigurationException, javax.xml.transform.TransformerException,
			emrpaser.exceptions.PropertyValueException {

		dental_ont = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		dental_ont.read("file:" + owl_file.getCanonicalPath());
//		org.apache.jena.ontology.Individual cc = dental_ont.getIndividual("http://www.semanticweb.org/msiip/ontologies/CDSSinRPD#combined_clasp_1");
//		org.apache.jena.ontology.ObjectProperty com_pos = dental_ont.getObjectProperty("component_position");
		des = new Descriptions(dental_ont, modifier_file);
		this.owl_file = owl_file;
		initialize();
	}

	public static native Mat getRpdDesign(OntModel ontModel, Mat mat);

	public static native Mat getRpdDesign(OntModel ontModel);

	/**
	 * Launch the application.
	 *
	 */
	public static void main(String[] args) {


		File owl_file = new File("res//base_template.owl");
		File modifier_file = new File("res//label_modifier_description.txt");

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LabelTool window = new LabelTool(owl_file, modifier_file);
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	class EachRowEditor implements TableCellEditor {
		protected Hashtable editors;

		protected TableCellEditor editor, defaultEditor;

		JTable table;

		/**
		 * Constructs a EachRowEditor. create default editor
		 *
		 * @see TableCellEditor
		 * @see DefaultCellEditor
		 */
		public EachRowEditor(JTable table) {
			this.table = table;
			editors = new Hashtable();
			defaultEditor = new DefaultCellEditor(new JTextField());
		}

		/**
		 * @param row
		 *            table row
		 * @param editor
		 *            table cell editor
		 */

		public void setEditorAt(int row, TableCellEditor editor) {
			editors.put(new Integer(row), editor);
		}

		public Component getTableCellEditorComponent(JTable table, Object value,
													 boolean isSelected, int row, int column) {
			//editor = (TableCellEditor)editors.get(new Integer(row));
			//if (editor == null) {
			//  editor = defaultEditor;
			//}
			return editor.getTableCellEditorComponent(table, value, isSelected,
					row, column);
		}

		public Object getCellEditorValue() {
			return editor.getCellEditorValue();
		}

		public boolean stopCellEditing() {
			return editor.stopCellEditing();
		}

		public void cancelCellEditing() {
			editor.cancelCellEditing();
		}

		public boolean isCellEditable(EventObject anEvent) {
			selectEditor((MouseEvent) anEvent);
			return editor.isCellEditable(anEvent);
		}

		public void addCellEditorListener(CellEditorListener l) {
			editor.addCellEditorListener(l);
		}

		public void removeCellEditorListener(CellEditorListener l) {
			editor.removeCellEditorListener(l);
		}

		public boolean shouldSelectCell(EventObject anEvent) {
			selectEditor((MouseEvent) anEvent);
			return editor.shouldSelectCell(anEvent);
		}

		protected void selectEditor(MouseEvent e) {
			int row;
			if (e == null) {
				row = table.getSelectionModel().getAnchorSelectionIndex();
			} else {
				row = table.rowAtPoint(e.getPoint());
			}
			editor = (TableCellEditor) editors.get(new Integer(row));
			if (editor == null) {
				editor = defaultEditor;
			}
		}
	}



	private static List<ToothMap> locateToothMaps(String content_str, int content_start) {

		List<ToothMap> res = new ArrayList<ToothMap>();
		Pattern tooth_map_pattern = Pattern.compile(ToothMap.tooth_map_regex);
		Matcher m = tooth_map_pattern.matcher(content_str);

		while (m.find()) {

			int start_index = m.start();
			int end_index = m.end();

			String tooth_map_str = content_str.substring(start_index, end_index);
			ToothMap tooth_map = new ToothMap(tooth_map_str, content_start + start_index, content_start + end_index);
			res.add(tooth_map);
		}
		return res;
	}

	/**
	 * Initialize the contents of the frame.
	 *
	 * @throws PropertyValueException
	 */
	private void initialize() throws PropertyValueException {

		frame = new JFrame();
		frame.setBounds(100, 100, 861, 688);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setLayout(new BorderLayout(5, 5));

		JPanel panel_west = new JPanel();
		frame.getContentPane().add(panel_west, BorderLayout.EAST);
//		panel_west.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		panel_west.setLayout(new BorderLayout());
		JPanel panel_label_title = new JPanel();
		panel_label_title.setLayout(new BorderLayout());
		JScrollPane label_scroll_pane = new JScrollPane();
		JTextField label_scroll_title = new JTextField("结构化电子病历");
		label_scroll_title.setHorizontalAlignment(JTextField.CENTER);
		label_scroll_title.setFont(new Font("微软雅黑", Font.BOLD, 28));
		label_scroll_title.setOpaque(false);
		panel_label_title.setPreferredSize(new Dimension(0, 80));
		panel_label_title.add(label_scroll_title, BorderLayout.CENTER);

		JTextField label_scroll_rights = new JTextField("口腔数字化医疗技术和材料国家工程实验室");
		label_scroll_rights.setHorizontalAlignment(JTextField.CENTER);
		label_scroll_rights.setHorizontalAlignment(JTextField.CENTER);
		label_scroll_rights.setFont(new Font("宋体", Font.PLAIN, 14));
		label_scroll_rights.setOpaque(false);

		panel_west.add(panel_label_title, BorderLayout.NORTH);
		panel_west.add(label_scroll_pane, BorderLayout.CENTER);
		panel_west.add(label_scroll_rights, BorderLayout.SOUTH);

		JCheckBox chckbx_show_all_labels = new JCheckBox("显示全部");
		this.chckbx_show_all_labels = chckbx_show_all_labels;

		label_table = new JTable();


		ListSelectionModel label_table_model = label_table.getSelectionModel();
		label_table_model.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {
//				label_table.validate();

				int selected_row = label_table.getSelectedRow();
				if (selected_row == -1)
					return;

				PropertyLabel label = label_list.get(selected_row);
				if (!chckbx_show_all_labels.isSelected())
					try {
						setEMRTextColor(label);
					} catch (BadLocationException e1) {
						e1.printStackTrace();
					}
			}
		});
		label_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		label_table.setRowHeight(50);
		JPopupMenu label_menu = new JPopupMenu();
		JMenu nt_tag = new JMenu("标记");
		this.label_menu = label_menu;

		Object[][] cell_data = null;
		DefaultTableModel model = new DefaultTableModel(cell_data, label_table_headers);
		label_table.setModel(model);
		label_table.getColumnModel().getColumn(0).setPreferredWidth(150);
		label_table.getColumnModel().getColumn(1).setPreferredWidth(250);
		label_table.getColumnModel().getColumn(2).setPreferredWidth(300);
		label_table.getColumnModel().getColumn(3).setPreferredWidth(150);
		label_table.getColumnModel().getColumn(4).setPreferredWidth(100);
		label_table.setPreferredScrollableViewportSize(new Dimension(950, 800));

		label_table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {

				int x = e.getX();
				int y = e.getY();

				if (e.isPopupTrigger())
					label_menu.show(e.getComponent(), x, y);

				int row = e.getY() / label_table.getRowHeight();
				label_table.setRowSelectionInterval(row, row);
				PropertyLabel label = label_list.get(row);
				try {
					if (!chckbx_show_all_labels.isSelected())
						setEMRTextColor(label);
				} catch (BadLocationException e1) {
					e1.printStackTrace();
				}
			}
		});
		label_table.setFont(new Font("微软雅黑", Font.PLAIN, 20));
		label_scroll_pane.setViewportView(label_table);

		label_table.add(label_menu);
		label_menu.add(nt_tag);

		for (LabelModifier modifier : LabelModifier.values()) {

			String name = modifier.name();
			String description = this.des.modifier_descriptions.get(modifier);
			String item_text = name;
			if (description != null)
				item_text = item_text + " ( " + this.des.modifier_descriptions.get(modifier) + " )";
			JMenuItem modifier_item = new JMenuItem(item_text);
			modifier_item.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseReleased(MouseEvent arg0) {

					DefaultTableModel label_table_model = (DefaultTableModel) label_table.getModel();
					int row_index = label_table.getSelectedRow();
					if (row_index == -1)
						return;
					label_table_model.setValueAt(name, row_index, 5);
					label_list.get(row_index).setModifier(modifier);
				}
			});

			modifier_item.setFont(new Font("微软雅黑", Font.PLAIN, 20));
			nt_tag.add(modifier_item);
		}

		JMenuItem delete = new JMenuItem("删除");
		delete.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {

				DefaultTableModel label_table_model = (DefaultTableModel) label_table.getModel();
				int row_index = label_table.getSelectedRow();
				if (row_index == -1)
					return;

				label_list.remove(row_index);
				label_table_model.removeRow(row_index);
				try {
					if (chckbx_show_all_labels.isSelected())
						setEMRTextColorAllLabels();
				} catch (BadLocationException e1) {
					e1.printStackTrace();
				}
			}
		});
		label_menu.add(delete);

		JMenuItem remove_tooth_map = new JMenuItem("取消牙位图");
		remove_tooth_map.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {

				DefaultTableModel label_table_model = (DefaultTableModel) label_table.getModel();
				int row_index = label_table.getSelectedRow();
				if (row_index == -1)
					return;

				PropertyLabel label = label_list.get(row_index);
				label.deleteToothMap();
				label_table_model.setValueAt("", row_index, 0);
			}
		});
		remove_tooth_map.setFont(new Font("微软雅黑", Font.PLAIN, 20));
		this.remove_tooth_map = remove_tooth_map;

		JMenu tooth_map_menu = new JMenu("牙位图");
		this.tooth_map_menu = tooth_map_menu;
		label_menu.add(tooth_map_menu);
		tooth_map_menu.add(remove_tooth_map);

		JPanel panel_center = new JPanel();
		frame.getContentPane().add(panel_center, BorderLayout.CENTER);
//		panel_center.setLayout(new GridLayout(1, 1, 0, 0));
		panel_center.setLayout(new BorderLayout(20, 0));
		JTextPane emr_text = new JTextPane();
		emr_text.setEditable(false);
		this.emr_text = emr_text;

		emr_text.setFont(new Font("微软雅黑", Font.PLAIN, 22));
		JScrollPane emr_scroll_pane = new JScrollPane(emr_text);
		JTextField emr_scroll_title = new JTextField("北京大学口腔医院电子病历");
		emr_scroll_title.setHorizontalAlignment(JTextField.CENTER);
		emr_scroll_title.setHorizontalAlignment(JTextField.CENTER);
		emr_scroll_title.setFont(new Font("微软雅黑", Font.BOLD, 28));
		emr_scroll_title.setOpaque(false);
		JPanel panel_emr_title = new JPanel() {
			protected void paintComponent(Graphics g) {
				ImageIcon icon1 = new ImageIcon("res\\icon1.png");
				ImageIcon icon2 = new ImageIcon("res\\icon2.png");
//				ImageIcon icon3 = new ImageIcon("res\\icon3.png");
				icon1.setImage(icon1.getImage().getScaledInstance(54,54, Image.SCALE_DEFAULT));
//				icon3.setImage(icon3.getImage().getScaledInstance(135,80, Image.SCALE_DEFAULT));
				icon2.setImage(icon2.getImage().getScaledInstance(54,54, Image.SCALE_DEFAULT));
				g.drawImage(icon1.getImage(), 100, 13, icon1.getIconWidth(), icon1.getIconHeight(), this);
//				g.drawImage(icon3.getImage(), 165, 0, icon3.getIconWidth(), icon3.getIconHeight(), this);
				g.drawImage(icon2.getImage(), 32, 13, icon2.getIconWidth(), icon2.getIconHeight(), this);
			}
		};
		panel_emr_title.setOpaque(false);
		panel_emr_title.setLayout(new BorderLayout());
		panel_emr_title.setPreferredSize(new Dimension(0, 80));
		panel_emr_title.add(emr_scroll_title, BorderLayout.CENTER);

		JTextField emr_scroll_rights = new JTextField("北京大学口腔医院和清华大学联合研发");
		emr_scroll_rights.setHorizontalAlignment(JTextField.CENTER);
		emr_scroll_rights.setHorizontalAlignment(JTextField.CENTER);
		emr_scroll_rights.setFont(new Font("宋体", Font.PLAIN, 14));
		emr_scroll_rights.setOpaque(false);

		panel_center.add(panel_emr_title, BorderLayout.NORTH);
		panel_center.add(emr_scroll_pane, BorderLayout.CENTER);
		panel_center.add(emr_scroll_rights, BorderLayout.SOUTH);

		JPanel panel_north = new JPanel();
		frame.getContentPane().add(panel_north, BorderLayout.NORTH);

		JButton choose_file = new JButton("打开文件");
		choose_file.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {

				if (arg0.getButton() != MouseEvent.BUTTON1)
					return;

				JFileChooser file_chooser = new JFileChooser();
				if (current_dir != null)
					file_chooser.setCurrentDirectory(current_dir);
				file_chooser.showOpenDialog(null);
				File choosed_file_tmp = file_chooser.getSelectedFile();

				if (choosed_file_tmp == null)
					return;
				choosed_file = choosed_file_tmp;

				try {
					if (!choosed_file.getCanonicalPath().endsWith(".txt"))
						return;
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				if (choosed_file != null) {

					try {

						chckbx_show_all_labels.setSelected(false);

						choose_file_path.setText(choosed_file.getCanonicalPath());
						readEMRFile(choosed_file);
						((DefaultTableModel) label_table.getModel()).setRowCount(0);
//
//						File dir = choosed_file.getParentFile();
//						current_dir = dir;
//
//						DefaultTableModel label_table_model = (DefaultTableModel) label_table.getModel();
//						label_table_model.setDataVector(null, label_table_headers);
//
//						label_list.clear();
//						mouth_ont = null;
//						mandibular_rpd_plans = null;
//						maxillary_rpd_plans = null;
//						current_rpd_plan = null;
//						is_missing_str = null;
//
//						String input_file_path = choosed_file.getCanonicalPath();
//						int dot_index = input_file_path.lastIndexOf(".");
//						String xml_file_path = input_file_path.substring(0, dot_index) + ".xml";
//						File xml_file = new File(xml_file_path);
//						label_xml_file = null;
//
//						if (!xml_file.exists()) {
//							File owl_file = new File("res//base_template.owl");
//							File modifier_file = new File("res//label_modifier_description.txt");
//							File res_dir = new File("res");
//							File data_dir = new File("data");
//							File excel_file = new File(res_dir.getCanonicalPath() + "\\ontology_definition_1209.xlsx");
//							File rule_file = new File(res_dir.getCanonicalPath() + "\\rules_all_with_value_20170116.txt");
//							File general_regex_file = new File(res_dir.getCanonicalPath() + "\\general_regex_with_value.txt");
//							File all_sen_file = new File(data_dir.getCanonicalPath() + "\\all_sen_checking.txt");
//							File unmatched_file = new File(data_dir.getCanonicalPath() + "\\emr_data_ummatched.txt");
//
//							ruleParserWithStats p = new ruleParserWithStats(rule_file, general_regex_file, owl_file, modifier_file, excel_file);
//							p.parseFile(choosed_file, xml_file);
//							xml_file = new File(xml_file_path);
//						}
//
//						label_xml_file = xml_file;
//						label_xml_file = null;
//						readLabelsFromXml(label_xml_file);

					} catch (IOException e) {
						e.printStackTrace();
//					} catch (ParserConfigurationException e) {
//						e.printStackTrace();
//					} catch (SAXException e) {
//						e.printStackTrace();
//					} catch (PropertyValueException e) {
//						e.printStackTrace();
					} catch (BadLocationException e) {
						e.printStackTrace();
//					} catch (emrpaser.exceptions.PropertyValueException e) {
//						e.printStackTrace();
//					} catch (javax.xml.transform.TransformerException e) {
//						e.printStackTrace();
					}
				}
			}

			public void readEMRFile(File emr_file) throws IOException, BadLocationException {

				StringBuilder s = new StringBuilder();
				BufferedReader r = null;
				r = new BufferedReader(new InputStreamReader(new FileInputStream(emr_file), "utf8"));

				String line = null;
				while ((line = r.readLine()) != null)
					s.append(line + "\n");
				r.close();
				SimpleAttributeSet black = new SimpleAttributeSet();
				StyleConstants.setForeground(black, Color.BLACK);
				DefaultStyledDocument doc = (DefaultStyledDocument) emr_text.getDocument();
				doc.replace(0, doc.getLength(), s.toString(), black);
				emr_text.setCaretPosition(0);
				tooth_maps = locateToothMaps(s.toString(), 0);
				updateToothMapItems();
			}
		});

		choose_file_path = new JTextField();
		choose_file_path.setEditable(false);
		choose_file_path.setColumns(80);
		panel_north.setLayout(new FormLayout(new ColumnSpec[]{
				FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,
				ColumnSpec.decode("123px"),
				FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,
				new ColumnSpec(ColumnSpec.FILL, Sizes.bounded(Sizes.PREFERRED, Sizes.constant("200px", true), Sizes.constant("500px", true)), 0),
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,},
				new RowSpec[]{
						FormSpecs.LINE_GAP_ROWSPEC,
						RowSpec.decode("40px"),}));
		panel_north.add(choose_file, "2, 2, left, center");
		panel_north.add(choose_file_path, "4, 2, center, center");

		JButton save = new JButton("保存");
		save.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					File file = choosed_file;
					if (file == null)
						return;
					String input_file_path = file.getCanonicalPath();
					int dot_index = input_file_path.lastIndexOf(".");
					String xml_file_path = input_file_path.substring(0, dot_index) + ".xml";
					updateLabelList();
					writeLabelsAsXml(new File(xml_file_path));
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (ParserConfigurationException e1) {
					e1.printStackTrace();
				} catch (PropertyValueException e1) {
					e1.printStackTrace();
				} catch (TransformerException e1) {
					e1.printStackTrace();
				}
			}
		});
		panel_north.add(save, "6, 2");

		chckbx_show_all_labels.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent event) {

				if (chckbx_show_all_labels.isSelected()) {

					try {
						setEMRTextColorAllLabels();
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
				} else {

					try {
						resetEMRText();
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
				}
			}
		});


		JButton txt2xml_button = new JButton("实例化病例");
		txt2xml_button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				try {
					File dir = choosed_file.getParentFile();
					current_dir = dir;

					DefaultTableModel label_table_model = (DefaultTableModel) label_table.getModel();
					label_table_model.setDataVector(null, label_table_headers);

					label_list.clear();
					mouth_ont = null;
					mandibular_rpd_plans = null;
					maxillary_rpd_plans = null;
					current_rpd_plan = null;
					is_missing_str = null;

					String input_file_path = choosed_file.getCanonicalPath();
					int dot_index = input_file_path.lastIndexOf(".");
					String xml_file_path = input_file_path.substring(0, dot_index) + ".xml";
					File xml_file = new File(xml_file_path);
					label_xml_file = null;

					if (!xml_file.exists()) {
						File owl_file = new File("res//base_template.owl");
						File modifier_file = new File("res//label_modifier_description.txt");
						File res_dir = new File("res");
						File excel_file = new File(res_dir.getCanonicalPath() + "\\ontology_definition_1209.xlsx");
						File rule_file = new File(res_dir.getCanonicalPath() + "\\rules_all_with_value_20170116.txt");
						File general_regex_file = new File(res_dir.getCanonicalPath() + "\\general_regex_with_value.txt");
						ruleParserWithStats p = new ruleParserWithStats(rule_file, general_regex_file, owl_file, modifier_file, excel_file);
						p.parseFile(choosed_file, xml_file);
						xml_file = new File(xml_file_path);
					}

					label_xml_file = xml_file;
					readLabelsFromXml(xml_file);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				} catch (SAXException e) {
					e.printStackTrace();
				} catch (PropertyValueException e) {
					e.printStackTrace();
				} catch (emrpaser.exceptions.PropertyValueException e) {
					e.printStackTrace();
				} catch (javax.xml.transform.TransformerException e) {
					e.printStackTrace();
				}
			}
		});

		JButton gen_design_button = new JButton("生成设计方案");
		gen_design_button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {

				if (label_xml_file == null)
					return;

				if (is_missing_str == null)
					return;

				try {
					mouth_ont = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
					mouth_ont.read("file:" + owl_file.getCanonicalPath());
					Instantialize.convertXmlToOnt(mouth_ont, label_xml_file);
					mouth = new Mouth(mouth_ont);
					//rpd_plans = BeamSearch.searchMandibular(mouth);
					mandibular_rpd_plans = SearchRPDPlan.searchMandibular(mouth);
					maxillary_rpd_plans = SearchRPDPlan.searchMaxillary(mouth);
//					int design_count = 0;
//					for (RPDPlan plan:mandibular_rpd_plans) {
//						design_count++;
//						OntModel design_ont = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
//						design_ont.read("file:" + owl_file.getCanonicalPath());
//						planToOwl(plan, design_ont);
//						FileWriter out = new FileWriter("res//" + design_count+"_owl.owl");
//						design_ont.write(out);
//						imwrite(design_count + "mandibular_design.png", getRpdDesign(design_ont));
//					}
//
//					for (RPDPlan plan:maxillary_rpd_plans) {
//						design_count++;
//						OntModel design_ont = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
//						design_ont.read("file:" + owl_file.getCanonicalPath());
//						planToOwl(plan, design_ont);
//						FileWriter out = new FileWriter("res//" + design_count+"_owl.owl");
//						design_ont.write(out);
//						imwrite(design_count + "maxillary_design.png", getRpdDesign(design_ont));
//					}

					@SuppressWarnings("unused")
					List<RPDPlan> plans_buffer = new ArrayList<>();
					if (mandibular_rpd_plans != null) plans_buffer.addAll(mandibular_rpd_plans);
					if (maxillary_rpd_plans != null) plans_buffer.addAll(maxillary_rpd_plans);
					drawRPDPlans();
//					drawPlanExplanation();

//					drawRPDPlans(mandibular_rpd_plans);
//					drawRPDPlans(maxillary_rpd_plans);
//					showRPDPlans();
				} catch (ParserConfigurationException | SAXException | IOException | ToothMapException
						| ToothModifierException | PropertyValueException e) {
					e.printStackTrace();
				} catch (RuleException | ClaspAssemblyException | ToothPosException | EdentulousTypeException e) {
					e.printStackTrace();
				}
			}
		});
		panel_north.add(txt2xml_button, "8, 2");
		panel_north.add(gen_design_button, "10, 2");
		chckbx_show_all_labels.setFont(new Font("微软雅黑", Font.PLAIN, 18));
		panel_north.add(chckbx_show_all_labels, "12, 2");

		JPopupMenu property_menu = new JPopupMenu();
		property_menu.setFont(new Font("微软雅黑", Font.PLAIN, 22));

		addEMRTextPopup(emr_text, property_menu);
		addPropertyMenuItems(property_menu);

		initRPDPlanPopupMenu();
		initToothPopupMenu();
		initComponentPopupMenu();
	}

	private void initRPDPlanPopupMenu() {

		this.rpd_plan_menu = new JPopupMenu();
		JMenu zone3_menu = new JMenu("zone3");
		JMenu zone4_menu = new JMenu("zone4");
		zone3_menu.setFont(new Font("微软雅黑", Font.PLAIN, 18));
		zone4_menu.setFont(new Font("微软雅黑", Font.PLAIN, 18));
		this.rpd_plan_menu.add(zone3_menu);
		this.rpd_plan_menu.add(zone4_menu);
		for (int i = 1; i <= 8; i++) {

			int tooth_num = i;
			JMenuItem add_tooth_item_zone3 = new JMenuItem("tooth" + "3" + String.valueOf(i));
			add_tooth_item_zone3.setFont(new Font("微软雅黑", Font.PLAIN, 18));
			add_tooth_item_zone3.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseReleased(MouseEvent arg0) {
					int plan_index = (Integer) (plan_choice.getSelectedItem());
					Tooth tooth = current_rpd_plan.getMouth().getTooth(3, tooth_num);
					current_rpd_plan.addTooth(tooth);
					JTree new_rpd_plan_tree = buildRPDPlanTree(current_rpd_plan, plan_index);
					if (new_rpd_plan_tree != null) {
						rpd_plan_tree.setModel(new_rpd_plan_tree.getModel());
						TreeNode root = (TreeNode) rpd_plan_tree.getModel().getRoot();
						expandAll(rpd_plan_tree, new TreePath(root), true);
					}
				}
			});
			zone3_menu.add(add_tooth_item_zone3);

			JMenuItem add_tooth_item_zone4 = new JMenuItem("tooth" + "4" + String.valueOf(i));
			add_tooth_item_zone4.setFont(new Font("微软雅黑", Font.PLAIN, 18));
			add_tooth_item_zone4.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseReleased(MouseEvent arg0) {
					int plan_index = (Integer) (plan_choice.getSelectedItem());
					Tooth tooth = current_rpd_plan.getMouth().getTooth(4, tooth_num);
					current_rpd_plan.addTooth(tooth);
					JTree new_rpd_plan_tree = buildRPDPlanTree(current_rpd_plan, plan_index);
					if (new_rpd_plan_tree != null) {
						rpd_plan_tree.setModel(new_rpd_plan_tree.getModel());
						TreeNode root = (TreeNode) rpd_plan_tree.getModel().getRoot();
						expandAll(rpd_plan_tree, new TreePath(root), true);
					}
				}
			});
			zone4_menu.add(add_tooth_item_zone4);
		}
	}

	private void initToothPopupMenu() {

		this.tooth_menu = new JPopupMenu();
		JMenuItem delete_tooth_item = new JMenuItem("删除");
		delete_tooth_item.setFont(new Font("微软雅黑", Font.PLAIN, 18));
		this.tooth_menu.add(delete_tooth_item);
		delete_tooth_item.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseReleased(MouseEvent arg0) {

				TreePath path = rpd_plan_tree.getSelectionPath();
				DefaultMutableTreeNode selected_node = (DefaultMutableTreeNode) path.getLastPathComponent();
				Object user_obj = selected_node.getUserObject();
				int plan_index = (Integer) (plan_choice.getSelectedItem());
				if (user_obj.getClass().equals(Tooth.class)) {
					Tooth tooth = (Tooth) user_obj;
					current_rpd_plan.removeTooth(tooth);
					JTree new_rpd_plan_tree = buildRPDPlanTree(current_rpd_plan, plan_index);
					if (new_rpd_plan_tree != null) {
						rpd_plan_tree.setModel(new_rpd_plan_tree.getModel());
						TreeNode root = (TreeNode) rpd_plan_tree.getModel().getRoot();
						expandAll(rpd_plan_tree, new TreePath(root), true);
					}
				}
			}
		});
	}

	private void initComponentPopupMenu() {

		this.component_menu = new JPopupMenu();
		JMenuItem delete_component_item = new JMenuItem("删除");
		delete_component_item.setFont(new Font("微软雅黑", Font.PLAIN, 18));
		this.component_menu.add(delete_component_item);

		delete_component_item.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseReleased(MouseEvent arg0) {

				TreePath path = rpd_plan_tree.getSelectionPath();
				DefaultMutableTreeNode selected_node = (DefaultMutableTreeNode) path.getLastPathComponent();
				Object user_obj = selected_node.getUserObject();
				int plan_index = (Integer) (plan_choice.getSelectedItem());
				if (rpd.components.Component.class.isInstance(user_obj)) {
					rpd.components.Component component = (rpd.components.Component) user_obj;
					current_rpd_plan.removeComponent(component);
					JTree new_rpd_plan_tree = buildRPDPlanTree(current_rpd_plan, plan_index);
					if (new_rpd_plan_tree != null) {
						rpd_plan_tree.setModel(new_rpd_plan_tree.getModel());
						TreeNode root = (TreeNode) rpd_plan_tree.getModel().getRoot();
						expandAll(rpd_plan_tree, new TreePath(root), true);
					}
				}
			}
		});
	}

	public BufferedImage addTextToImage(BufferedImage image, String text) {

		Graphics g = image.getGraphics();
		g.setFont(new Font("微软雅黑", Font.PLAIN, 30));
		g.setColor(Color.BLACK);
		g.drawString("病历："+text, 30, 50);
		g.dispose();
//		ImageIO.write(image, "png", new File("test.png"));

		return image;
	}

	private void drawRPDPlans() throws java.io.IOException, exceptions.rpd.RuleException {
		if ((this.mandibular_rpd_plans == null || this.mandibular_rpd_plans.size() == 0)
				&& (this.maxillary_rpd_plans == null || this.maxillary_rpd_plans.size() == 0)) {
			return;
		}
		JDialog design_dialog = new JDialog(this.frame, "可摘局部义齿设计方案");
		JPanel rpd_plan_panel = new JPanel(new BorderLayout());
		int total_height = 0;
		int total_width = 0;
		int line_height = 400;
		int print_height = 50;

		String input_file_path = choosed_file.getCanonicalPath();
		int dot_index = input_file_path.lastIndexOf(".");
		int gang_index = input_file_path.lastIndexOf("\\");
		String txt_file_name = input_file_path.substring(gang_index+1, dot_index);

		int mandibular_width = 0;
		if (!(this.mandibular_rpd_plans == null || this.mandibular_rpd_plans.size() == 0)) {

			generateAndSaveRPDPlanPicture(this.mandibular_rpd_plans, txt_file_name);
			JPanel mandibular_plan_panel = new JPanel(new FlowLayout());

			total_height += line_height + print_height;
			for (int i = 1; i <= 3; i++) {
				String picture_name = "out//picture//" + txt_file_name + "_mandibular_RPD_design_" + i + ".png";
				ImageIcon im = new ImageIcon(picture_name);
				if (im.getIconWidth() == -1) {
					continue;
				}

				BufferedImage buffered_pic = new BufferedImage(
						im.getIconWidth(),
						im.getIconHeight(),
						BufferedImage.TYPE_INT_RGB
				);
				Graphics g_pic = buffered_pic.createGraphics();
				im.paintIcon(null, g_pic, 0,0);
				g_pic.dispose();

				ImageIcon base = new ImageIcon("res//base.png");
				BufferedImage buffered_base = new BufferedImage(
						base.getIconWidth(),
						base.getIconHeight(),
						BufferedImage.TYPE_INT_RGB
				);
				Graphics g_base = buffered_base.createGraphics();
				base.paintIcon(null, g_base, 0, 0);
				g_base.dispose();

				BufferedImage im_print = addTextToImage(buffered_pic, txt_file_name);
				BufferedImage base_print = addTextToImage(buffered_base, txt_file_name);
				String im_print_name = "out//picture//" + txt_file_name + "_mandibular_RPD_design_" + i + "_print.png";
				String base_print_name = "res//base_print.png";

				File im_print_file = new File(im_print_name);
				File base_print_file = new File(base_print_name);
				ImageIO.write(im_print, "png", im_print_file);
				ImageIO.write(base_print, "png", base_print_file);

				int src_im_height = im.getIconHeight();
				int src_im_width = im.getIconWidth();
				double scale_factor = (double) line_height / (double) src_im_height;
				double rescale_height = src_im_height * scale_factor;
				double rescale_width = src_im_width * scale_factor;
				int dest_im_width = (int) rescale_width;
				int dest_im_height = (int) rescale_height;
				im.setImage(im.getImage().getScaledInstance(dest_im_width, dest_im_height, Image.SCALE_DEFAULT));


				JLabel rpd_plan_label = new JLabel();
				rpd_plan_label.setSize(dest_im_width, dest_im_height);
				rpd_plan_label.setIcon(im);
				Border label_border = BorderFactory.createLineBorder(Color.BLACK);
				rpd_plan_label.setBorder(label_border);

				JButton print_button = new JButton("打印");
				print_button.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseReleased(MouseEvent arg0) {
//						String[] imgFileNameList = {im_print_name, base_print_name};
//						new PrintImage().drawImage(fileNameList, 1);
						try {
							new PrintTestFile().print(choosed_file, im_print_name, base_print_name, txt_file_name, 1);
						}
						catch (IOException ie) {
							ie.printStackTrace();
						}

					}
				});

				JPanel cur_plan_panel = new JPanel(new BorderLayout());
				cur_plan_panel.add(rpd_plan_label, BorderLayout.NORTH);
				cur_plan_panel.add(print_button, BorderLayout.SOUTH);
				mandibular_plan_panel.add(cur_plan_panel);
				mandibular_width += dest_im_width;
			}

			mandibular_plan_panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
					"下颌设计方案图示", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION,
					new Font("微软雅黑", Font.BOLD, 20)));

//			JPanel mandibular_title_panel = new JPanel(new BorderLayout());
//			JTextField mandibular_title = new JTextField("下颌设计方案");
//			mandibular_title.setHorizontalAlignment(JTextField.CENTER);
//			mandibular_title.setFont(new Font("微软雅黑", Font.BOLD, 20));
//			mandibular_title.setEditable(false);
//			mandibular_title_panel.setPreferredSize(new Dimension(0, 35));
//			mandibular_title_panel.add(mandibular_title, BorderLayout.CENTER);

			JPanel mandibular_all_panel = new JPanel(new BorderLayout());
//			mandibular_all_panel.add(mandibular_title_panel, BorderLayout.NORTH);
			mandibular_all_panel.add(mandibular_plan_panel, BorderLayout.CENTER);
			int v = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
			int h = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
			JScrollPane mandibular_scroll_pane = new JScrollPane(showMandibularPlans(), v, h);
			mandibular_scroll_pane.setPreferredSize(new Dimension(670, line_height));
//			mandibular_scroll_pane.setMinimumSize(new Dimension(670, line_height));
//			mandibular_all_panel.add(showMandibularPlans(), BorderLayout.EAST);
			mandibular_all_panel.add(mandibular_scroll_pane, BorderLayout.EAST);
//			rpd_plan_panel.setSize(total_width, line_height);
			rpd_plan_panel.add(mandibular_all_panel, BorderLayout.CENTER);
//			design_dialog.add(rpd_plan_panel);
		}

		int maxillary_width = 0;
		if (!(this.maxillary_rpd_plans == null || this.maxillary_rpd_plans.size() == 0)) {
			generateAndSaveRPDPlanPicture(this.maxillary_rpd_plans, txt_file_name);
			JPanel maxillary_plan_panel = new JPanel(new FlowLayout());

			total_height += line_height + print_height;
			total_width = 0;
			for (int i = 1; i <= 3; i++) {
				String picture_name = "out//picture//" + txt_file_name + "_maxillary_RPD_design_" + i + ".png";
				ImageIcon im = new ImageIcon(picture_name);
				if (im.getIconWidth() == -1) {
					continue;
				}

				BufferedImage buffered_pic = new BufferedImage(
						im.getIconWidth(),
						im.getIconHeight(),
						BufferedImage.TYPE_INT_RGB
				);
				Graphics g_pic = buffered_pic.createGraphics();
				im.paintIcon(null, g_pic, 0,0);
				g_pic.dispose();

				ImageIcon base = new ImageIcon("res//base.png");
				BufferedImage buffered_base = new BufferedImage(
						base.getIconWidth(),
						base.getIconHeight(),
						BufferedImage.TYPE_INT_RGB
				);
				Graphics g_base = buffered_base.createGraphics();
				base.paintIcon(null, g_base, 0, 0);
				g_base.dispose();

				BufferedImage im_print = addTextToImage(buffered_pic, txt_file_name);
				BufferedImage base_print = addTextToImage(buffered_base, txt_file_name);
				String im_print_name = "out//picture//" + txt_file_name + "_maxillary_RPD_design_" + i + "_print.png";
				String base_print_name = "res//base_print.png";

				File im_print_file = new File(im_print_name);
				File base_print_file = new File(base_print_name);
				ImageIO.write(im_print, "png", im_print_file);
				ImageIO.write(base_print, "png", base_print_file);
				int src_im_height = im.getIconHeight();
				int src_im_width = im.getIconWidth();
				double scale_factor = (double) line_height / (double) src_im_height;
				double rescale_height = src_im_height * scale_factor;
				double rescale_width = src_im_width * scale_factor;
				int dest_im_width = (int) rescale_width;
				int dest_im_height = (int) rescale_height;
				im.setImage(im.getImage().getScaledInstance(dest_im_width, dest_im_height, Image.SCALE_DEFAULT));
				JLabel rpd_plan_label = new JLabel();
				rpd_plan_label.setSize(dest_im_width, dest_im_height);
				rpd_plan_label.setIcon(im);
				Border label_border = BorderFactory.createLineBorder(Color.BLACK);
				rpd_plan_label.setBorder(label_border);

				JButton print_button = new JButton("打印");
				print_button.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseReleased(MouseEvent arg0) {
//						String[] imgFileNameList = {im_print_name, base_print_name};
//						new PrintImage().drawImage(fileNameList, 1);
						try {
							new PrintTestFile().print(choosed_file, im_print_name, base_print_name, txt_file_name, 1);
						}
						catch (IOException ie) {
							ie.printStackTrace();
						}
					}
				});

				JPanel cur_plan_panel = new JPanel(new BorderLayout());
				cur_plan_panel.add(rpd_plan_label, BorderLayout.NORTH);
				cur_plan_panel.add(print_button, BorderLayout.SOUTH);
				maxillary_plan_panel.add(cur_plan_panel);
				maxillary_width += dest_im_width;
			}
			maxillary_plan_panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
					"上颌设计方案图示", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION,
					new Font("微软雅黑", Font.BOLD, 20)));


//			JPanel maxillary_title_panel = new JPanel(new BorderLayout());
//			JTextField maxillary_title = new JTextField("上颌设计方案");
//			maxillary_title.setHorizontalAlignment(JTextField.CENTER);
//			maxillary_title.setFont(new Font("微软雅黑", Font.BOLD, 20));
//			maxillary_title.setEditable(false);
//			maxillary_title_panel.setPreferredSize(new Dimension(0, 35));
//			maxillary_title_panel.add(maxillary_title, BorderLayout.CENTER);

			JPanel maxillary_all_panel = new JPanel(new BorderLayout());
//			maxillary_all_panel.add(maxillary_title_panel, BorderLayout.NORTH);
			maxillary_all_panel.add(maxillary_plan_panel, BorderLayout.CENTER);
			int v = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
			int h = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
			JScrollPane maxillary_scroll_pane = new JScrollPane(showMaxillaryPlans(), v, h);
			maxillary_scroll_pane.setPreferredSize(new Dimension(670, line_height));
			maxillary_all_panel.add(maxillary_scroll_pane, BorderLayout.EAST);
//			rpd_plan_panel.setSize(total_width, line_height);
			rpd_plan_panel.add(maxillary_all_panel, BorderLayout.NORTH);
//			design_dialog.add(rpd_plan_panel);
		}


		JTextField design_rights = new JTextField("北京大学口腔医院和清华大学联合研发");
		design_rights.setHorizontalAlignment(JTextField.CENTER);
		design_rights.setHorizontalAlignment(JTextField.CENTER);
		design_rights.setFont(new Font("宋体", Font.PLAIN, 14));
		design_rights.setOpaque(false);
//		rpd_plan_panel.add(design_rights, BorderLayout.SOUTH);

		JButton modifyPlan = new JButton("修改方案");

		modifyPlan.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				modifyPlan();
			}
		});
		JPanel buttomPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttomPane.add(modifyPlan);
		buttomPane.add(design_rights);
		rpd_plan_panel.add(buttomPane, BorderLayout.SOUTH);
		design_dialog.add(rpd_plan_panel);

		total_width = (mandibular_width > maxillary_width) ? mandibular_width : maxillary_width;
		total_width += 750;
		total_height += 108;
//		rpd_plan_panel.setSize(total_width, total_height);
		design_dialog.setSize(total_width, total_height);
		design_dialog.setLocationRelativeTo(null);
		design_dialog.setVisible(true);
	}


	private void modifyPlan() {

//		JScrollPane MaxillaryPlans = new JPanel(new BorderLayout());
//		JDialog modifyDialog = new JDialog(this.frame, "可摘局部义齿设计方案");

		GridLayout layout= new GridLayout(0,1);
		layout.setVgap(15);
		modifyFrame.setLayout(layout);
		modifyFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		if(flagCreateModifyPlan == true && modifyFrame.getComponentCount()>0 ){
			try {
				modifyFrame.getContentPane().removeAll();
//				modifyFrame.repaint();
			} catch (NumberFormatException a) {
				a.printStackTrace();
			}
		}

		if(this.maxillary_rpd_plans != null && this.maxillary_rpd_plans.size() != 0){
			try {
				JScrollPane MaxillaryPlans = new JScrollPane(showMaxillaryPlansAsTable());
				MaxillaryPlans.createHorizontalScrollBar();
				MaxillaryPlans.createVerticalScrollBar();
				modifyFrame.add(MaxillaryPlans);
				flagCreateModifyPlan = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if(this.mandibular_rpd_plans != null && this.mandibular_rpd_plans.size() != 0){
			try {
				JScrollPane MandibularPlans = new JScrollPane(showMandibularPlansAsTable());
//				JScrollPane MandibularPlans = showMandibularPlansAsTable();
				MandibularPlans.createHorizontalScrollBar();
				MandibularPlans.createVerticalScrollBar();
				modifyFrame.add(MandibularPlans);
				flagCreateModifyPlan = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		modifyFrame.setSize(1690,1000);
		modifyFrame.setLocationRelativeTo(null);
		modifyFrame.setVisible(true);

	}


	private void drawNewMandibularRPDPlans() throws java.io.IOException, exceptions.rpd.RuleException {
		if ((this.mandibular_rpd_plans == null || this.mandibular_rpd_plans.size() == 0))
			return;
		JFrame design_dialog = new JFrame("可摘局部义齿设计方案-下颌");
		design_dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		JPanel rpd_plan_panel = new JPanel(new BorderLayout());
		int total_height = 0;
		int total_width = 0;
		int line_height = 400;
		int print_height = 50;

		String input_file_path = choosed_file.getCanonicalPath();
		int dot_index = input_file_path.lastIndexOf(".");
		int gang_index = input_file_path.lastIndexOf("\\");
		String txt_file_name = input_file_path.substring(gang_index+1, dot_index);

		int mandibular_width = 0;
		if (!(this.mandibular_rpd_plans == null || this.mandibular_rpd_plans.size() == 0)) {

			generateAndSaveRPDPlanPicture(this.mandibular_rpd_plans, txt_file_name);
			JPanel mandibular_plan_panel = new JPanel(new FlowLayout());
			total_height += line_height + print_height;
			for (int i = 1; i <= 3; i++) {
				String picture_name = "out//picture//" + txt_file_name + "_mandibular_RPD_design_" + i + ".png";
				ImageIcon im = new ImageIcon(picture_name);
				if (im.getIconWidth() == -1) {
					continue;
				}

				BufferedImage buffered_pic = new BufferedImage(
						im.getIconWidth(),
						im.getIconHeight(),
						BufferedImage.TYPE_INT_RGB
				);
				Graphics g_pic = buffered_pic.createGraphics();
				im.paintIcon(null, g_pic, 0,0);
				g_pic.dispose();

				ImageIcon base = new ImageIcon("res//base.png");
				BufferedImage buffered_base = new BufferedImage(
						base.getIconWidth(),
						base.getIconHeight(),
						BufferedImage.TYPE_INT_RGB
				);
				Graphics g_base = buffered_base.createGraphics();
				base.paintIcon(null, g_base, 0, 0);
				g_base.dispose();

				BufferedImage im_print = addTextToImage(buffered_pic, txt_file_name);
				BufferedImage base_print = addTextToImage(buffered_base, txt_file_name);
				String im_print_name = "out//picture//" + txt_file_name + "_mandibular_RPD_design_" + i + "_print.png";
				String base_print_name = "res//base_print.png";

				File im_print_file = new File(im_print_name);
				File base_print_file = new File(base_print_name);
				ImageIO.write(im_print, "png", im_print_file);
				ImageIO.write(base_print, "png", base_print_file);

				int src_im_height = im.getIconHeight();
				int src_im_width = im.getIconWidth();
				double scale_factor = (double) line_height / (double) src_im_height;
				double rescale_height = src_im_height * scale_factor;
				double rescale_width = src_im_width * scale_factor;
				int dest_im_width = (int) rescale_width;
				int dest_im_height = (int) rescale_height;
				im.setImage(im.getImage().getScaledInstance(dest_im_width, dest_im_height, Image.SCALE_DEFAULT));


				JLabel rpd_plan_label = new JLabel();
				rpd_plan_label.setSize(dest_im_width, dest_im_height);
				rpd_plan_label.setIcon(im);
				Border label_border = BorderFactory.createLineBorder(Color.BLACK);
				rpd_plan_label.setBorder(label_border);

				JButton print_button = new JButton("打印");
				print_button.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseReleased(MouseEvent arg0) {
//						String[] imgFileNameList = {im_print_name, base_print_name};
//						new PrintImage().drawImage(fileNameList, 1);
						try {
							new PrintTestFile().print(choosed_file, im_print_name, base_print_name, txt_file_name, 1);
						}
						catch (IOException ie) {
							ie.printStackTrace();
						}

					}
				});

				JPanel cur_plan_panel = new JPanel(new BorderLayout());
				cur_plan_panel.add(rpd_plan_label, BorderLayout.NORTH);
				cur_plan_panel.add(print_button, BorderLayout.SOUTH);
				mandibular_plan_panel.add(cur_plan_panel);
				mandibular_width += dest_im_width;
			}

			mandibular_plan_panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
					"下颌设计方案图示", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION,
					new Font("微软雅黑", Font.BOLD, 20)));
			JPanel mandibular_all_panel = new JPanel(new BorderLayout());
			mandibular_all_panel.add(mandibular_plan_panel, BorderLayout.CENTER);
			rpd_plan_panel.add(mandibular_all_panel, BorderLayout.CENTER);
		}
		total_width = mandibular_width;
		total_width += 750;
		total_height += 108;
//		rpd_plan_panel.setSize(total_width, total_height);
		design_dialog.add(rpd_plan_panel);
		design_dialog.setSize(total_width, total_height);
		design_dialog.setLocationRelativeTo(null);
		design_dialog.setVisible(true);
	}


	private void drawNewMaxillaryRPDPlans()throws java.io.IOException, exceptions.rpd.RuleException {
		if ((this.maxillary_rpd_plans == null || this.maxillary_rpd_plans.size() == 0))
			return;
		JFrame design_dialog = new JFrame("可摘局部义齿设计方案-上颌");
		design_dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		JPanel rpd_plan_panel = new JPanel(new BorderLayout());
		int total_height = 0;
		int total_width = 0;
		int line_height = 400;
		int print_height = 50;

		String input_file_path = choosed_file.getCanonicalPath();
		int dot_index = input_file_path.lastIndexOf(".");
		int gang_index = input_file_path.lastIndexOf("\\");
		String txt_file_name = input_file_path.substring(gang_index+1, dot_index);
		int maxillary_width = 0;
		if (!(this.maxillary_rpd_plans == null || this.maxillary_rpd_plans.size() == 0)) {
			generateAndSaveRPDPlanPicture(this.maxillary_rpd_plans, txt_file_name);
			JPanel maxillary_plan_panel = new JPanel(new FlowLayout());

			total_height += line_height + print_height;
			total_width = 0;
			for (int i = 1; i <= 3; i++) {
				String picture_name = "out//picture//" + txt_file_name + "_maxillary_RPD_design_" + i + ".png";
				ImageIcon im = new ImageIcon(picture_name);
				if (im.getIconWidth() == -1) {
					continue;
				}

				BufferedImage buffered_pic = new BufferedImage(
						im.getIconWidth(),
						im.getIconHeight(),
						BufferedImage.TYPE_INT_RGB
				);
				Graphics g_pic = buffered_pic.createGraphics();
				im.paintIcon(null, g_pic, 0,0);
				g_pic.dispose();

				ImageIcon base = new ImageIcon("res//base.png");
				BufferedImage buffered_base = new BufferedImage(
						base.getIconWidth(),
						base.getIconHeight(),
						BufferedImage.TYPE_INT_RGB
				);
				Graphics g_base = buffered_base.createGraphics();
				base.paintIcon(null, g_base, 0, 0);
				g_base.dispose();

				BufferedImage im_print = addTextToImage(buffered_pic, txt_file_name);
				BufferedImage base_print = addTextToImage(buffered_base, txt_file_name);
				String im_print_name = "out//picture//" + txt_file_name + "_maxillary_RPD_design_" + i + "_print.png";
				String base_print_name = "res//base_print.png";

				File im_print_file = new File(im_print_name);
				File base_print_file = new File(base_print_name);
				ImageIO.write(im_print, "png", im_print_file);
				ImageIO.write(base_print, "png", base_print_file);
				int src_im_height = im.getIconHeight();
				int src_im_width = im.getIconWidth();
				double scale_factor = (double) line_height / (double) src_im_height;
				double rescale_height = src_im_height * scale_factor;
				double rescale_width = src_im_width * scale_factor;
				int dest_im_width = (int) rescale_width;
				int dest_im_height = (int) rescale_height;
				im.setImage(im.getImage().getScaledInstance(dest_im_width, dest_im_height, Image.SCALE_DEFAULT));
				JLabel rpd_plan_label = new JLabel();
				rpd_plan_label.setSize(dest_im_width, dest_im_height);
				rpd_plan_label.setIcon(im);
				Border label_border = BorderFactory.createLineBorder(Color.BLACK);
				rpd_plan_label.setBorder(label_border);

				JButton print_button = new JButton("打印");
				print_button.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseReleased(MouseEvent arg0) {
//						String[] imgFileNameList = {im_print_name, base_print_name};
//						new PrintImage().drawImage(fileNameList, 1);
						try {
							new PrintTestFile().print(choosed_file, im_print_name, base_print_name, txt_file_name, 1);
						}
						catch (IOException ie) {
							ie.printStackTrace();
						}
					}
				});

				JPanel cur_plan_panel = new JPanel(new BorderLayout());
				cur_plan_panel.add(rpd_plan_label, BorderLayout.NORTH);
				cur_plan_panel.add(print_button, BorderLayout.SOUTH);
				maxillary_plan_panel.add(cur_plan_panel);
				maxillary_width += dest_im_width;
			}
			maxillary_plan_panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
					"上颌设计方案图示", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION,
					new Font("微软雅黑", Font.BOLD, 20)));

			JPanel maxillary_all_panel = new JPanel(new BorderLayout());
			maxillary_all_panel.add(maxillary_plan_panel, BorderLayout.CENTER);
			rpd_plan_panel.add(maxillary_all_panel, BorderLayout.NORTH);
		}

		total_width = maxillary_width;
		total_width += 750;
		total_height += 108;
//		rpd_plan_panel.setSize(total_width, total_height);
		design_dialog.add(rpd_plan_panel);
		design_dialog.setSize(total_width, total_height);
		design_dialog.setLocationRelativeTo(null);
		design_dialog.setVisible(true);
	}


	private void drawPlanExplanation() {
		if ((this.mandibular_rpd_plans == null || this.mandibular_rpd_plans.size() == 0)
				&& (this.maxillary_rpd_plans == null || this.maxillary_rpd_plans.size() == 0)) {
			return;
		}

		JDialog explanation_dialog = new JDialog(this.frame, "可摘局部义齿设计方案简要说明");
		JPanel rpd_plan_panel = new JPanel(new BorderLayout());
		int total_height = 0;
		int total_width = 1250;
		int line_height = 320;

		if (!(this.mandibular_rpd_plans == null || this.mandibular_rpd_plans.size() == 0)) {
			JPanel mandibular_plan_panel = new JPanel(new FlowLayout());
			mandibular_plan_panel.setSize(total_width, line_height);
			total_height += line_height + 25;
			for (RPDPlan plan:this.mandibular_rpd_plans) {
				JTextArea plan_explanation_textarea = new JTextArea(plan.getPlanExplanation());
				plan_explanation_textarea.setColumns(50);
				plan_explanation_textarea.setLineWrap(true);
				plan_explanation_textarea.setWrapStyleWord(true);
				plan_explanation_textarea.setFont(new Font("微软雅黑",Font.PLAIN,16));
				int v = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
				int h = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
				JScrollPane plan_explanation_scroll_pane = new JScrollPane(plan_explanation_textarea, v, h);
				plan_explanation_scroll_pane.setPreferredSize(new Dimension(400, line_height));
				mandibular_plan_panel.add(plan_explanation_scroll_pane);
			}

			mandibular_plan_panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
					"下颌设计方案简要说明", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION,
					new Font("微软雅黑", Font.BOLD, 20)));
			rpd_plan_panel.add(mandibular_plan_panel, BorderLayout.CENTER);
		}

		if (!(this.maxillary_rpd_plans == null || this.maxillary_rpd_plans.size() == 0)) {
			JPanel maxillary_plan_panel = new JPanel(new FlowLayout());
			maxillary_plan_panel.setSize(total_width, line_height);
			total_height += line_height + 25;
			for (RPDPlan plan:this.maxillary_rpd_plans) {
				JTextArea plan_explanation_textarea = new JTextArea(plan.getPlanExplanation());
				plan_explanation_textarea.setColumns(50);
				plan_explanation_textarea.setLineWrap(true);
				plan_explanation_textarea.setWrapStyleWord(true);
				plan_explanation_textarea.setFont(new Font("微软雅黑",Font.PLAIN,16));
				int v = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
				int h = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
				JScrollPane plan_explanation_scroll_pane = new JScrollPane(plan_explanation_textarea, v, h);
				plan_explanation_scroll_pane.setPreferredSize(new Dimension(400, line_height));
				maxillary_plan_panel.add(plan_explanation_scroll_pane);
			}

			maxillary_plan_panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
					"上颌设计方案简要说明", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION,
					new Font("微软雅黑", Font.BOLD, 20)));

			rpd_plan_panel.add(maxillary_plan_panel, BorderLayout.NORTH);
		}

		JTextField design_rights = new JTextField("北京大学口腔医院和清华大学联合研发");
		design_rights.setHorizontalAlignment(JTextField.CENTER);
		design_rights.setHorizontalAlignment(JTextField.CENTER);
		design_rights.setFont(new Font("宋体", Font.PLAIN, 14));
		design_rights.setOpaque(false);
		rpd_plan_panel.add(design_rights, BorderLayout.SOUTH);
		explanation_dialog.add(rpd_plan_panel);

		total_height += 108;
		explanation_dialog.setSize(total_width, total_height);
		explanation_dialog.setLocationRelativeTo(null);
		explanation_dialog.setVisible(true);
	}

	private void generateAndSaveRPDPlanPicture(List<RPDPlan> plans, String txt_file_name)
			throws java.io.IOException, exceptions.rpd.RuleException {
		if (plans == null || plans.size() == 0) {
			return;
		}

		String plan_position_str = plans.get(0).getPosition().toString();
		int design_count = 0;
		for (RPDPlan plan : plans) {
			design_count++;
			OntModel design_ont = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
			design_ont.read("file:" + owl_file.getCanonicalPath());
//			design_ont.read("res//sample.owl");
			planToOwl(plan, design_ont);
			String output_ont =
					"out//ontology//" + txt_file_name + "_" + plan_position_str + "_RPD_design_" + design_count + ".owl";
			String output_picture =
					"out//picture//" + txt_file_name + "_" + plan_position_str + "_RPD_design_" + design_count + ".png";
//			FileWriter out = new FileWriter(output_ont);
//			design_ont.write(out, "RDF/XML");
//			File out_file = new File(output_ont);
			checkAndCreateFile(output_ont);
			checkAndCreateFile(output_picture);
			File out_ont_file = new File(output_ont);
			FileOutputStream out_stream = new FileOutputStream(out_ont_file);
			RDFDataMgr.write(out_stream, design_ont, RDFFormat.RDFXML);
			imwrite(output_picture, getRpdDesign(design_ont));
		}
	}

	private void checkAndCreateFile(String filepath) throws java.io.IOException{
		File file = new File(filepath);
		File file_dir = new File(file.getParent());

		if (file_dir.exists()) {
			if (!file.exists()) {
				file.createNewFile();
			}
		}
		else {
			file_dir.mkdirs();
			file.createNewFile();
		}
	}


	private JPanel showMandibularPlansAsTable() throws IOException{
		if (this.mandibular_rpd_plans == null || this.mandibular_rpd_plans.size() == 0) {
			return null;
		}
		int plan_count = 0;
		List<JTable> mandibularPlanTableList = new ArrayList<>();

		JTable mandibularPlanTable;

		for (RPDPlan plan:mandibular_rpd_plans) { //遍历Plan
			mandibularPlanTable = buildEachPlanTable(plan, plan_count);
			mandibularPlanTableList.add(mandibularPlanTable);
			plan_count++;
		}

		if(flagOriginalMandibulayPlan == true){
			mandibularPlanBackup  = savePlan(mandibularPlanTableList,mandibular_rpd_plans);
			System.out.print("mandibularPlanBackup!\t\n");
			flagOriginalMandibulayPlan = false;
		}

		JButton buttonSava = new JButton("保存当前下颌方案");
		buttonSava.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				List<RPDPlan> mandibular_rpd_plans_new = savePlan(mandibularPlanTableList,mandibular_rpd_plans);
				mandibular_rpd_plans.clear();
				for(RPDPlan plan:mandibular_rpd_plans_new){
					mandibular_rpd_plans.add(plan);
//					System.out.print("plan = \t\n" + plan + "\t\n");
				}
			}
		});

		JButton buttonReset = new JButton("重置数据");
		buttonReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				if(mandibularPlanBackup == mandibular_rpd_plans)
//					System.out.print("mandibularPlanBackup == mandibular_rpd_plans \t\n");
//				else
//					System.out.print("mandibularPlanBackup != mandibular_rpd_plans \t\n");
				mandibular_rpd_plans.clear();
				int i = 0;
				for (RPDPlan plan:mandibularPlanBackup){ //遍历Plan
//					System.out.print("reset mandibularPlanBackup = \t\n" + plan + "\t\n");
					mandibular_rpd_plans.add(plan);
					JTable mandibularPlanTable = buildEachPlanTable(plan, i);
					mandibularPlanTableList.add(mandibularPlanTable);
					i++;
				}
				modifyFrame.dispose();
				modifyPlan();
			}
		});

		JButton buttonDrawPlan = new JButton("绘制草图");
		buttonDrawPlan.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					drawNewMandibularRPDPlans();
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (RuleException e1) {
					e1.printStackTrace();
				}
			}
		});

		JPanel mandibularPaneAll = new JPanel(new BorderLayout());
		mandibularPaneAll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
				"下颌设计方案文本", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION,
				new Font("微软雅黑", Font.BOLD, 20)));
		JPanel mandibularTextPane = new JPanel(new GridLayout(0,1));
		JPanel mandibularButtonPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		mandibularButtonPane.add(buttonReset);
		mandibularButtonPane.add(buttonSava);
		mandibularButtonPane.add(buttonDrawPlan);

		int i = 1;
		for (JTable table:mandibularPlanTableList)  //遍历Plan
			mandibularTextPane.add(buildPlanTablePanel(table,Position.Mandibular,i++));

//		JScrollPane mandibularTextSP = new JScrollPane(mandibularTextPane);

		mandibularPaneAll.add(mandibularButtonPane,BorderLayout.NORTH);
		mandibularPaneAll.add(mandibularTextPane,BorderLayout.CENTER);
		return mandibularPaneAll;
	}


	private JPanel showMaxillaryPlansAsTable() throws IOException{
		if (this.maxillary_rpd_plans == null || this.maxillary_rpd_plans.size() == 0) {
			return null;
		}
		int plan_count = 0;
		List<JTable> maxillaryPlanTableList = new ArrayList<>();
		JTable maxillaryPlanTable;

		for (RPDPlan plan:maxillary_rpd_plans) { //遍历Plan
			maxillaryPlanTable = buildEachPlanTable(plan, plan_count);
			maxillaryPlanTableList.add(maxillaryPlanTable);
			plan_count++;
		}

		if(flagOriginalMaxilaryPlan == true){
			maxillaryPlanBackup = savePlan(maxillaryPlanTableList,maxillary_rpd_plans);
			flagOriginalMaxilaryPlan = false;
		}

		JButton buttonSava = new JButton("保存当前上颌方案");
		buttonSava.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				List<RPDPlan> maxillary_rpd_plans_new = savePlan(maxillaryPlanTableList,maxillary_rpd_plans);

				maxillary_rpd_plans.clear();
				for(RPDPlan plan:maxillary_rpd_plans_new){
					maxillary_rpd_plans.add(plan);
					System.out.print("plan = " + plan + "\t\n");
				}
			}
		});

		JButton buttonReset = new JButton("重置数据");
		buttonReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

//			if(maxillaryPlanBackup == maxillary_rpd_plans)
//				System.out.print("maxillary_rpd_plans_new == maxillary_rpd_plans\t\n");
//			else
//				System.out.print("maxillary_rpd_plans_new != maxillary_rpd_plans\t\n");

				maxillary_rpd_plans.clear();
				int i = 0;
				for (RPDPlan plan:maxillaryPlanBackup){ //遍历Plan
//					System.out.print("reset maxillaryPlanBackup = \t\n" + plan + "\t\n");
					maxillary_rpd_plans.add(plan);
					JTable maxillaryPlanTable = buildEachPlanTable(plan, i);
					maxillaryPlanTableList.add(maxillaryPlanTable);
					i++;
				}
				modifyFrame.dispose();
				modifyPlan();
			}
		});


		JButton buttonDrawPlan = new JButton("绘制草图");
		buttonDrawPlan.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					drawNewMaxillaryRPDPlans();
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (RuleException e1) {
					e1.printStackTrace();
				}
			}
		});

		JPanel maxillaryPaneAll = new JPanel(new BorderLayout());
		maxillaryPaneAll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
				"上颌设计方案文本", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION,
				new Font("微软雅黑", Font.BOLD, 20)));
		JPanel maxillaryTextPane = new JPanel(new GridLayout(0,1));
		JPanel maxillaryButtonPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		maxillaryButtonPane.add(buttonReset);
		maxillaryButtonPane.add(buttonSava);
		maxillaryButtonPane.add(buttonDrawPlan);
		int i = 1 ;
		for (JTable table:maxillaryPlanTableList)  //遍历Plan
			maxillaryTextPane.add(buildPlanTablePanel(table,Position.Maxillary,i++));

		maxillaryPaneAll.add(maxillaryButtonPane,BorderLayout.NORTH);
		maxillaryPaneAll.add(maxillaryTextPane,BorderLayout.CENTER);
		return maxillaryPaneAll;
	}

	//	origin
	private JPanel showMandibularPlans() throws IOException {
		if (mandibular_rpd_plans == null || mandibular_rpd_plans.size() == 0) {
			return null;
		}
		JPanel design_panel = new JPanel(new BorderLayout());

		mandibular_plan_tree = buildMandibularPlanTree();//Build the table
		for (int i=0; i<mandibular_plan_tree.getRowCount();i++) {
			mandibular_plan_tree.expandRow(i);//add in every line
		}
		design_panel.add(mandibular_plan_tree, BorderLayout.CENTER); //show the table

		design_panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
				"下颌设计方案文本", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION,
				new Font("微软雅黑", Font.BOLD, 20)));
		return design_panel;
	}

	private JPanel showMaxillaryPlans() {
		if (maxillary_rpd_plans == null || maxillary_rpd_plans.size() == 0) {
			return null;
		}
		JPanel design_panel = new JPanel(new BorderLayout());
		maxillary_plan_tree = buildMaxillaryPlanTree();
		for (int i=0; i<maxillary_plan_tree.getRowCount();i++) {
			maxillary_plan_tree.expandRow(i);
		}
		design_panel.add(maxillary_plan_tree, BorderLayout.CENTER);
		design_panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
				"上颌设计方案文本", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION,
				new Font("微软雅黑", Font.BOLD, 20)));
		return design_panel;
	}

//	private void showRPDPlans() {
//
//		if (mandibular_rpd_plans == null || mandibular_rpd_plans.size() == 0)
//			return;
//
//		JDialog design_dialog = new JDialog(this.frame, "可摘局部义齿设计方案");
//		JPanel rpd_plan_panel = new JPanel(new BorderLayout());
//		design_dialog.add(rpd_plan_panel);
//
//		rpd_plan_tree = new JTree();
//		rpd_plan_tree.addMouseListener(new MouseAdapter() {
//
//			public void mouseClicked(MouseEvent e) {
//
//				if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3 && rpd_plan_tree.isEnabled()) {
//
//					TreePath path = rpd_plan_tree.getPathForLocation(e.getX(), e.getY());
//					if (path != null) {
//
//						rpd_plan_tree.setSelectionPath(path);
//						DefaultMutableTreeNode selected_node = (DefaultMutableTreeNode) path.getLastPathComponent();
//						Object user_obj = selected_node.getUserObject();
//						if (user_obj.getClass().equals(String.class)) {
//							String node_str = (String) user_obj;
//							if (node_str.startsWith("方案"))
//								rpd_plan_menu.show(rpd_plan_tree, e.getX(), e.getY());
//						} else if (user_obj.getClass().equals(Tooth.class)) {
//							tooth_menu.show(rpd_plan_tree, e.getX(), e.getY());
//						} else if (rpd.components.Component.class.isInstance(user_obj)) {
//							component_menu.show(rpd_plan_tree, e.getX(), e.getY());
//						} else {
//						}
//					}
//				}
//			}
//		});
//		rpd_plan_tree.setFont(new Font("微软雅黑", Font.PLAIN, 18));
//
//		Vector<Integer> plan_index = new Vector<Integer>();
//		for (int i = 0; i < mandibular_rpd_plans.size(); i++)
//			plan_index.addElement(i);
//		plan_choice = new JComboBox<Integer>(plan_index);
//		rpd_plan_panel.add(plan_choice, BorderLayout.NORTH);
//		rpd_plan_panel.add(rpd_plan_tree, BorderLayout.CENTER);
//		plan_choice.addActionListener(new ActionListener() {
//
//			@Override
//			public void actionPerformed(ActionEvent arg0) {
//				if (plan_choice.getSelectedItem() != null) {
//					int plan_index = (Integer) (plan_choice.getSelectedItem());
//					RPDPlan rpd_plan = mandibular_rpd_plans.get(plan_index);
//					current_rpd_plan = rpd_plan;
//					JTree new_rpd_plan_tree = buildRPDPlanTree(rpd_plan, plan_index);
//					if (new_rpd_plan_tree != null) {
//						rpd_plan_tree.setModel(new_rpd_plan_tree.getModel());
//						TreeNode root = (TreeNode) rpd_plan_tree.getModel().getRoot();
//						expandAll(rpd_plan_tree, new TreePath(root), true);
//					}
//				}
//			}
//		});
//
//		design_dialog.setSize(1000, 1000);
//		design_dialog.setLocationRelativeTo(null);
//		design_dialog.setVisible(true);
//	}

//	private JTree buildMandibularPlanTree() {
//		if (mandibular_rpd_plans == null || mandibular_rpd_plans.size() == 0) {
//			return null;
//		}//no Mandibular Plan
//		DefaultMutableTreeNode top_node = new DefaultMutableTreeNode("下颌设计方案");//root of plan
//		JTree mandibular_plan_Tree = new JTree(top_node);
//		mandibular_plan_Tree.setEnabled(true);
//		mandibular_plan_Tree.setFont(new Font("微软雅黑", Font.PLAIN, 18));
//
//		int plan_count = 0;
//		for (RPDPlan plan:mandibular_rpd_plans) { //遍历Plan
//			plan_count++;
//			DefaultMutableTreeNode plan_node = new DefaultMutableTreeNode("方案" + plan_count);
//			top_node.add(plan_node);
//			Map<ArrayList<Tooth>, Set<rpd.components.Component>> tooth_components = plan.getToothComponents();//read;use
//			ArrayList<ArrayList<Tooth>> plan_teeth = new ArrayList<>(tooth_components.keySet());//sort
//			Collections.sort(plan_teeth, new Comparator<ArrayList<Tooth>>() {
//				public int compare(ArrayList<Tooth> left, ArrayList<Tooth> right) {
//					return left.get(0).compareTo(right.get(0));
//				}
//			});
//			for (ArrayList<Tooth> tooth : plan_teeth) {
//				Set<rpd.components.Component> components = tooth_components.get(tooth);
//				for (rpd.components.Component component : components) {
//					DefaultMutableTreeNode component_node = new DefaultMutableTreeNode(component);
//					plan_node.add(component_node);
//				}
//			}
//		}
//		return mandibular_plan_Tree;
//	}
	private JPanel buildPlanTablePanel(final JTable table, Position pos, int count){

		JComboBox comMandibularComponentsName = new JComboBox(CString.mandibularComponentsName);
		JComboBox comMaxillaryComponentsName = new JComboBox(CString.maxillaryComponentsName);
		JComboBox comClaspMaterial = new JComboBox(CString.cClaspMaterial);
		JComboBox comClaspPosition = new JComboBox(CString.cClaspPosition);
		JComboBox comRestPosition = new JComboBox(CString.cRestPosition);
		JComboBox comClaspOrRestPosition = new JComboBox(CString.cClaspOrRestPosition);
		JComboBox comClaspComponentName = new JComboBox(CString.classClaspComponentName);
		JComboBox comRestComponentName = new JComboBox(CString.classRestComponentName);
		JComboBox comBaseComponentName = new JComboBox(CString.classBaseComponentName);
		JComboBox comMandibularConnecterComponentName = new JComboBox(CString.classMandibularConnecterComponentName);
		JComboBox comMaxillaryConnecterComponentName = new JComboBox(CString.classMaxillaryConnecterComponentName);
		JComboBox comMaxillaryConnecterToothPosition = new JComboBox(CString.cMaxillaryConnecterToothPosition);
		JComboBox comMandibularConnecterToothPosition = new JComboBox(CString.cMandibularConnecterToothPosition);
		JComboBox comNull = new JComboBox(CString.stringnull);


		comNull.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				final JComponent c = (JComponent) e.getSource();
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						c.requestFocus();
						System.out.println(c);
						if (c instanceof JComboBox) {
							System.out.println("a");
						}
					}
				});
			}
		});

		comMandibularComponentsName.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				final JComponent c = (JComponent) e.getSource();
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						c.requestFocus();
						System.out.println(c);
						if (c instanceof JComboBox) {
							System.out.println("a");
						}
					}
				});
			}
		});

		comMaxillaryComponentsName.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				final JComponent c = (JComponent) e.getSource();
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						c.requestFocus();
						System.out.println(c);
						if (c instanceof JComboBox) {
							System.out.println("a");
						}
					}
				});
			}
		});

		comClaspMaterial.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				final JComponent c = (JComponent) e.getSource();
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						c.requestFocus();
						System.out.println(c);
						if (c instanceof JComboBox) {
							System.out.println("a");
						}
					}
				});
			}
		});

		comClaspPosition.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				final JComponent c = (JComponent) e.getSource();
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						c.requestFocus();
						System.out.println(c);
						if (c instanceof JComboBox) {
							System.out.println("a");
						}
					}
				});
			}
		});

		comRestPosition.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				final JComponent c = (JComponent) e.getSource();
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						c.requestFocus();
						System.out.println(c);
						if (c instanceof JComboBox) {
							System.out.println("a");
						}
					}
				});
			}
		});

		comMandibularConnecterToothPosition.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				final JComponent c = (JComponent) e.getSource();
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						c.requestFocus();
						System.out.println(c);
						if (c instanceof JComboBox) {
							System.out.println("a");
						}
					}
				});
			}
		});

		comClaspComponentName.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				final JComponent c = (JComponent) e.getSource();
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						c.requestFocus();
						System.out.println(c);
						if (c instanceof JComboBox) {
							System.out.println("a");
						}
					}
				});
			}
		});

		comRestComponentName.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				final JComponent c = (JComponent) e.getSource();
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						c.requestFocus();
						System.out.println(c);
						if (c instanceof JComboBox) {
							System.out.println("a");
						}
					}
				});
			}
		});

		comMandibularConnecterComponentName.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				final JComponent c = (JComponent) e.getSource();
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						c.requestFocus();
						System.out.println(c);
						if (c instanceof JComboBox) {
							System.out.println("a");
						}
					}
				});
			}
		});

		comBaseComponentName.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				final JComponent c = (JComponent) e.getSource();
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						c.requestFocus();
						System.out.println(c);
						if (c instanceof JComboBox) {
							System.out.println("a");
						}
					}
				});
			}
		});

		comMaxillaryConnecterToothPosition.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				final JComponent c = (JComponent) e.getSource();
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						c.requestFocus();
						System.out.println(c);
						if (c instanceof JComboBox) {
							System.out.println("a");
						}
					}
				});
			}
		});


		comMaxillaryConnecterComponentName.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				final JComponent c = (JComponent) e.getSource();
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						c.requestFocus();
						System.out.println(c);
						if (c instanceof JComboBox) {
							System.out.println("a");
						}
					}
				});
			}
		});


		EachRowEditor rowEditor1 = new EachRowEditor(table);
		EachRowEditor rowEditor2 = new EachRowEditor(table);
		EachRowEditor rowEditor3 = new EachRowEditor(table);
		EachRowEditor rowEditor4 = new EachRowEditor(table);
		EachRowEditor rowEditor5 = new EachRowEditor(table);
		DefaultTableModel model = (DefaultTableModel) table.getModel();

		JButton buttonRemove = new JButton("移除选中行数据");
		buttonRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String classname;
				int count[]=table.getSelectedRows();
				if (count.length<=0) {
					return;
				}
				else {
					for (int i = count.length; i > 0; i--) {
						model.removeRow(table.getSelectedRow());
					}
				}
				for(int rowNum = 0;rowNum < table.getModel().getRowCount();rowNum++){
					if (model.getValueAt(rowNum,2) != null)
						classname = model.getValueAt(rowNum,2).toString();
					else
						break;
					switch (classname) {
						case "rpd.components.AkerClasp":
						case "rpd.components.WroughtWireClasp":
						case "rpd.components.CombinationClasp":
						case "rpd.components.CanineClasp":
						case "rpd.components.CanineAkerClasp":
						case "rpd.components.HalfHalfClasp":
						case "rpd.components.BackActionClasp":
						case "rpd.components.ReverseBackActionClasp":
						case "rpd.components.RingClasp":
						case "rpd.components.CombinedClasp":
						case "rpd.components.EmbrasureClasp":
						case "rpd.components.ContinuousClasp":
						case "rpd.components.RPAClasp":
							rowEditor1.setEditorAt(rowNum, new DefaultCellEditor(comClaspMaterial));
							table.getColumn("属性1").setCellEditor(rowEditor1);
							rowEditor2.setEditorAt(rowNum, new DefaultCellEditor(comClaspPosition));
							table.getColumn("属性2").setCellEditor(rowEditor2);
							rowEditor5.setEditorAt(rowNum, new DefaultCellEditor(comClaspComponentName));
							table.getColumn("组件名称").setCellEditor(rowEditor5);
							rowEditor3.setEditorAt(rowNum, new DefaultCellEditor(comNull));
							table.getColumn("舌侧对抗#1").setCellEditor(rowEditor3);
							table.getColumn("舌侧对抗#2").setCellEditor(rowEditor3);
							break;
						case "rpd.components.OcclusalRest":
						case "rpd.components.LingualRest":
							rowEditor2.setEditorAt(rowNum, new DefaultCellEditor(comRestPosition));
							table.getColumn("属性2").setCellEditor(rowEditor2);
							rowEditor5.setEditorAt(rowNum, new DefaultCellEditor(comRestComponentName));
							table.getColumn("组件名称").setCellEditor(rowEditor5);
							rowEditor3.setEditorAt(rowNum, new DefaultCellEditor(comNull));
							table.getColumn("属性1").setCellEditor(rowEditor3);
							table.getColumn("舌侧对抗#1").setCellEditor(rowEditor3);
							table.getColumn("舌侧对抗#2").setCellEditor(rowEditor3);
							break;
						case "rpd.components.LingualBarConnector":
						case "rpd.components.LingualPlateConnector":
							rowEditor3.setEditorAt(rowNum, new DefaultCellEditor(comMandibularConnecterToothPosition));
							table.getColumn("舌侧对抗#1").setCellEditor(rowEditor3);
							rowEditor4.setEditorAt(rowNum, new DefaultCellEditor(comMandibularConnecterToothPosition));
							table.getColumn("舌侧对抗#2").setCellEditor(rowEditor4);
							rowEditor5.setEditorAt(rowNum, new DefaultCellEditor(comMandibularConnecterComponentName));
							table.getColumn("组件名称").setCellEditor(rowEditor5);
							rowEditor1.setEditorAt(rowNum, new DefaultCellEditor(comNull));
							table.getColumn("属性1").setCellEditor(rowEditor1);
							table.getColumn("属性2").setCellEditor(rowEditor1);
							break;
						case "rpd.components.SinglePalatalStrapConnector":
						case "rpd.components.CombinationAnteriorPosteriorPalatalStrapConnector":
						case "rpd.components.PalatalPlateConnector":
						case "rpd.components.FullPalatalPlateConnector":
						case "rpd.components.ModifiedPalatalPlateConnector":
							rowEditor3.setEditorAt(rowNum, new DefaultCellEditor(comMaxillaryConnecterToothPosition));
							table.getColumn("舌侧对抗#1").setCellEditor(rowEditor3);
							rowEditor4.setEditorAt(rowNum, new DefaultCellEditor(comMaxillaryConnecterToothPosition));
							table.getColumn("舌侧对抗#2").setCellEditor(rowEditor4);
							rowEditor5.setEditorAt(rowNum, new DefaultCellEditor(comMaxillaryConnecterComponentName));
							table.getColumn("组件名称").setCellEditor(rowEditor5);
							rowEditor1.setEditorAt(rowNum, new DefaultCellEditor(comNull));
							table.getColumn("属性1").setCellEditor(rowEditor1);
							table.getColumn("属性2").setCellEditor(rowEditor1);
							break;
						case "rpd.components.DentureBase":
							rowEditor5.setEditorAt(rowNum, new DefaultCellEditor(comBaseComponentName));
							table.getColumn("组件名称").setCellEditor(rowEditor5);
							rowEditor3.setEditorAt(rowNum, new DefaultCellEditor(comNull));
							table.getColumn("舌侧对抗#1").setCellEditor(rowEditor3);
							table.getColumn("舌侧对抗#2").setCellEditor(rowEditor3);
							table.getColumn("属性1").setCellEditor(rowEditor3);
							table.getColumn("属性2").setCellEditor(rowEditor3);
							break;
					}
				}
			}
		});

		JButton buttonAddColumn = new JButton("新增舌侧对抗");
		buttonAddColumn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final int col = table.getColumnCount();
				int row = table.getModel().getRowCount();
				String connecter;
				model.addColumn("舌侧对抗#"+(col-5));
				for(int i = 0; i<row; i++){
					if(model.getValueAt(i,3).toString()!= null)
						connecter = model.getValueAt(i,3).toString();
					else
						break;
					if(connecter == "上颌腭带（Single Palatal Strap）"
							|| connecter == "上颌前后腭带（Combination Anterior and Posterior Palatal Strap）"
							|| connecter == "上颌腭板（Palatal Plate）"
							|| connecter == "上颌全腭板（Full Palatal Plate）"
							|| connecter == "上颌变异腭板（Modified Palatal Plate）"){
						rowEditor1.setEditorAt(i, new DefaultCellEditor(comMaxillaryConnecterToothPosition));
						table.getColumn("舌侧对抗#"+(col-5)).setCellEditor(rowEditor1);
					}
					else if(connecter == "下颌舌板（Lingual Plate）"){
						rowEditor1.setEditorAt(i, new DefaultCellEditor(comMandibularConnecterToothPosition));
						table.getColumn("舌侧对抗#"+(col-5)).setCellEditor(rowEditor1);
					}
				}
				String classname;
				hideTableColumn(table,0);
				hideTableColumn(table,2);

				for(int rowNum =0;rowNum<table.getModel().getRowCount();rowNum++){
						if (model.getValueAt(rowNum,2) != null)
							classname = model.getValueAt(rowNum,2).toString();
						else
							break;
					switch (classname) {
						case "rpd.components.AkerClasp":
						case "rpd.components.WroughtWireClasp":
						case "rpd.components.CombinationClasp":
						case "rpd.components.CanineClasp":
						case "rpd.components.CanineAkerClasp":
						case "rpd.components.HalfHalfClasp":
						case "rpd.components.BackActionClasp":
						case "rpd.components.ReverseBackActionClasp":
						case "rpd.components.RingClasp":
						case "rpd.components.CombinedClasp":
						case "rpd.components.EmbrasureClasp":
						case "rpd.components.ContinuousClasp":
						case "rpd.components.RPAClasp":
							rowEditor1.setEditorAt(rowNum, new DefaultCellEditor(comClaspMaterial));
							table.getColumn("属性1").setCellEditor(rowEditor1);
							rowEditor2.setEditorAt(rowNum, new DefaultCellEditor(comClaspPosition));
							table.getColumn("属性2").setCellEditor(rowEditor2);
							rowEditor5.setEditorAt(rowNum, new DefaultCellEditor(comClaspComponentName));
							table.getColumn("组件名称").setCellEditor(rowEditor5);
							break;
						case "rpd.components.OcclusalRest":
						case "rpd.components.LingualRest":
							rowEditor2.setEditorAt(rowNum, new DefaultCellEditor(comRestPosition));
							table.getColumn("属性2").setCellEditor(rowEditor2);
							rowEditor5.setEditorAt(rowNum, new DefaultCellEditor(comRestComponentName));
							table.getColumn("组件名称").setCellEditor(rowEditor5);
							break;
						case "rpd.components.LingualBarConnector":
						case "rpd.components.LingualPlateConnector":
							rowEditor3.setEditorAt(rowNum, new DefaultCellEditor(comMandibularConnecterToothPosition));
							table.getColumn("舌侧对抗#1").setCellEditor(rowEditor3);
							rowEditor4.setEditorAt(rowNum, new DefaultCellEditor(comMandibularConnecterToothPosition));
							table.getColumn("舌侧对抗#2").setCellEditor(rowEditor4);
							rowEditor5.setEditorAt(rowNum, new DefaultCellEditor(comMandibularConnecterComponentName));
							table.getColumn("组件名称").setCellEditor(rowEditor5);
							break;
						case "rpd.components.SinglePalatalStrapConnector":
						case "rpd.components.CombinationAnteriorPosteriorPalatalStrapConnector":
						case "rpd.components.PalatalPlateConnector":
						case "rpd.components.FullPalatalPlateConnector":
						case "rpd.components.ModifiedPalatalPlateConnector":
							rowEditor3.setEditorAt(rowNum, new DefaultCellEditor(comMaxillaryConnecterToothPosition));
							table.getColumn("舌侧对抗#1").setCellEditor(rowEditor3);
							rowEditor4.setEditorAt(rowNum, new DefaultCellEditor(comMaxillaryConnecterToothPosition));
							table.getColumn("舌侧对抗#2").setCellEditor(rowEditor4);
							rowEditor5.setEditorAt(rowNum, new DefaultCellEditor(comMaxillaryConnecterComponentName));
							table.getColumn("组件名称").setCellEditor(rowEditor5);
							break;
						case "rpd.components.DentureBase":
							rowEditor5.setEditorAt(rowNum, new DefaultCellEditor(comBaseComponentName));
							table.getColumn("组件名称").setCellEditor(rowEditor5);
							break;
					}
				}
			}
		});

		JButton buttonAdd = new JButton("新增组件");
		buttonAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedRow = table.getModel().getRowCount();
				int col  = table.getColumnCount();
				String[] arrTemp = new String[20];
//				arrTemp[0] = model.getValueAt(selectedRow - 1, 0).toString();
				model.insertRow(selectedRow, arrTemp);
				if(pos == Position.Mandibular){
					rowEditor5.setEditorAt(selectedRow, new DefaultCellEditor(comMandibularComponentsName));
					rowEditor4.setEditorAt(selectedRow, new DefaultCellEditor(comMandibularConnecterToothPosition));
					rowEditor3.setEditorAt(selectedRow, new DefaultCellEditor(comMandibularConnecterToothPosition));
				}
				else{
					rowEditor5.setEditorAt(selectedRow, new DefaultCellEditor(comMaxillaryComponentsName));
					rowEditor4.setEditorAt(selectedRow, new DefaultCellEditor(comMaxillaryConnecterToothPosition));
					rowEditor3.setEditorAt(selectedRow, new DefaultCellEditor(comMaxillaryConnecterToothPosition));
				}
				rowEditor1.setEditorAt(selectedRow, new DefaultCellEditor(comClaspMaterial));
				rowEditor2.setEditorAt(selectedRow, new DefaultCellEditor(comClaspOrRestPosition));
				table.getColumn("组件名称").setCellEditor(rowEditor5);
				table.getColumn("属性1").setCellEditor(rowEditor1);
				table.getColumn("属性2").setCellEditor(rowEditor2);
//				table.getColumn("舌侧对抗#1").setCellEditor(rowEditor3);
//				table.getColumn("舌侧对抗#2").setCellEditor(rowEditor4);
				for(int i = 6; i<col; i ++){
					table.getColumn("舌侧对抗#"+(i-5)).setCellEditor(rowEditor3);
				}
//				table.updateUI();
				String classname;
				for(int rowNum = 0;rowNum < table.getModel().getRowCount();rowNum++){
					if (model.getValueAt(rowNum,2) != null)
						classname = model.getValueAt(rowNum,2).toString();
					else
						break;
					switch (classname) {
						case "rpd.components.AkerClasp":
						case "rpd.components.WroughtWireClasp":
						case "rpd.components.CombinationClasp":
						case "rpd.components.CanineClasp":
						case "rpd.components.CanineAkerClasp":
						case "rpd.components.HalfHalfClasp":
						case "rpd.components.BackActionClasp":
						case "rpd.components.ReverseBackActionClasp":
						case "rpd.components.RingClasp":
						case "rpd.components.CombinedClasp":
						case "rpd.components.EmbrasureClasp":
						case "rpd.components.ContinuousClasp":
						case "rpd.components.RPAClasp":
							rowEditor1.setEditorAt(rowNum, new DefaultCellEditor(comClaspMaterial));
							table.getColumn("属性1").setCellEditor(rowEditor1);
							rowEditor2.setEditorAt(rowNum, new DefaultCellEditor(comClaspPosition));
							table.getColumn("属性2").setCellEditor(rowEditor2);
							rowEditor5.setEditorAt(rowNum, new DefaultCellEditor(comClaspComponentName));
							table.getColumn("组件名称").setCellEditor(rowEditor5);
							break;
						case "rpd.components.OcclusalRest":
						case "rpd.components.LingualRest":
							rowEditor2.setEditorAt(rowNum, new DefaultCellEditor(comRestPosition));
							table.getColumn("属性2").setCellEditor(rowEditor2);
							rowEditor5.setEditorAt(rowNum, new DefaultCellEditor(comRestComponentName));
							table.getColumn("组件名称").setCellEditor(rowEditor5);
							break;
						case "rpd.components.LingualBarConnector":
						case "rpd.components.LingualPlateConnector":
							rowEditor3.setEditorAt(rowNum, new DefaultCellEditor(comMandibularConnecterToothPosition));
							table.getColumn("舌侧对抗#1").setCellEditor(rowEditor3);
							rowEditor4.setEditorAt(rowNum, new DefaultCellEditor(comMandibularConnecterToothPosition));
							table.getColumn("舌侧对抗#2").setCellEditor(rowEditor4);
							rowEditor5.setEditorAt(rowNum, new DefaultCellEditor(comMandibularConnecterComponentName));
							table.getColumn("组件名称").setCellEditor(rowEditor5);
							break;
						case "rpd.components.SinglePalatalStrapConnector":
						case "rpd.components.CombinationAnteriorPosteriorPalatalStrapConnector":
						case "rpd.components.PalatalPlateConnector":
						case "rpd.components.FullPalatalPlateConnector":
						case "rpd.components.ModifiedPalatalPlateConnector":
							rowEditor3.setEditorAt(rowNum, new DefaultCellEditor(comMaxillaryConnecterToothPosition));
							table.getColumn("舌侧对抗#1").setCellEditor(rowEditor3);
							rowEditor4.setEditorAt(rowNum, new DefaultCellEditor(comMaxillaryConnecterToothPosition));
							table.getColumn("舌侧对抗#2").setCellEditor(rowEditor4);
							rowEditor5.setEditorAt(rowNum, new DefaultCellEditor(comMaxillaryConnecterComponentName));
							table.getColumn("组件名称").setCellEditor(rowEditor5);
							break;
						case "rpd.components.DentureBase":
							rowEditor5.setEditorAt(rowNum, new DefaultCellEditor(comBaseComponentName));
							table.getColumn("组件名称").setCellEditor(rowEditor5);
							break;
					}
				}
				hideTableColumn(table,0);
				hideTableColumn(table,2);
			}
		});


		JScrollPane tablePanel = new JScrollPane(table);
//		String title;
//		if(pos == Position.Maxillary)
//			title = "上颌设计方案文本";
//		else
//			title = "下颌设计方案文本";
//		tablePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
//				"方案"+count, TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION,
//				new Font("微软雅黑", Font.BOLD, 20)));
		tablePanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		tablePanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		table.setRowHeight(30);
		int preferredHight = model.getRowCount() * 30;
		int preferredWidth = model.getColumnCount() * 200;
		table.setBackground(Color.white);
		table.setFont(new Font("微软雅黑", Font.PLAIN, 18));
//		maxillary_plan_table.getColumnModel().getColumn(0).setMaxWidth(0);
		table.getColumnModel().getColumn(1).setMinWidth(200);
//		maxillary_plan_table.getColumnModel().getColumn(2).setPreferredWidth(200);
		table.getColumnModel().getColumn(3).setMinWidth(200);
		table.getColumnModel().getColumn(4).setMinWidth(200);
		table.getColumnModel().getColumn(5).setMinWidth(200);
		table.getColumnModel().getColumn(6).setMinWidth(200);
		table.getColumnModel().getColumn(7).setMinWidth(200);
		table.setPreferredScrollableViewportSize(new Dimension(preferredWidth, preferredHight));
		table.setMaximumSize(new Dimension(preferredWidth, preferredHight));
		hideTableColumn(table,0);
		hideTableColumn(table,2);

		JPanel Pane = new JPanel(new BorderLayout());//right!
		Pane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
				"方案"+count, TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION,
				new Font("微软雅黑", Font.BOLD, 20)));
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		buttonPanel.setPreferredSize(new Dimension(160, 50));

		buttonPanel.add(buttonAdd);
		buttonPanel.add(buttonRemove);
		buttonPanel.add(buttonAddColumn);
		Pane.add(buttonPanel,BorderLayout.NORTH);
		Pane.add(tablePanel,BorderLayout.CENTER);
		return Pane;
	}

	private JTable buildEachPlanTable(RPDPlan plan, int plan_count) {
	if (plan == null) {
		return null;
	}
	DefaultTableModel defaultTableModel = new DefaultTableModel(CString.col, 0);
	JTable table = new JTable(defaultTableModel);

	table = setRowEditors(plan, table, plan_count);

	DefaultTableModel model = (DefaultTableModel)table.getModel();
	int preferredHight = model.getRowCount() * 30;
	table.setRowHeight(30);
	table.setBackground(Color.white);
	table.setFont(new Font("微软雅黑", Font.PLAIN, 18));
	hideTableColumn(table, 0);
	table.getColumnModel().getColumn(1).setPreferredWidth(200);
	hideTableColumn(table, 2);
	table.getColumnModel().getColumn(3).setPreferredWidth(200);
	table.getColumnModel().getColumn(4).setPreferredWidth(200);
	table.getColumnModel().getColumn(5).setPreferredWidth(200);
	table.getColumnModel().getColumn(6).setPreferredWidth(200);
	table.getColumnModel().getColumn(7).setPreferredWidth(200);
	table.setPreferredScrollableViewportSize(new Dimension(1200, preferredHight));

	return table;
}


	private JTree buildMandibularPlanTree() {
		if (mandibular_rpd_plans == null || mandibular_rpd_plans.size() == 0) {
			return null;
		}
		DefaultMutableTreeNode top_node = new DefaultMutableTreeNode("下颌设计方案");
		JTree mandibular_plan_tree = new JTree(top_node);
		mandibular_plan_tree.setEnabled(true);
		mandibular_plan_tree.setFont(new Font("微软雅黑", Font.PLAIN, 18));

		int plan_count = 0;
		for (RPDPlan plan:mandibular_rpd_plans) {
			plan_count++;
			DefaultMutableTreeNode plan_node = new DefaultMutableTreeNode("方案" + plan_count);
			top_node.add(plan_node);
			Map<ArrayList<Tooth>, Set<rpd.components.Component>> tooth_components = plan.getToothComponents();
			ArrayList<ArrayList<Tooth>> plan_teeth = new ArrayList<>(tooth_components.keySet());
			Collections.sort(plan_teeth, new Comparator<ArrayList<Tooth>>() {
				public int compare(ArrayList<Tooth> left, ArrayList<Tooth> right) {
					return left.get(0).compareTo(right.get(0));
				}
			});
			for (ArrayList<Tooth> tooth : plan_teeth) {
				Set<rpd.components.Component> components = tooth_components.get(tooth);
				for (rpd.components.Component component : components) {
					DefaultMutableTreeNode component_node = new DefaultMutableTreeNode(component);
					plan_node.add(component_node);
				}
			}
		}
		return mandibular_plan_tree;
	}

	private JTree buildMaxillaryPlanTree() {
		if (maxillary_rpd_plans == null || maxillary_rpd_plans.size() == 0) {
			return null;
		}
		DefaultMutableTreeNode top_node = new DefaultMutableTreeNode("上颌设计方案");
		JTree maxillary_plan_tree = new JTree(top_node);
		maxillary_plan_tree.setEnabled(true);
		maxillary_plan_tree.setFont(new Font("微软雅黑", Font.PLAIN, 18));

		int plan_count = 0;
		for (RPDPlan plan:maxillary_rpd_plans) {
			plan_count++;
			DefaultMutableTreeNode plan_node = new DefaultMutableTreeNode("方案" + plan_count);
			top_node.add(plan_node);
			Map<ArrayList<Tooth>, Set<rpd.components.Component>> tooth_components = plan.getToothComponents();
			ArrayList<ArrayList<Tooth>> plan_teeth = new ArrayList<>(tooth_components.keySet());
			Collections.sort(plan_teeth, new Comparator<ArrayList<Tooth>>() {
				public int compare(ArrayList<Tooth> left, ArrayList<Tooth> right) {
					return left.get(0).compareTo(right.get(0));
				}
			});
			for (ArrayList<Tooth> tooth : plan_teeth) {
				Set<rpd.components.Component> components = tooth_components.get(tooth);
				for (rpd.components.Component component : components) {
					DefaultMutableTreeNode component_node = new DefaultMutableTreeNode(component);
					plan_node.add(component_node);
				}
			}
		}
		return maxillary_plan_tree;
	}

	private JTree buildRPDPlanTree(RPDPlan rpd_plan, int plan_index) {

		if (rpd_plan == null)
			return null;

		DefaultMutableTreeNode top_node = new DefaultMutableTreeNode("方案" + plan_index);
		JTree rpd_plan_tree = new JTree(top_node);
		rpd_plan_tree.setEnabled(true);
		rpd_plan_tree.setFont(new Font("微软雅黑", Font.PLAIN, 18));

		Map<ArrayList<Tooth>, Set<rpd.components.Component>> tooth_components = rpd_plan.getToothComponents();
		ArrayList<ArrayList<Tooth>> plan_teeth = new ArrayList<>(tooth_components.keySet());

		Collections.sort(plan_teeth, new Comparator<ArrayList<Tooth>>() {
			public int compare(ArrayList<Tooth> left, ArrayList<Tooth> right) {
				return left.get(0).compareTo(right.get(0));
			}
		});

//		for (ArrayList<Tooth> tooth : plan_teeth) {
//			Set<rpd.components.Component> components = tooth_components.get(tooth);
//			DefaultMutableTreeNode tooth_node = new DefaultMutableTreeNode(tooth);
//			top_node.add(tooth_node);
//			for (rpd.components.Component component : components) {
//				DefaultMutableTreeNode component_node = new DefaultMutableTreeNode(component);
//				tooth_node.add(component_node);
//			}
//		}
		for (ArrayList<Tooth> tooth : plan_teeth) {
			Set<rpd.components.Component> components = tooth_components.get(tooth);
			for (rpd.components.Component component : components) {
				DefaultMutableTreeNode component_node = new DefaultMutableTreeNode(component);
				top_node.add(component_node);
			}

		}

		return rpd_plan_tree;
	}

	@SuppressWarnings("rawtypes")
	private void expandAll(JTree tree, TreePath parent, boolean expand) {

		TreeNode node = (TreeNode) parent.getLastPathComponent();
		if (node.getChildCount() > 0) {
			for (Enumeration e = node.children(); e.hasMoreElements(); ) {
				TreeNode n = (TreeNode) e.nextElement();
				TreePath path = parent.pathByAddingChild(n);
				expandAll(tree, path, expand);
			}
		}

		if (expand)
			tree.expandPath(parent);
		else
			tree.collapsePath(parent);
	}

	private void addPropertyMenuItems(JPopupMenu popup) throws PropertyValueException {

		Set<DatatypeProperty> top_dps = OntFunc.getTopDataProperties(dental_ont);
		List<String> top_dp_names = new ArrayList<String>();
		for (DatatypeProperty dp : top_dps)
			top_dp_names.add(dp.getLocalName());
		String[] top_dp_names_array = top_dp_names.toArray(new String[0]);
		Arrays.sort(top_dp_names_array);

		Stack<DatatypeProperty> dp_stack = new Stack<DatatypeProperty>();
		for (String dp_name : top_dp_names_array) {

			DatatypeProperty dp = this.dental_ont.getDatatypeProperty(OntFunc.prefix + dp_name);
			this.addToMenu(popup, dp);
			List<? extends OntProperty> sub_dp_list = dp.listSubProperties().toList();
			List<String> sub_dp_names = new ArrayList<String>();
			for (OntProperty sub_dp : sub_dp_list)
				sub_dp_names.add(sub_dp.getLocalName());
			String[] sub_dp_names_array = sub_dp_names.toArray(new String[0]);
			Arrays.sort(sub_dp_names_array);

			for (int i = sub_dp_names_array.length - 1; i >= 0; i--) {

				String sub_dp_name = sub_dp_names_array[i];
				DatatypeProperty sub_dp = this.dental_ont.getDatatypeProperty(OntFunc.prefix + sub_dp_name);
				dp_stack.push(sub_dp);
			}
		}

		while (!dp_stack.isEmpty()) {

			DatatypeProperty dp = dp_stack.pop();
			DatatypeProperty super_dp = dp.getSuperProperty().asDatatypeProperty();
			JComponent super_dp_menu = this.property_menu_map.get(super_dp);
			this.addToMenu(super_dp_menu, dp);

			List<? extends OntProperty> sub_dp_list = dp.listSubProperties().toList();
			List<String> sub_dp_names = new ArrayList<String>();
			for (OntProperty sub_dp : sub_dp_list)
				sub_dp_names.add(sub_dp.getLocalName());
			String[] sub_dp_names_array = sub_dp_names.toArray(new String[0]);
			Arrays.sort(sub_dp_names_array);

			for (int i = sub_dp_names_array.length - 1; i >= 0; i--) {
				String sub_dp_name = sub_dp_names_array[i];
				DatatypeProperty sub_dp = this.dental_ont.getDatatypeProperty(OntFunc.prefix + sub_dp_name);
				dp_stack.push(sub_dp);
			}
		}
	}

	private void addToMenu(JComponent menu, DatatypeProperty dp) throws PropertyValueException {

		List<? extends OntProperty> sub_ps = OntFunc.getSubProperties(dp);

		if (sub_ps != null && sub_ps.size() != 0) {

			JMenu property_menu = new JMenu(dp.getLocalName());
			property_menu.setFont(new Font("微软雅黑", Font.PLAIN, 18));
			menu.add(property_menu);
			this.property_menu_map.put(dp, property_menu);
		} else
			addPropertyAndValues(menu, dp);
	}

	private void addBooleanValues(JMenu property_menu, DatatypeProperty dp) throws PropertyValueException {

		PropertyDescription dp_des = this.des.property_descriptions.get(dp);
		Map<Boolean, String> value_des = dp_des.getBooleanValueDescriptions();
		if (value_des == null) {

			//System.out.println("can not get values for boolean property: " + dp.getLocalName());
			return;
		}

		boolean[] values = {false, true};

		for (boolean value : values) {

			String boolean_value_description = value_des.get(value);
			JMenuItem value_item = new JMenuItem(String.valueOf(value) + " ( " + boolean_value_description + " )");
			value_item.setFont(new Font("微软雅黑", Font.PLAIN, 18));
			property_menu.add(value_item);

			value_item.addMouseListener(new MouseAdapter() {

				public void mouseReleased(MouseEvent e) {

					String label_text = emr_text.getSelectedText();
					if (label_text == null)
						return;

					int end_offset = emr_text.getCaretPosition();
					int start_offset = emr_text.getCaretPosition() - label_text.length();
					ToothMap tooth_map = null;
					if (dp_des.isToothRelated())
						tooth_map = findValidToothMap(start_offset);
					BooleanPropertyValue boolean_value = new BooleanPropertyValue(value);
					PropertyLabel label = new PropertyLabel(start_offset, end_offset, label_text, dp,
							boolean_value, LabelModifier.None, tooth_map);
					DefaultTableModel label_table_model = (DefaultTableModel) label_table.getModel();
					label_table_model.addRow(new Object[]{

							tooth_map,
							label_text,
							dp.getLocalName(),
							value,
							boolean_value_description,
							label.getModifier()
					});
					label_list.add(label);

					if (chckbx_show_all_labels.isSelected())
						try {
							setEMRTextColorAllLabels();
						} catch (BadLocationException e1) {
							e1.printStackTrace();
						}
				}
			});
		}
	}

	private void addListValues(JMenu property_menu, DatatypeProperty dp) throws PropertyValueException {

		PropertyDescription dp_des = this.des.property_descriptions.get(dp);
		Map<Integer, String> value_des = dp_des.getListValueDescriptions();
		if (value_des == null) {

			//System.out.println("can not get values for list-value property: " + dp.getLocalName());
			return;
		}

		List<Integer> list_values = dp_des.getListValues();

		for (int value : list_values) {

			String list_value_description = value_des.get(value);
			JMenuItem value_item = new JMenuItem(String.valueOf(value) + " ( " + list_value_description + " )");
			value_item.setFont(new Font("微软雅黑", Font.PLAIN, 18));
			property_menu.add(value_item);

			value_item.addMouseListener(new MouseAdapter() {

				public void mouseReleased(MouseEvent e) {

					String label_text = emr_text.getSelectedText();
					if (label_text == null)
						return;

					int end_offset = emr_text.getCaretPosition();
					int start_offset = emr_text.getCaretPosition() - label_text.length();
					ToothMap tooth_map = null;
					if (dp_des.isToothRelated())
						tooth_map = findValidToothMap(start_offset);
					ListPropertyValue list_value = new ListPropertyValue(value);
					PropertyLabel label = new PropertyLabel(start_offset, end_offset, label_text,
							dp, list_value, LabelModifier.None, tooth_map);
					DefaultTableModel label_table_model = (DefaultTableModel) label_table.getModel();
					label_table_model.addRow(new Object[]{

							tooth_map,
							label_text,
							dp.getLocalName(),
							value,
							list_value_description,
							label.getModifier()
					});
					label_list.add(label);

					if (chckbx_show_all_labels.isSelected())
						try {
							setEMRTextColorAllLabels();
						} catch (BadLocationException e1) {
							e1.printStackTrace();
						}
				}
			});
		}
	}

	private void addIntValue(JMenu property_menu, DatatypeProperty dp) throws PropertyValueException {

		PropertyDescription dp_des = this.des.property_descriptions.get(dp);
		String int_value_description = dp_des.getIntValueDescription();

		JMenuItem value_item = new JMenuItem("int value: " + int_value_description);
		value_item.setFont(new Font("微软雅黑", Font.PLAIN, 18));
		property_menu.add(value_item);

		value_item.addMouseListener(new MouseAdapter() {

			public void mouseReleased(MouseEvent e) {

				String label_text = emr_text.getSelectedText();
				if (label_text == null)
					return;

				valueDialog(int_value_description);
				if (!value_valid)
					return;

				int end_offset = emr_text.getCaretPosition();
				int start_offset = emr_text.getCaretPosition() - label_text.length();
				ToothMap tooth_map = null;
				if (dp_des.isToothRelated())
					tooth_map = findValidToothMap(start_offset);

				IntPropertyValue int_value = new IntPropertyValue(Integer.valueOf(value));
				PropertyLabel label = new PropertyLabel(start_offset, end_offset, label_text,
						dp, int_value, LabelModifier.None, tooth_map);
				DefaultTableModel label_table_model = (DefaultTableModel) label_table.getModel();
				label_table_model.addRow(new Object[]{

						tooth_map,
						label_text,
						dp.getLocalName(),
						value,
						int_value_description,
						label.getModifier()
				});
				label_list.add(label);

				if (chckbx_show_all_labels.isSelected())
					try {
						setEMRTextColorAllLabels();
					} catch (BadLocationException e1) {
						e1.printStackTrace();
					}
			}
		});
	}

	private void addDoubleValue(JMenu property_menu, DatatypeProperty dp) throws PropertyValueException {

		PropertyDescription dp_des = this.des.property_descriptions.get(dp);
		String double_value_description = dp_des.getDoubleValueDescription();

		JMenuItem value_item = new JMenuItem("double value: " + double_value_description);
		value_item.setFont(new Font("微软雅黑", Font.PLAIN, 18));
		property_menu.add(value_item);

		value_item.addMouseListener(new MouseAdapter() {

			public void mouseReleased(MouseEvent e) {

				String label_text = emr_text.getSelectedText();
				if (label_text == null)
					return;

				valueDialog(double_value_description);
				if (!value_valid)
					return;

				int end_offset = emr_text.getCaretPosition();
				int start_offset = emr_text.getCaretPosition() - label_text.length();
				ToothMap tooth_map = null;
				if (dp_des.isToothRelated())
					tooth_map = findValidToothMap(start_offset);

				DoublePropertyValue double_value = new DoublePropertyValue(Double.valueOf(value));
				PropertyLabel label = new PropertyLabel(start_offset, end_offset, label_text,
						dp, double_value, LabelModifier.None, tooth_map);
				DefaultTableModel label_table_model = (DefaultTableModel) label_table.getModel();
				label_table_model.addRow(new Object[]{

						tooth_map,
						label_text,
						dp.getLocalName(),
						value,
						double_value_description,
						label.getModifier()
				});
				label_list.add(label);

				if (chckbx_show_all_labels.isSelected())
					try {
						setEMRTextColorAllLabels();
					} catch (BadLocationException e1) {
						e1.printStackTrace();
					}
			}
		});
	}

	private void addStringValue(JMenu property_menu, DatatypeProperty dp) throws PropertyValueException {

		PropertyDescription dp_des = this.des.property_descriptions.get(dp);
		String string_value_description = dp_des.getStringValueDescription();

		JMenuItem value_item = new JMenuItem("string value: " + string_value_description);
		value_item.setFont(new Font("微软雅黑", Font.PLAIN, 18));
		property_menu.add(value_item);

		value_item.addMouseListener(new MouseAdapter() {

			public void mouseReleased(MouseEvent e) {

				String label_text = emr_text.getSelectedText();
				if (label_text == null)
					return;

				valueDialog(string_value_description);
				if (!value_valid)
					return;

				int end_offset = emr_text.getCaretPosition();
				int start_offset = emr_text.getCaretPosition() - label_text.length();
				ToothMap tooth_map = null;
				if (dp_des.isToothRelated())
					tooth_map = findValidToothMap(start_offset);

				StringPropertyValue string_value = new StringPropertyValue(String.valueOf(value));
				PropertyLabel label = new PropertyLabel(start_offset, end_offset, label_text,
						dp, string_value, LabelModifier.None, tooth_map);
				DefaultTableModel label_table_model = (DefaultTableModel) label_table.getModel();
				label_table_model.addRow(new Object[]{

						tooth_map,
						label_text,
						dp.getLocalName(),
						value,
						string_value_description,
						label.getModifier()
				});
				label_list.add(label);

				if (chckbx_show_all_labels.isSelected())
					try {
						setEMRTextColorAllLabels();
					} catch (BadLocationException e1) {
						e1.printStackTrace();
					}
			}
		});
	}

	private void addPropertyAndValues(JComponent menu, DatatypeProperty dp) throws PropertyValueException {

		List<? extends OntResource> value_ranges = dp.listRange().toList();

		JMenu property_menu = new JMenu(dp.getLocalName());
		property_menu.setFont(new Font("微软雅黑", Font.PLAIN, 18));
		menu.add(property_menu);
		this.property_menu_map.put(dp, property_menu);

		for (OntResource value_range : value_ranges) {

			if (value_range.equals(XSD.xboolean))
				addBooleanValues(property_menu, dp);
			else if (OntFunc.isRDFList(value_range))
				addListValues(property_menu, dp);
			else if (value_range.equals(XSD.xint))
				addIntValue(property_menu, dp);
			else if (value_range.equals(XSD.xdouble))
				addDoubleValue(property_menu, dp);
			else if (value_range.equals(XSD.xstring))
				addStringValue(property_menu, dp);
			else
				System.out.println("unknown value type: " + value_range.getLocalName() + " at property " + dp.getLocalName());
		}
	}

	private void addEMRTextPopup(java.awt.Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {

			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger())
					showMenu(e);
			}

			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger())
					showMenu(e);
			}

			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}

	private void setEMRTextColor(PropertyLabel label) throws BadLocationException {

		int start_offset = label.getStartOffset();
		int end_offset = label.getEndOffset();

		SimpleAttributeSet red = new SimpleAttributeSet();
		StyleConstants.setForeground(red, Color.RED);
		SimpleAttributeSet black = new SimpleAttributeSet();
		StyleConstants.setForeground(black, Color.BLACK);
		SimpleAttributeSet green = new SimpleAttributeSet();
		StyleConstants.setForeground(green, Color.GREEN);

		DefaultStyledDocument doc = (DefaultStyledDocument) emr_text.getDocument();
		int doc_length = doc.getLength();

		String text1 = doc.getText(0, start_offset);
		doc.replace(0, start_offset, text1, black);

		ToothMap tooth_map = label.getToothMap();
		if (tooth_map != null) {

			int t_map_start = tooth_map.getStartIndex();
			int t_map_end = tooth_map.getEndIndex();
			String tooth_map_text = doc.getText(t_map_start, t_map_end - t_map_start);
			doc.replace(t_map_start, t_map_end - t_map_start, tooth_map_text, green);
		}

		String text2 = doc.getText(start_offset, end_offset - start_offset);
		doc.replace(start_offset, end_offset - start_offset, text2, red);

		String text3 = doc.getText(end_offset, doc_length - end_offset);
		doc.replace(end_offset, doc.getLength() - end_offset, text3, black);

		emr_text.setCaretPosition(start_offset);
	}

	private void setEMRTextColorToothMapItem(PropertyLabel label, ToothMap tooth_map) throws BadLocationException {

		if (tooth_map == null)
			return;

		int start_offset = label.getStartOffset();
		int end_offset = label.getEndOffset();

		int t_map_start = tooth_map.getStartIndex();
		int t_map_end = tooth_map.getEndIndex();

		SimpleAttributeSet red = new SimpleAttributeSet();
		StyleConstants.setForeground(red, Color.RED);
		SimpleAttributeSet black = new SimpleAttributeSet();
		StyleConstants.setForeground(black, Color.BLACK);
		SimpleAttributeSet green = new SimpleAttributeSet();
		StyleConstants.setForeground(green, Color.GREEN);

		DefaultStyledDocument doc = (DefaultStyledDocument) emr_text.getDocument();
		resetEMRText();

		String tooth_map_text = doc.getText(t_map_start, t_map_end - t_map_start);
		doc.replace(t_map_start, t_map_end - t_map_start, tooth_map_text, green);

		String text2 = doc.getText(start_offset, end_offset - start_offset);
		doc.replace(start_offset, end_offset - start_offset, text2, red);

		emr_text.setCaretPosition(start_offset);
	}

	private void setEMRTextColorAllLabels() throws BadLocationException {

		SimpleAttributeSet red = new SimpleAttributeSet();
		StyleConstants.setForeground(red, Color.RED);
		SimpleAttributeSet black = new SimpleAttributeSet();
		StyleConstants.setForeground(black, Color.BLACK);

		List<PropertyLabel> labels = this.label_list;
		DefaultStyledDocument doc = (DefaultStyledDocument) emr_text.getDocument();

		int first_pos = -1;
		resetEMRText();
		for (PropertyLabel label : labels) {

			int start_offset = label.getStartOffset();
			int end_offset = label.getEndOffset();

			if (first_pos == -1)
				first_pos = start_offset;

			String label_text = doc.getText(start_offset, end_offset - start_offset);
			doc.replace(start_offset, end_offset - start_offset, label_text, red);
		}

		if (first_pos != -1)
			emr_text.setCaretPosition(first_pos);
	}

	private void resetEMRText() throws BadLocationException {

		SimpleAttributeSet black = new SimpleAttributeSet();
		StyleConstants.setForeground(black, Color.BLACK);

		DefaultStyledDocument doc = (DefaultStyledDocument) emr_text.getDocument();
		int old_pos = emr_text.getCaretPosition();
		int text_length = doc.getLength();
		String text = doc.getText(0, text_length);
		doc.replace(0, text_length, text, black);
		emr_text.setCaretPosition(old_pos);
	}

	private ToothMap findValidToothMap(int sentence_start) {

		int i = 0;
		for (; i < tooth_maps.size(); i++) {

			ToothMap tooth_map = tooth_maps.get(i);
			int tooth_map_start = tooth_map.getStartIndex();
			if (tooth_map_start > sentence_start)
				break;
		}

		if (i == 0)
			return null;
		else
			return tooth_maps.get(i - 1);
	}

	private void updateLabelList() {
		List<PropertyLabel> new_lable_list = new ArrayList<>();
		for (int num_row = 0; num_row < label_table.getRowCount(); num_row++) {
			int start_offset = -1;
			int end_offset = -1;
			String property_text = null;
			String property_str = null;
			DatatypeProperty property = null;
			String value_type = null;
			String value_str = null;
			PropertyValue value = null;
			LabelModifier modifier = null;
			ToothMap tooth_map = null;


			start_offset = label_list.get(0).getStartOffset();
			end_offset = label_list.get(0).getEndOffset();
			if (label_table.getModel().getValueAt(num_row, 0) != null) {
				if (label_table.getModel().getValueAt(num_row, 0).getClass() == ToothMap.class) {
					tooth_map = (ToothMap) label_table.getModel().getValueAt(num_row, 0);
				} else {
					tooth_map = new ToothMap(
							(String) label_table.getModel().getValueAt(num_row, 0),
							label_list.get(num_row).getStartOffset(),
							label_list.get(num_row).getEndOffset());
				}
			}
			if (label_table.getModel().getValueAt(num_row, 1) != null) {
				property_text = (String) label_table.getModel().getValueAt(num_row, 1);
			}
			if (label_table.getModel().getValueAt(num_row, 2) != null) {
				property_str = (String) label_table.getModel().getValueAt(num_row, 2);
				property = this.dental_ont.getDatatypeProperty(OntFunc.prefix + property_str);
			}
			if (label_table.getModel().getValueAt(num_row, 3) != null) {
				if (label_table.getModel().getValueAt(num_row, 3) instanceof PropertyValue) {
					value = (PropertyValue) label_table.getModel().getValueAt(num_row, 3);
				} else {
					value_str = (String) label_table.getModel().getValueAt(num_row, 3);
					String NS = "http://www.w3.org/2001/XMLSchema#";
					if (property.hasRange(dental_ont.getOntResource(NS + "boolean"))) {
						if (value_str.equals("0") || value_str.equals("false")) {
							value = new BooleanPropertyValue(false);
						} else if (value_str.equals("1") || value_str.equals("true")) {
							value = new BooleanPropertyValue(true);
						}
					} else if (property.hasRange(dental_ont.getOntResource(NS + "list"))) {
						value = new ListPropertyValue(Integer.valueOf(value_str));
					} else if (property.hasRange(dental_ont.getOntResource(NS + "int"))) {
						value = new IntPropertyValue(Integer.valueOf(value_str));
					} else if (property.hasRange(dental_ont.getOntResource(NS + "double"))) {
						value = new DoublePropertyValue(Double.valueOf(value_str));
					} else if (property.hasRange(dental_ont.getOntResource(NS + "string"))) {
						value = new StringPropertyValue(value_str);
					}
				}
			}
			if (label_table.getModel().getValueAt(num_row, 5) != null) {
				if (label_table.getModel().getValueAt(num_row, 5).getClass() == LabelModifier.class) {
					modifier = (LabelModifier) label_table.getModel().getValueAt(num_row, 5);
				} else {
					modifier = LabelModifier.valueOf((String) label_table.getModel().getValueAt(num_row, 5));
				}
			}
			PropertyLabel label = new PropertyLabel(
					start_offset, end_offset, property_text, property, value, modifier, tooth_map);
			new_lable_list.add(label);
		}
		label_list.clear();
		label_list.addAll(new_lable_list);
	}

	private void writeLabelsAsXml(File label_xml_file) throws ParserConfigurationException,
			PropertyValueException, TransformerException, FileNotFoundException {

		if (this.label_list == null)
			return;

		PropertyLabel[] label_array = this.label_list.toArray(new PropertyLabel[0]);
		Arrays.sort(label_array, new LabelPosComparator());

		org.w3c.dom.Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		Element root = document.createElement("labels");
		document.appendChild(root);

		for (PropertyLabel label : label_array) {

			Element label_element = document.createElement("label");
			root.appendChild(label_element);

			ToothMap tooth_map = label.getToothMap();
			if (tooth_map != null) {

				String tooth_map_str = String.valueOf(tooth_map.toString());
				Element tooth_map_str_element = document.createElement("tooth_map_str");
				tooth_map_str_element.appendChild(document.createTextNode(tooth_map_str));
				label_element.appendChild(tooth_map_str_element);
			}

			String text = label.getPropertyText();
			Element text_element = document.createElement("text");
			text_element.appendChild(document.createTextNode(text));
			label_element.appendChild(text_element);

			String property = label.getProperty().getLocalName();
			Element property_element = document.createElement("property");
			property_element.appendChild(document.createTextNode(property));
			label_element.appendChild(property_element);

			String property_value_type = null;
			String property_value = null;
			PropertyValue value = label.getPropertyValue();
			if (value.isBooleanValue()) {

				property_value_type = "boolean";
				if (value.getBooleanValue())
					property_value = "1";
				else
					property_value = "0";
			} else if (value.isListValue()) {

				property_value_type = "list_int";
				property_value = String.valueOf(value.getListValue());
			} else if (value.isIntValue()) {

				property_value_type = "int";
				property_value = String.valueOf(value.getIntValue());
			} else if (value.isDoubleValue()) {

				property_value_type = "double";
				property_value = String.valueOf(value.getDoubleValue());
			} else if (value.isStringValue()) {

				property_value_type = "string";
				property_value = String.valueOf(value.getStringValue());
			} else {
			}
			Element value_type_element = document.createElement("value_type");
			value_type_element.appendChild(document.createTextNode(property_value_type));
			label_element.appendChild(value_type_element);
			Element value_element = document.createElement("value");
			value_element.appendChild(document.createTextNode(property_value));
			label_element.appendChild(value_element);

			LabelModifier modifier = label.getModifier();
			if (modifier != null) {

				String modifier_str = label.getModifier().name();
				Element modifier_element = document.createElement("modifier");
				modifier_element.appendChild(document.createTextNode(modifier_str));
				label_element.appendChild(modifier_element);
			}

			String start_offset = String.valueOf(label.getStartOffset());
			Element start_offset_element = document.createElement("start_offset");
			start_offset_element.appendChild(document.createTextNode(start_offset));
			label_element.appendChild(start_offset_element);

			String end_offset = String.valueOf(label.getEndOffset());
			Element end_offset_element = document.createElement("end_offset");
			end_offset_element.appendChild(document.createTextNode(end_offset));
			label_element.appendChild(end_offset_element);

			if (tooth_map != null) {

				String tooth_map_start_offset = String.valueOf(tooth_map.getStartIndex());
				Element tooth_map_start_element = document.createElement("tooth_map_start_offset");
				tooth_map_start_element.appendChild(document.createTextNode(tooth_map_start_offset));
				label_element.appendChild(tooth_map_start_element);

				String tooth_map_end_offset = String.valueOf(tooth_map.getEndIndex());
				Element tooth_map_end_element = document.createElement("tooth_map_end_offset");
				tooth_map_end_element.appendChild(document.createTextNode(tooth_map_end_offset));
				label_element.appendChild(tooth_map_end_element);
			}
		}

		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		DOMSource source = new DOMSource(document);
		transformer.setOutputProperty(OutputKeys.ENCODING, "utf8");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		PrintWriter pw = new PrintWriter(new FileOutputStream(label_xml_file));
		StreamResult result = new StreamResult(pw);
		transformer.transform(source, result);
	}

	private void readLabelsFromXml(File label_xml_file) throws ParserConfigurationException,
			SAXException, IOException, PropertyValueException {

		//Map<OntProperty, PropertyDescription> property_descriptions = this.des.property_descriptions;
//		if (label_xml_file == null) {
//			label_table = null;
//			return;
//		}

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		org.w3c.dom.Document document = db.parse(label_xml_file);

		Node all_labels = document.getFirstChild();
		NodeList label_node_list = all_labels.getChildNodes();

		for (int i = 0; i < label_node_list.getLength(); i++) {

			int start_offset = -1;
			int end_offset = -1;
			String property_text = null;
			String property_str = null;
			DatatypeProperty property = null;
			String value_type = null;
			String value_str = null;
			PropertyValue value = null;
			LabelModifier modifier = null;
			int tooth_map_start_offset = -1;
			int tooth_map_end_offset = -1;
			String tooth_map_str = null;

			Node label_node = label_node_list.item(i);
			NodeList label_node_fields = label_node.getChildNodes();
			if (label_node_fields.getLength() == 0)
				continue;

			for (int j = 0; j < label_node_fields.getLength(); j++) {

				Node label_node_field = label_node_fields.item(j);

				String field_name = label_node_field.getNodeName();
				String field_content = label_node_field.getTextContent();

				if (field_name.equals("property")) {

					property_str = field_content;
					property = this.dental_ont.getDatatypeProperty(OntFunc.prefix + field_content);
				}
				if (field_name.equals("text"))
					property_text = field_content;
				if (field_name.equals("value_type"))
					value_type = field_content;
				if (field_name.equals("value"))
					value_str = field_content;
				if (field_name.equals("modifier"))
					modifier = LabelModifier.valueOf(field_content);
				if (field_name.equals("start_offset"))
					start_offset = Integer.valueOf(field_content);
				if (field_name.equals("end_offset"))
					end_offset = Integer.valueOf(field_content);
				if (field_name.equals("tooth_map_start_offset"))
					tooth_map_start_offset = Integer.valueOf(field_content);
				if (field_name.equals("tooth_map_end_offset"))
					tooth_map_end_offset = Integer.valueOf(field_content);
				if (field_name.equals("tooth_map_str"))
					tooth_map_str = field_content;
			}

			if (property == null) {

				System.out.println("unknown property: " + property_str + " in " + label_xml_file.getName());
				continue;
			}

			if (property_str.equals("is_missing")) {
				if (tooth_map_str != null)
					is_missing_str = tooth_map_str;
			}

			if (value_type.equals("boolean")) {

				if (value_str.equals("0"))
					value = new BooleanPropertyValue(false);
				else if (value_str.equals("1"))
					value = new BooleanPropertyValue(true);
				else
					throw new PropertyValueException("can not parse boolean value: " + value_str);
			} else if (value_type.equals("list_int"))
				value = new ListPropertyValue(Integer.valueOf(value_str));
			else if (value_type.equals("int"))
				value = new IntPropertyValue(Integer.valueOf(value_str));
			else if (value_type.equals("double"))
				value = new DoublePropertyValue(Double.valueOf(value_str));
			else if (value_type.equals("string"))
				value = new StringPropertyValue(value_str);
			else
				throw new PropertyValueException("unknown value type: " + value_type);

			PropertyLabel label = null;
			ToothMap tooth_map = null;
			if (tooth_map_start_offset != -1)
				tooth_map = new ToothMap(tooth_map_str, tooth_map_start_offset, tooth_map_end_offset);

			PropertyDescription dp_des = this.des.property_descriptions.get(property);
			if (tooth_map == null && dp_des != null && dp_des.isToothRelated() &&
					this.locate_tooth_map_when_reading)
				tooth_map = this.findValidToothMap(start_offset);

			label = new PropertyLabel(start_offset, end_offset, property_text, property, value, modifier, tooth_map);
			this.label_list.add(label);
			PropertyDescription property_des = this.des.property_descriptions.get(property);
			String value_description = null;

			if (value_type.equals("boolean")) {

				Map<Boolean, String> boolean_value_descriptions = property_des.getBooleanValueDescriptions();
				if (value_str.equals("0"))
					value_description = boolean_value_descriptions.get(false);
				else if (value_str.equals("1"))
					value_description = boolean_value_descriptions.get(true);
				else
					throw new PropertyValueException("can not parse boolean value: " + value_str);
			} else if (value_type.equals("list_int")) {

				Map<Integer, String> list_value_descriptions = property_des.getListValueDescriptions();
				value_description = list_value_descriptions.get(Integer.valueOf(value_str));
			} else if (value_type.equals("int"))
				value_description = property_des.getIntValueDescription();
			else if (value_type.equals("double"))
				value_description = property_des.getDoubleValueDescription();
			else if (value_type.equals("string"))
				value_description = property_des.getStringValueDescription();
			else
				throw new PropertyValueException("unknown value type: " + value_type);

			DefaultTableModel label_table_model = (DefaultTableModel) label_table.getModel();
			label_table_model.addRow(new Object[]{

					tooth_map,
					property_text,
					property.getLocalName(),
					value,
					value_description,
					modifier
			});
		}
	}

	private void valueDialog(String value_description) {

		value_valid = false;
		value_dialog = new JDialog();

		value_dialog.setTitle("value");
		value_dialog.setModalityType(ModalityType.APPLICATION_MODAL);
		value_dialog.pack();
		Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
		value_dialog.setBounds((screen_size.width - 300) / 2,
				(screen_size.height - 170) / 2, 500, 170);
		value_dialog.setResizable(true);

		JLabel label = new JLabel("说明：" + value_description);
		label.setFont(new Font("微软雅黑", Font.PLAIN, 18));
		JTextField value_field = new JTextField();
		value_field.setFont(new Font("微软雅黑", Font.PLAIN, 18));
		JButton ok_button = new JButton("确定");
		ok_button.addMouseListener(new MouseAdapter() {

			public void mouseReleased(MouseEvent e) {

				String value_str = value_field.getText();
				if (value_str == null || value_str.length() == 0)
					return;

				value = value_str;
				value_valid = true;
				value_dialog.setVisible(false);
			}
		});

		value_dialog.getContentPane().add(label, BorderLayout.NORTH);
		value_dialog.getContentPane().add(value_field, BorderLayout.CENTER);
		JPanel south_panel = new JPanel();
		value_dialog.getContentPane().add(south_panel, BorderLayout.SOUTH);
		south_panel.add(ok_button);

		value_dialog.setVisible(true);
	}

	private void updateToothMapItems() {

		this.label_menu.remove(this.tooth_map_menu);
		this.tooth_map_items.clear();

		JMenu tooth_map_menu = new JMenu("牙位图");
		this.tooth_map_menu = tooth_map_menu;
		label_menu.add(tooth_map_menu);
		tooth_map_menu.add(remove_tooth_map);

		for (ToothMap tooth_map : this.tooth_maps) {

			String tooth_map_str = tooth_map.getToothMapString().trim();
			JMenuItem tooth_map_item = new JMenuItem(tooth_map_str);

			tooth_map_item.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseReleased(MouseEvent arg0) {

					DefaultTableModel label_table_model = (DefaultTableModel) label_table.getModel();
					int row_index = label_table.getSelectedRow();
					if (row_index == -1)
						return;
					label_table_model.setValueAt(tooth_map_str, row_index, 0);
					label_list.get(row_index).setToothMap(tooth_map);
				}

				public void mouseEntered(MouseEvent arg0) {

					int row_index = label_table.getSelectedRow();
					if (row_index == -1)
						return;
					PropertyLabel label = label_list.get(row_index);
					if (!chckbx_show_all_labels.isSelected()) {

						try {
							setEMRTextColorToothMapItem(label, tooth_map);
						} catch (BadLocationException e) {
							e.printStackTrace();
						}
					}
				}

				public void mouseExited(MouseEvent arg0) {

					int row_index = label_table.getSelectedRow();
					if (row_index == -1)
						return;
					PropertyLabel label = label_list.get(row_index);
					if (!chckbx_show_all_labels.isSelected()) {

						try {
							setEMRTextColor(label);
						} catch (BadLocationException e) {
							e.printStackTrace();
						}
					}
				}

			});

			tooth_map_item.setFont(new Font("微软雅黑", Font.PLAIN, 20));
			tooth_map_menu.add(tooth_map_item);
			this.tooth_map_items.add(tooth_map_item);
		}
	}

	public void planToOwl(RPDPlan plan, OntModel resOnt) throws exceptions.rpd.RuleException {
//		FileWriter out = new FileWriter("instance_model.owl");
//		template_model.write(out);
//		File instance_model_file = new File("instance_model.owl");
//		OntModel resOnt = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
//		resOnt.read("file:" + instance_model_file.getCanonicalPath());
		String SOURCE = "http://www.semanticweb.org/msiip/ontologies/CDSSinRPD";
		String NS = SOURCE + "#";
		int indCount = 0;

		Map<ArrayList<Tooth>, Set<rpd.components.Component>> tooth_components = plan.getToothComponents();
		for (ArrayList<Tooth> tooth_pos : tooth_components.keySet())
			for (rpd.components.Component component : tooth_components.get(tooth_pos)) {
//			System.out.println("component = " + component + "\t\n");
//			System.out.println("tooth_pos = " + tooth_pos + "\t\n");
				indCount++;
				String className = component.getClass().getName();
				switch (className) {
					case "rpd.components.AkerClasp":
						addAkerClaspToOwl(component, resOnt, NS, indCount);
						break;
					case "rpd.components.WroughtWireClasp":
						addWroughtWireClaspToOwl(component, resOnt, NS, indCount);
						break;
					case "rpd.components.CombinationClasp":
						addCombinationClaspToOwl(component, resOnt, NS, indCount);
						break;
					case "rpd.components.CanineClasp":
						addCanineClaspToOwl(component, resOnt, NS, indCount);
						break;
					case "rpd.components.CanineAkerClasp":
						addCanineAkerClaspToOwl(component, resOnt, NS, indCount);
						break;
					case "rpd.components.HalfHalfClasp":
						addHalfHalfClaspToOwl(component, resOnt, NS, indCount);
						break;
					case "rpd.components.BackActionClasp":
						addBackActionClaspToOwl(component, resOnt, NS, indCount);
						break;
					case "rpd.components.ReverseBackActionClasp":
						addReverseBackActionClaspToOwl(component, resOnt, NS, indCount);
						break;
					case "rpd.components.RingClasp":
						addRingClaspToOwl(component, resOnt, NS, indCount);
						break;
					case "rpd.components.CombinedClasp":
						addCombinedClaspToOwl(component, resOnt, NS, indCount);
						break;
					case "rpd.components.EmbrasureClasp":
						addEmbrasureClaspToOwl(component, resOnt, NS, indCount);
						break;
					case "rpd.components.ContinuousClasp":
						addContinuousClaspToOwl(component, resOnt, NS, indCount);
						break;
					case "rpd.components.RPAClasp":
						addRPAClaspToOwl(component, resOnt, NS, indCount);
						break;
					case "rpd.components.OcclusalRest":
						addOcclusalRestToOwl(component, resOnt, NS, indCount);
						break;
					case "rpd.components.LingualRest":
						addLingualRestToOwl(component, resOnt, NS, indCount);
						break;
					case "rpd.components.SinglePalatalStrapConnector":
						addSinglePalatalStrapConnectorToOwl(component, resOnt, NS, indCount);
						break;
					case "rpd.components.CombinationAnteriorPosteriorPalatalStrapConnector":
						addCombinationAnteriorPosteriorPalatalStrapConnectorToOwl(component, resOnt, NS, indCount);
						break;
					case "rpd.components.PalatalPlateConnector":
						addPalatalPlateConnectorToOwl(component, resOnt, NS, indCount);
						break;
					case "rpd.components.FullPalatalPlateConnector":
						addFullPalatalPlateConnectorToOwl(component, resOnt, NS, indCount);
						break;
					case "rpd.components.ModifiedPalatalPlateConnector":
						addModifiedPalatalPlateConnectorToOwl(component, resOnt, NS, indCount);
						break;
					case "rpd.components.LingualBarConnector":
						addLingualBarConnectorToOwl(component, resOnt, NS, indCount);
						break;
					case "rpd.components.LingualPlateConnector":
						addLingualPlateConnectorToOwl(component, resOnt, NS, indCount);
						break;
					case "rpd.components.DentureBase":
						addDentureBase(component, resOnt, NS, indCount);
						break;
				}
			}
		for (EdentulousSpace edentulousSpace : plan.getEdentulousSpaces()) {
			indCount++;
			addEdentulousSpace(edentulousSpace, resOnt, NS, indCount);
		}
		return;
	}

	public void addEdentulousSpace(EdentulousSpace edentulousSpace, OntModel model, String NS, int indCount) {

		OntClass edentulousSpaceClass = model.getOntClass(NS + "edentulous_space");
		Individual indEdentulousSpace = model.createIndividual(NS + indCount, edentulousSpaceClass);
		OntProperty component_position = model.getObjectProperty(NS + "component_position");
		indEdentulousSpace.addProperty(
				component_position,
				model.getIndividual(NS + edentulousSpace.getLeftMost().toString()));
//		indEdentulousSpace.addProperty(
//				component_position,
//				model.getIndividual(NS + edentulousSpace.getRightMost().toString()));
		if (!edentulousSpace.getRightMost().equals(edentulousSpace.getLeftMost())) {
			indEdentulousSpace.addProperty(
					component_position,
					model.getIndividual(NS + edentulousSpace.getRightMost().toString()));
		}

		return;
	}

	public void addDentureBase(rpd.components.Component base, OntModel model, String NS, int indCount) {
		DentureBase dentureBase = (DentureBase) base;
		OntClass dentureBaseClass = model.getOntClass(NS + "denture_base");
		Individual indDentureBase = model.createIndividual(NS + indCount, dentureBaseClass);
		setComponentToothPos(model, indDentureBase, dentureBase.getToothPos(), NS);
	}

	public void addAkerClaspToOwl(rpd.components.Component clasp, OntModel model, String NS, int indCount) {

		AkerClasp akerClasp = (AkerClasp) clasp;
		OntClass akerClaspClass = model.getOntClass(NS + "aker_clasp");
		Individual indAkerClasp = model.createIndividual(NS + indCount, akerClaspClass);
		setClaspTipDirection(model, indAkerClasp, akerClasp.getTipDirection(), NS);
		setClaspMaterial(model, indAkerClasp, akerClasp.getMaterial(), NS);
		setComponentToothPos(model, indAkerClasp, akerClasp.getToothPos(), NS);
		return;
	}

	public void addWroughtWireClaspToOwl(rpd.components.Component clasp, OntModel model, String NS, int indCount) {

		WroughtWireClasp wroughtWireClasp = (WroughtWireClasp) clasp;
		OntClass wroughtWireClaspClass = model.getOntClass(NS + "wrought_wire_clasp");
		Individual indWroughtWireClasp = model.createIndividual(NS + indCount, wroughtWireClaspClass);
		setClaspTipDirection(model, indWroughtWireClasp, wroughtWireClasp.getTipDirection(), NS);
		setClaspMaterial(model, indWroughtWireClasp, wroughtWireClasp.getMaterial(), NS);
		setComponentToothPos(model, indWroughtWireClasp, wroughtWireClasp.getToothPos(), NS);
		return;
	}

	public void addCombinationClaspToOwl(rpd.components.Component clasp, OntModel model, String NS, int indCount) {
		CombinationClasp combinationClasp = (CombinationClasp) clasp;
		OntClass combinationClaspClass = model.getOntClass(NS + "combination_clasp");
		Individual indCombinationClasp = model.createIndividual(NS + indCount, combinationClaspClass);
		setClaspTipDirection(model, indCombinationClasp, combinationClasp.getTipDirection(), NS);
		setComponentToothPos(model, indCombinationClasp, combinationClasp.getToothPos(), NS);
		return;
	}

	public void addCanineClaspToOwl(rpd.components.Component clasp, OntModel model, String NS, int indCount) {

		CanineClasp canineClasp = (CanineClasp) clasp;
		OntClass canineClaspClass = model.getOntClass(NS + "canine_clasp");
		Individual indCanineClasp = model.createIndividual(NS + indCount, canineClaspClass);
		setClaspTipDirection(model, indCanineClasp, canineClasp.getTipDirection(), NS);
		setClaspMaterial(model, indCanineClasp, canineClasp.getMaterial(), NS);
		return;
	}

	public void addCanineAkerClaspToOwl(rpd.components.Component clasp, OntModel model, String NS, int indCount) {

		CanineAkerClasp canineAkerClasp = (CanineAkerClasp) clasp;
		OntClass canineAkerClaspClass = model.getOntClass(NS + "canine_aker_clasp");
		Individual indCanineAkerClasp = model.createIndividual(NS + indCount, canineAkerClaspClass);
		setClaspTipDirection(model, indCanineAkerClasp, canineAkerClasp.getTipDirection(), NS);
		setClaspMaterial(model, indCanineAkerClasp, canineAkerClasp.getMaterial(), NS);
		setComponentToothPos(model, indCanineAkerClasp, canineAkerClasp.getToothPos(), NS);
		return;
	}

	public void addHalfHalfClaspToOwl(rpd.components.Component clasp, OntModel model, String NS, int indCount) {

		HalfHalfClasp halfHalfClasp = (HalfHalfClasp) clasp;
		OntClass halfHalfClaspClass = model.getOntClass(NS + "half_and_half_clasp");
		Individual indHalfHalfClasp = model.createIndividual(NS + indCount, halfHalfClaspClass);
		setClaspMaterial(model, indHalfHalfClasp, halfHalfClasp.getMaterial(), NS);
		setComponentToothPos(model, indHalfHalfClasp, halfHalfClasp.getToothPos(), NS);
		return;
	}

	public void addBackActionClaspToOwl(rpd.components.Component clasp, OntModel model, String NS, int indCount) {

		BackActionClasp backActionClasp = (BackActionClasp) clasp;
		OntClass backActionClaspClass = model.getOntClass(NS + "back_action_clasp");
		Individual indBackActionClasp = model.createIndividual(NS + indCount, backActionClaspClass);
		setClaspTipDirection(model, indBackActionClasp, backActionClasp.getTipDirection(), NS);
		setClaspMaterial(model, indBackActionClasp, backActionClasp.getMaterial(), NS);
		setComponentToothPos(model, indBackActionClasp, backActionClasp.getToothPos(), NS);
		return;
	}

	public void addReverseBackActionClaspToOwl(
			rpd.components.Component clasp, OntModel model, String NS, int indCount) {

		ReverseBackActionClasp reverseBackActionClasp = (ReverseBackActionClasp) clasp;
		OntClass reverseBackActionClaspClass = model.getOntClass(NS + "reverse_back_action_clasp");
		Individual indReverseBackActionClasp = model.createIndividual(NS + indCount, reverseBackActionClaspClass);
		setClaspTipDirection(model, indReverseBackActionClasp, reverseBackActionClasp.getTipDirection(), NS);
		setClaspMaterial(model, indReverseBackActionClasp, reverseBackActionClasp.getMaterial(), NS);
		setComponentToothPos(model, indReverseBackActionClasp, reverseBackActionClasp.getToothPos(), NS);
		return;
	}

	public void addRingClaspToOwl(rpd.components.Component clasp, OntModel model, String NS, int indCount) {

		RingClasp ringClasp = (RingClasp) clasp;
		OntClass ringClaspClass = model.getOntClass(NS + "ring_clasp");
		Individual indRingClasp = model.createIndividual(NS + indCount, ringClaspClass);
		setClaspTipDirection(model, indRingClasp, ringClasp.getTipDirection(), NS);
		setClaspMaterial(model, indRingClasp, ringClasp.getMaterial(), NS);
		setComponentToothPos(model, indRingClasp, ringClasp.getToothPos(), NS);
		return;
	}

	public void addCombinedClaspToOwl(rpd.components.Component clasp, OntModel model, String NS, int indCount) {

		CombinedClasp combinedClasp = (CombinedClasp) clasp;
		OntClass combinedClaspClass = model.getOntClass(NS + "combined_clasp");
		Individual indCombinedClasp = model.createIndividual(NS + indCount, combinedClaspClass);
		setClaspMaterial(model, indCombinedClasp, combinedClasp.getMaterial(), NS);
		setComponentToothPos(model, indCombinedClasp, combinedClasp.getToothPos(), NS);
		return;
	}

	public void addEmbrasureClaspToOwl(rpd.components.Component clasp, OntModel model, String NS, int indCount) {

		EmbrasureClasp embrasureClasp = (EmbrasureClasp) clasp;
		OntClass embrasureClaspClass = model.getOntClass(NS + "embrasure_clasp");
		Individual indEmbrasureClasp = model.createIndividual(NS + indCount, embrasureClaspClass);
		setClaspMaterial(model, indEmbrasureClasp, embrasureClasp.getMaterial(), NS);
		setComponentToothPos(model, indEmbrasureClasp, embrasureClasp.getToothPos(), NS);
		return;
	}

	public void addContinuousClaspToOwl(rpd.components.Component clasp, OntModel model, String NS, int indCount) {

		ContinuousClasp continuousClasp = (ContinuousClasp) clasp;
		OntClass continuousClaspClass = model.getOntClass(NS + "continuous_clasp");
		Individual indContinuousClasp = model.createIndividual(NS + indCount, continuousClaspClass);
		setClaspMaterial(model, indContinuousClasp, continuousClasp.getMaterial(), NS);
		setComponentToothPos(model, indContinuousClasp, continuousClasp.getToothPos(), NS);
		return;
	}

	public void addRPAClaspToOwl(rpd.components.Component clasp, OntModel model, String NS, int indCount) {

		RPAClasp RPAClasp = (RPAClasp) clasp;
		OntClass RPAClaspClass = model.getOntClass(NS + "RPA_clasps");
		Individual indRPAClasp = model.createIndividual(NS + indCount, RPAClaspClass);
		setClaspTipDirection(model, indRPAClasp, RPAClasp.getTipDirection(), NS);
		setClaspMaterial(model, indRPAClasp, RPAClasp.getMaterial(), NS);
		setComponentToothPos(model, indRPAClasp, RPAClasp.getToothPos(), NS);
		return;
	}

	public void addOcclusalRestToOwl(rpd.components.Component rest, OntModel model, String NS, int indCount) {

		OcclusalRest occlusalRest = (OcclusalRest) rest;
		OntClass occlusalRestClass = model.getOntClass(NS + "occlusal_rest");
		Individual indOcclusalRest = model.createIndividual(NS + indCount, occlusalRestClass);
		setRestMesialOrDistal(model, indOcclusalRest, occlusalRest.getMesialOrDistal(), NS);
		setComponentToothPos(model, indOcclusalRest, occlusalRest.getToothPos(), NS);
		return;
	}

	public void addLingualRestToOwl(rpd.components.Component rest, OntModel model, String NS, int indCount) {

		LingualRest lingualRest = (LingualRest) rest;
		OntClass lingualRestClass = model.getOntClass(NS + "lingual_rest");
		Individual indLingualRest = model.createIndividual(NS + indCount, lingualRestClass);
		setComponentToothPos(model, indLingualRest, lingualRest.getToothPos(), NS);
		return;
	}

	public void addSinglePalatalStrapConnectorToOwl(
			rpd.components.Component connector, OntModel model, String NS, int indCount) {

		SinglePalatalStrapConnector singlePalatalStrapConnector = (SinglePalatalStrapConnector) connector;
		OntClass singlePalatalStrapConnectorClass = model.getOntClass(NS + "single_palatal_strap");
		Individual indSinglePalatalStrapConnector
				= model.createIndividual(NS + indCount, singlePalatalStrapConnectorClass);
		setComponentToothPos(model, indSinglePalatalStrapConnector, singlePalatalStrapConnector.getToothPos(), NS);
		return;
	}

	public void addCombinationAnteriorPosteriorPalatalStrapConnectorToOwl(
			rpd.components.Component connector, OntModel model, String NS, int indCount) {

		CombinationAnteriorPosteriorPalatalStrapConnector CAPPSConnector
				= (CombinationAnteriorPosteriorPalatalStrapConnector) connector;
		OntClass CAPPSConnectorClass = model.getOntClass(NS + "combination_anterior_posterior_palatal_strap");
		Individual indCAPPSConnector = model.createIndividual(NS + indCount, CAPPSConnectorClass);
		setComponentToothPos(model, indCAPPSConnector, CAPPSConnector.getToothPos(), NS);
		return;
	}

	public void addPalatalPlateConnectorToOwl(
			rpd.components.Component connector, OntModel model, String NS, int indCount) {

		PalatalPlateConnector palatalPlateConnector = (PalatalPlateConnector) connector;
		OntClass palatalPlateConnectorClass = model.getOntClass(NS + "palatal_plate");
		Individual indPalatalPlateConnector = model.createIndividual(NS + indCount, palatalPlateConnectorClass);
		setComponentToothPos(model, indPalatalPlateConnector, palatalPlateConnector.getToothPos(), NS);
		setMajorConnectorLingualConfrontation(
				model, indPalatalPlateConnector, palatalPlateConnector.getLingualConfrontation(), NS);
		return;
	}

	public void addFullPalatalPlateConnectorToOwl(
			rpd.components.Component cnnector, OntModel model, String NS, int indCount) {

		FullPalatalPlateConnector fullPalatalPlateConnector = (FullPalatalPlateConnector) cnnector;
		OntClass fullPalatalPlateConnectorClass = model.getOntClass(NS + "full_palatal_plate");
		Individual indFullPalatalPlateConnector
				= model.createIndividual(NS + indCount, fullPalatalPlateConnectorClass);
		setComponentToothPos(model, indFullPalatalPlateConnector, fullPalatalPlateConnector.getToothPos(), NS);
		setMajorConnectorLingualConfrontation(
				model, indFullPalatalPlateConnector, fullPalatalPlateConnector.getLingualConfrontation(), NS);
		return;
	}

	public void addModifiedPalatalPlateConnectorToOwl(
			rpd.components.Component connector, OntModel model, String NS, int indCount) {

		ModifiedPalatalPlateConnector modifiedPalatalPlateConnector = (ModifiedPalatalPlateConnector) connector;
		OntClass modifiedPalatalPlateConnectorClass = model.getOntClass(NS + "modified_palatal_plate");
		Individual indModifiedPalatalPlateConnector
				= model.createIndividual(NS + indCount, modifiedPalatalPlateConnectorClass);
		setComponentToothPos(model, indModifiedPalatalPlateConnector, modifiedPalatalPlateConnector.getToothPos(), NS);
		setMajorConnectorLingualConfrontation(
				model, indModifiedPalatalPlateConnector, modifiedPalatalPlateConnector.getLingualConfrontation(), NS);
		return;
	}

	public void addLingualBarConnectorToOwl(
			rpd.components.Component connector, OntModel model, String NS, int indCount) {

		LingualBarConnector lingualBarConnector = (LingualBarConnector) connector;
		OntClass lingualBarConnectorClass = model.getOntClass(NS + "lingual_bar");
		Individual indLingualBarConnector = model.createIndividual(NS + indCount, lingualBarConnectorClass);
		setComponentToothPos(model, indLingualBarConnector, lingualBarConnector.getToothPos(), NS);
		return;
	}

	public void addLingualPlateConnectorToOwl(
			rpd.components.Component connector, OntModel model, String NS, int indCount) {

		LingualPlateConnector lingualPlateConnector = (LingualPlateConnector) connector;
		OntClass lingualPlateConnectorClass = model.getOntClass(NS + "lingual_plate");
		Individual indLingualPlateConnector = model.createIndividual(NS + indCount, lingualPlateConnectorClass);
		setComponentToothPos(model, indLingualPlateConnector, lingualPlateConnector.getToothPos(), NS);
		setMajorConnectorLingualConfrontation(
				model, indLingualPlateConnector, lingualPlateConnector.getLingualConfrontation(), NS);
		return;
	}

	public void setClaspTipDirection(OntModel model, Individual indClasp, Position tip_direction, String NS) {

		OntProperty clasp_tip_direction = model.getOntProperty(NS + "clasp_tip_direction");
		if (tip_direction == Mesial) {
			indClasp.addProperty(clasp_tip_direction, model.createTypedLiteral(0));
		} else if (tip_direction == Position.Distal) {
			indClasp.addProperty(clasp_tip_direction, model.createTypedLiteral(1));
		}
	}

	public void setClaspMaterial(OntModel model, Individual indClasp, ClaspMaterial material, String NS) {

		OntProperty clasp_material = model.getOntProperty(NS + "clasp_material");
		if (material == ClaspMaterial.Cast) {
			indClasp.addProperty(clasp_material, model.createTypedLiteral(0));
		} else if (material == ClaspMaterial.WW) {
			indClasp.addProperty(clasp_material, model.createTypedLiteral(1));
		}
	}

	public void setRestMesialOrDistal(OntModel model, Individual indRest, Position mesial_or_distal, String NS) {

		OntProperty rest_mesial_or_distal = model.getOntProperty(NS + "rest_mesial_or_distal");
		if (mesial_or_distal == Mesial) {
			indRest.addProperty(rest_mesial_or_distal, model.createTypedLiteral(0));
		} else if (mesial_or_distal == Position.Distal) {
			indRest.addProperty(rest_mesial_or_distal, model.createTypedLiteral(1));
		}
	}

	public void setComponentToothPos(OntModel model, Individual indComponent, ArrayList<Tooth> tooth_pos, String NS) {

		OntProperty component_position = model.getObjectProperty(NS + "component_position");
		for (Tooth tooth : tooth_pos) {
			indComponent.addProperty(component_position, model.getIndividual(NS + tooth.toString()));
		}
	}

	public void setMajorConnectorLingualConfrontation(
			OntModel model, Individual indMajorConnector, HashSet<Tooth> lingualConfrontation, String NS) {
		OntProperty lingual_confrontation = model.getObjectProperty(NS + "lingual_confrontation");
		for (Tooth tooth:lingualConfrontation) {
			indMajorConnector.addProperty(lingual_confrontation, model.getIndividual(NS + tooth.toString()));
		}
	}

	/**************************************************************************************************************************/

	public rpd.components.AkerClasp modifyAkerClasp(String toothPosition ,Position tip_direction , ClaspMaterial material) {
		int[] strtoToothList = getToothList(toothPosition);
		Tooth toothList = new Tooth(strtoToothList[1], strtoToothList[0]);
		rpd.components.AkerClasp akerClasp = new AkerClasp(toothList, tip_direction, material);
		return akerClasp;
	}

	public rpd.components.AkerClasp modifyAkerClasp(String toothPosition ,Position tip_direction) {
		int[] strtoToothList = getToothList(toothPosition);
		Tooth toothList = new Tooth(strtoToothList[1], strtoToothList[0]);
		rpd.components.AkerClasp akerClasp = new AkerClasp(toothList, tip_direction);
		return akerClasp;
	}

	public rpd.components.EmbrasureClasp modifyEmbrasureClasp(String toothPosition) {
		int[] strtoToothArrayList = getToothArrayList(toothPosition);
		Tooth toothListDB1 = new Tooth(strtoToothArrayList[1],strtoToothArrayList[0]);
		Tooth toothListDB2 = new Tooth(strtoToothArrayList[3],strtoToothArrayList[2]);
		ArrayList<Tooth> toothListDB = new ArrayList<Tooth>(){{add(toothListDB2); add(toothListDB1);}};
		rpd.components.EmbrasureClasp embrasureClasp = new EmbrasureClasp(toothListDB);
		return embrasureClasp;
	}

	public rpd.components.ContinuousClasp modifyContinuousClasp(String toothPosition , ClaspMaterial material) {
		int[] strtoToothArrayList = getToothArrayList(toothPosition);
		Tooth toothListDB1 = new Tooth(strtoToothArrayList[1],strtoToothArrayList[0]);
		Tooth toothListDB2 = new Tooth(strtoToothArrayList[3],strtoToothArrayList[2]);
		ArrayList<Tooth> toothListDB = new ArrayList<Tooth>(){{add(toothListDB2); add(toothListDB1);}};
		rpd.components.ContinuousClasp continuousClasp = new ContinuousClasp(toothListDB,material);
		return continuousClasp;
	}

	public rpd.components.ContinuousClasp modifyContinuousClasp(String toothPosition) {
		int[] strtoToothArrayList = getToothArrayList(toothPosition);
		Tooth toothListDB1 = new Tooth(strtoToothArrayList[1],strtoToothArrayList[0]);
		Tooth toothListDB2 = new Tooth(strtoToothArrayList[3],strtoToothArrayList[2]);
		ArrayList<Tooth> toothListDB = new ArrayList<Tooth>(){{add(toothListDB2); add(toothListDB1);}};
		rpd.components.ContinuousClasp continuousClasp = new ContinuousClasp(toothListDB);
		return continuousClasp;
	}

	public rpd.components.WroughtWireClasp modifyWroughtWireClasp(String toothPosition ,Position tip_direction) {
		int[] strtoToothList = getToothList(toothPosition);
		Tooth toothList = new Tooth(strtoToothList[1], strtoToothList[0]);
		rpd.components.WroughtWireClasp wroughtWireClasp = new WroughtWireClasp(toothList, tip_direction);
		return wroughtWireClasp;
	}
	public rpd.components.OcclusalRest modifyOcclusalRest(String toothPosition ,Position mesial_or_distal) {
		int[] strtoToothList = getToothList(toothPosition);
		Tooth toothList = new Tooth(strtoToothList[1], strtoToothList[0]);
		rpd.components.OcclusalRest occlusalRest = new OcclusalRest(toothList, mesial_or_distal);
		return occlusalRest;
	}

	public rpd.components.CombinationClasp modifyCombinationClasp(String toothPosition ,Position tip_direction) {
		int[] strtoToothList = getToothList(toothPosition);
		Tooth toothList = new Tooth(strtoToothList[1], strtoToothList[0]);
		rpd.components.CombinationClasp combinationClasp = new CombinationClasp(toothList, tip_direction);
		return combinationClasp;
	}

	public rpd.components.CombinationClasp modifyCombinationClasp(String toothPosition) {
		int[] strtoToothList = getToothList(toothPosition);
		Tooth toothList = new Tooth(strtoToothList[1], strtoToothList[0]);
		rpd.components.CombinationClasp combinationClasp = new CombinationClasp(toothList);
		return combinationClasp;
	}

	public rpd.components.CanineClasp modifyCanineClasp(String toothPosition ,ClaspMaterial material) {
		int[] strtoToothList = getToothList(toothPosition);
		Tooth toothList = new Tooth(strtoToothList[1], strtoToothList[0]);
		rpd.components.CanineClasp canineClasp = new CanineClasp(toothList, material);
		return canineClasp;
	}

	public rpd.components.CanineClasp modifyCanineClasp(String toothPosition) {
		int[] strtoToothList = getToothList(toothPosition);
		Tooth toothList = new Tooth(strtoToothList[1], strtoToothList[0]);
		rpd.components.CanineClasp canineClasp = new CanineClasp(toothList);
		return canineClasp;
	}

	public rpd.components.CanineAkerClasp modifyCanineAkerClasp(String toothPosition ,Position tip_direction , ClaspMaterial material) {
		int[] strtoToothList = getToothList(toothPosition);
		Tooth toothList = new Tooth(strtoToothList[1], strtoToothList[0]);
		rpd.components.CanineAkerClasp canineAkerClasp = new CanineAkerClasp(toothList, tip_direction, material);
		return canineAkerClasp;
	}

	public rpd.components.CanineAkerClasp modifyCanineAkerClasp(String toothPosition ,Position tip_direction) {
		int[] strtoToothList = getToothList(toothPosition);
		Tooth toothList = new Tooth(strtoToothList[1], strtoToothList[0]);
		rpd.components.CanineAkerClasp canineAkerClasp = new CanineAkerClasp(toothList, tip_direction);
		return canineAkerClasp;
	}

	public rpd.components.HalfHalfClasp modifyHalfHalfClasp(String toothPosition ,ClaspMaterial material) {
		int[] strtoToothList = getToothList(toothPosition);
		Tooth toothList = new Tooth(strtoToothList[1], strtoToothList[0]);
		rpd.components.HalfHalfClasp halfHalfClasp = new HalfHalfClasp(toothList, material);
		return halfHalfClasp;
	}

	public rpd.components.HalfHalfClasp modifyHalfHalfClasp(String toothPosition) {
		int[] strtoToothList = getToothList(toothPosition);
		Tooth toothList = new Tooth(strtoToothList[1], strtoToothList[0]);
		rpd.components.HalfHalfClasp halfHalfClasp = new HalfHalfClasp(toothList);
		return halfHalfClasp;
	}

	public rpd.components.BackActionClasp modifyBackActionClasp(String toothPosition ,ClaspMaterial material) {
		int[] strtoToothList = getToothList(toothPosition);
		Tooth toothList = new Tooth(strtoToothList[1], strtoToothList[0]);
		rpd.components.BackActionClasp backActionClasp = new BackActionClasp(toothList, material);
		return backActionClasp;
	}

	public rpd.components.BackActionClasp modifyBackActionClasp(String toothPosition) {
		int[] strtoToothList = getToothList(toothPosition);
		Tooth toothList = new Tooth(strtoToothList[1], strtoToothList[0]);
		rpd.components.BackActionClasp backActionClasp = new BackActionClasp(toothList);
		return backActionClasp;
	}

	public rpd.components.ReverseBackActionClasp modifyReverseBackActionClasp(String toothPosition ,ClaspMaterial material) {
		int[] strtoToothList = getToothList(toothPosition);
		Tooth toothList = new Tooth(strtoToothList[1], strtoToothList[0]);
		rpd.components.ReverseBackActionClasp reverseBackActionClasp = new ReverseBackActionClasp(toothList, material);
		return reverseBackActionClasp;
	}

	public rpd.components.ReverseBackActionClasp modifyReverseBackActionClasp(String toothPosition) {
		int[] strtoToothList = getToothList(toothPosition);
		Tooth toothList = new Tooth(strtoToothList[1], strtoToothList[0]);
		rpd.components.ReverseBackActionClasp reverseBackActionClasp = new ReverseBackActionClasp(toothList);
		return reverseBackActionClasp;
	}

	public rpd.components.RingClasp modifyRingClasp(String toothPosition ,ClaspMaterial material) {
		int[] strtoToothList = getToothList(toothPosition);
		Tooth toothList = new Tooth(strtoToothList[1], strtoToothList[0]);
		rpd.components.RingClasp ringClasp = new RingClasp(toothList, material);
		return ringClasp;
	}

	public rpd.components.RingClasp modifyRingClasp(String toothPosition) {
		int[] strtoToothList = getToothList(toothPosition);
		Tooth toothList = new Tooth(strtoToothList[1], strtoToothList[0]);
		rpd.components.RingClasp ringClasp = new RingClasp(toothList);
		return ringClasp;
	}

	public rpd.components.CombinedClasp modifyCombinedClasp(String toothPosition ,ClaspMaterial material) {
		int[] strtoToothArrayList = getToothArrayList(toothPosition);
		Tooth toothListDB1 = new Tooth(strtoToothArrayList[1],strtoToothArrayList[0]);
		Tooth toothListDB2 = new Tooth(strtoToothArrayList[3],strtoToothArrayList[2]);
		ArrayList<Tooth> toothListDB = new ArrayList<Tooth>(){{add(toothListDB2); add(toothListDB1);}};
		rpd.components.CombinedClasp combinedClasp =new CombinedClasp(toothListDB ,material);
		return combinedClasp;
	}

	public rpd.components.CombinedClasp modifyCombinedClasp(String toothPosition) {
		int[] strtoToothArrayList = getToothArrayList(toothPosition);
		Tooth toothListDB1 = new Tooth(strtoToothArrayList[1],strtoToothArrayList[0]);
		Tooth toothListDB2 = new Tooth(strtoToothArrayList[3],strtoToothArrayList[2]);
		ArrayList<Tooth> toothListDB = new ArrayList<Tooth>(){{add(toothListDB2); add(toothListDB1);}};
		rpd.components.CombinedClasp combinedClasp =new CombinedClasp(toothListDB);
		return combinedClasp;
	}

	public rpd.components.RPAClasp modifyRPAClasp(String toothPosition ,ClaspMaterial material) {
		int[] strtoToothList = getToothList(toothPosition);
		Tooth toothList = new Tooth(strtoToothList[1], strtoToothList[0]);
		rpd.components.RPAClasp rPAClasp = new RPAClasp(toothList, material);
		return rPAClasp;
	}

	public rpd.components.RPAClasp modifyRPAClasp(String toothPosition) {
		int[] strtoToothList = getToothList(toothPosition);
		Tooth toothList = new Tooth(strtoToothList[1], strtoToothList[0]);
		rpd.components.RPAClasp rPAClasp = new RPAClasp(toothList);
		return rPAClasp;
	}

	public rpd.components.LingualRest modifyLingualRest(String toothPosition) {
		int[] strtoToothList = getToothList(toothPosition);
		Tooth toothList = new Tooth(strtoToothList[1], strtoToothList[0]);
		rpd.components.LingualRest lingualRest = new LingualRest(toothList);
		return lingualRest;
	}
	/**********************************************************		SinglePalatalStrapConnector		***********************************************************************/

	public rpd.components.SinglePalatalStrapConnector modifySinglePalatalStrapConnector(String toothPosition, Maxillary maxillary) {
//		int[] strtoToothArrayList = getToothArrayList(toothPosition);
//		Tooth toothListDB1 = new Tooth(strtoToothArrayList[1],strtoToothArrayList[0]);
//		Tooth toothListDB2 = new Tooth(strtoToothArrayList[3],strtoToothArrayList[2]);
//		Set<Tooth> toothListDB = new HashSet<Tooth>();
//		toothListDB.add(toothListDB2);
//		toothListDB.add(toothListDB1);
		int[] strtoToothArrayList = getToothArrayListEight(toothPosition);
		Set<Tooth> toothListDB = new HashSet<Tooth>();

		Tooth toothListDB1 = new Tooth(strtoToothArrayList[1],strtoToothArrayList[0]);
		Tooth toothListDB2 = new Tooth(strtoToothArrayList[3],strtoToothArrayList[2]);
		Tooth toothListDB3 = new Tooth(strtoToothArrayList[5],strtoToothArrayList[4]);
		Tooth toothListDB4 = new Tooth(strtoToothArrayList[7],strtoToothArrayList[6]);

		toothListDB.add(toothListDB4);
		toothListDB.add(toothListDB3);
		toothListDB.add(toothListDB2);
		toothListDB.add(toothListDB1);
		rpd.components.SinglePalatalStrapConnector singlePalatalStrapConnector =new SinglePalatalStrapConnector(toothListDB, maxillary);
		return singlePalatalStrapConnector;
	}

	public rpd.components.SinglePalatalStrapConnector modifySinglePalatalStrapConnector(String toothPosition, Maxillary maxillary, String lingualConfrontation) {
		rpd.components.SinglePalatalStrapConnector singlePalatalStrapConnector = modifySinglePalatalStrapConnector(toothPosition, maxillary);
		int[] strtoToothList = getToothList(lingualConfrontation);
		HashSet<Tooth> toothListTemp = singlePalatalStrapConnector.getLingualConfrontation();
		ArrayList<Tooth> removedLingualConfrontation = new ArrayList(toothListTemp);
		singlePalatalStrapConnector.removeLingualConfrontation(removedLingualConfrontation);
		Tooth toothList = new Tooth(strtoToothList[1], strtoToothList[0]);
		singlePalatalStrapConnector.addLingualConfrontation(toothList);
		return singlePalatalStrapConnector;
	}

	public rpd.components.SinglePalatalStrapConnector modifySinglePalatalStrapConnector(String toothPosition, Maxillary maxillary, List<String> lingualConfrontation, int num) {
		rpd.components.SinglePalatalStrapConnector singlePalatalStrapConnector = modifySinglePalatalStrapConnector(toothPosition, maxillary);

		int[] strtoToothArrayList = getToothArrayListUnsigned(lingualConfrontation,num);
		ArrayList<Tooth> toothList = new ArrayList<>();
		for(int i = 0;i < num *2; i+=2 ){
			Tooth tooth = new Tooth(strtoToothArrayList[i+1],strtoToothArrayList[i]);
			toothList.add(tooth);
		}

		if( singlePalatalStrapConnector.getLingualConfrontation()!=null ){
			HashSet<Tooth> toothListTemp = singlePalatalStrapConnector.getLingualConfrontation();
			ArrayList<Tooth> removedLingualConfrontation = new ArrayList(toothListTemp);
			singlePalatalStrapConnector.removeLingualConfrontation(removedLingualConfrontation);
		}
		if(toothList!=null){
			System.out.print("toothList = "+ toothList + "\t\n");
			try {
				singlePalatalStrapConnector.addLingualConfrontation(toothList);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return singlePalatalStrapConnector;
	}
	/***********************************************************		PalatalPlateConnector		************************************************************************/

	public rpd.components.PalatalPlateConnector modifyPalatalPlateConnector(String toothPosition, Maxillary maxillary) {

		int[] strtoToothArrayList = getToothArrayListEight(toothPosition);
		Set<Tooth> toothListDB = new HashSet<Tooth>();

		Tooth toothListDB1 = new Tooth(strtoToothArrayList[1],strtoToothArrayList[0]);
		Tooth toothListDB2 = new Tooth(strtoToothArrayList[3],strtoToothArrayList[2]);
		Tooth toothListDB3 = new Tooth(strtoToothArrayList[5],strtoToothArrayList[4]);
		Tooth toothListDB4 = new Tooth(strtoToothArrayList[7],strtoToothArrayList[6]);

		toothListDB.add(toothListDB4);
		toothListDB.add(toothListDB3);
		toothListDB.add(toothListDB2);
		toothListDB.add(toothListDB1);
		rpd.components.PalatalPlateConnector palatalPlateConnector =new PalatalPlateConnector(toothListDB, maxillary);
		return palatalPlateConnector;
	}

	public rpd.components.PalatalPlateConnector modifyPalatalPlateConnector(String toothPosition, Maxillary maxillary, String lingualConfrontation) {
		rpd.components.PalatalPlateConnector palatalPlateConnector = modifyPalatalPlateConnector(toothPosition, maxillary);
		int[] strtoToothList = getToothList(lingualConfrontation);
		HashSet<Tooth> toothListTemp = palatalPlateConnector.getLingualConfrontation();
		ArrayList<Tooth> removedLingualConfrontation = new ArrayList(toothListTemp);
		palatalPlateConnector.removeLingualConfrontation(removedLingualConfrontation);
		Tooth toothList = new Tooth(strtoToothList[1], strtoToothList[0]);
		palatalPlateConnector.addLingualConfrontation(toothList);
		return palatalPlateConnector;
	}

	public rpd.components.PalatalPlateConnector modifyPalatalPlateConnector(String toothPosition, Maxillary maxillary, List<String> lingualConfrontation, int num) {
		rpd.components.PalatalPlateConnector palatalPlateConnector = modifyPalatalPlateConnector(toothPosition, maxillary);

		int[] strtoToothArrayList = getToothArrayListUnsigned(lingualConfrontation,num);
		ArrayList<Tooth> toothList = new ArrayList<>();
		for(int i = 0;i < num *2; i+=2 ){
			Tooth tooth = new Tooth(strtoToothArrayList[i+1],strtoToothArrayList[i]);
			toothList.add(tooth);
		}

		HashSet<Tooth> toothListTemp = palatalPlateConnector.getLingualConfrontation();
		ArrayList<Tooth> removedLingualConfrontation = new ArrayList(toothListTemp);
		palatalPlateConnector.removeLingualConfrontation(removedLingualConfrontation);
		palatalPlateConnector.addLingualConfrontation(toothList);
		return palatalPlateConnector;
	}
	/***********************************************************		FullPalatalPlateConnector		************************************************************************/


	public rpd.components.FullPalatalPlateConnector modifyFullPalatalPlateConnector(String toothPosition, Maxillary maxillary) {
		int[] strtoToothArrayList = getToothArrayList(toothPosition);
		Tooth toothListDB1 = new Tooth(strtoToothArrayList[1],strtoToothArrayList[0]);
		Tooth toothListDB2 = new Tooth(strtoToothArrayList[3],strtoToothArrayList[2]);
		Set<Tooth> toothListDB = new HashSet<Tooth>();
		toothListDB.add(toothListDB2);
		toothListDB.add(toothListDB1);
		rpd.components.FullPalatalPlateConnector fullPalatalPlateConnector =new FullPalatalPlateConnector(toothListDB, maxillary);
		return fullPalatalPlateConnector;
	}

	public rpd.components.FullPalatalPlateConnector modifyFullPalatalPlateConnector(String toothPosition, Maxillary maxillary, String lingualConfrontation) {
		rpd.components.FullPalatalPlateConnector fullPalatalPlateConnector = modifyFullPalatalPlateConnector(toothPosition, maxillary);
		int[] strtoToothList = getToothList(lingualConfrontation);
		HashSet<Tooth> toothListTemp = fullPalatalPlateConnector.getLingualConfrontation();
		ArrayList<Tooth> removedLingualConfrontation = new ArrayList(toothListTemp);
		fullPalatalPlateConnector.removeLingualConfrontation(removedLingualConfrontation);
		Tooth toothList = new Tooth(strtoToothList[1], strtoToothList[0]);
		fullPalatalPlateConnector.addLingualConfrontation(toothList);
		return fullPalatalPlateConnector;
	}

	public rpd.components.FullPalatalPlateConnector modifyFullPalatalPlateConnector(String toothPosition, Maxillary maxillary, List<String> lingualConfrontation, int num) {
		rpd.components.FullPalatalPlateConnector fullPalatalPlateConnector = modifyFullPalatalPlateConnector(toothPosition, maxillary);

		int[] strtoToothArrayList = getToothArrayListUnsigned(lingualConfrontation,num);
		ArrayList<Tooth> toothList = new ArrayList<>();
		for(int i = 0;i < num *2; i+=2 ){
			Tooth tooth = new Tooth(strtoToothArrayList[i+1],strtoToothArrayList[i]);
			toothList.add(tooth);
		}

		HashSet<Tooth> toothListTemp = fullPalatalPlateConnector.getLingualConfrontation();
		ArrayList<Tooth> removedLingualConfrontation = new ArrayList(toothListTemp);
		fullPalatalPlateConnector.removeLingualConfrontation(removedLingualConfrontation);
		fullPalatalPlateConnector.addLingualConfrontation(toothList);
		return fullPalatalPlateConnector;
	}
	/***********************************************************		CombinationAnteriorPosteriorPalatalStrapConnector		************************************************************************/

	public rpd.components.CombinationAnteriorPosteriorPalatalStrapConnector modifyCombinationAnteriorPosteriorPalatalStrapConnector(String toothPosition, Maxillary maxillary) {
//		int[] strtoToothArrayList = getToothArrayList(toothPosition);
//		Tooth toothListDB1 = new Tooth(strtoToothArrayList[1],strtoToothArrayList[0]);
//		Tooth toothListDB2 = new Tooth(strtoToothArrayList[3],strtoToothArrayList[2]);
//		Set<Tooth> toothListDB = new HashSet<Tooth>();
//		toothListDB.add(toothListDB2);
//		toothListDB.add(toothListDB1);
		int[] strtoToothArrayList = getToothArrayListEight(toothPosition);
		Set<Tooth> toothListDB = new HashSet<Tooth>();

		Tooth toothListDB1 = new Tooth(strtoToothArrayList[1],strtoToothArrayList[0]);
		Tooth toothListDB2 = new Tooth(strtoToothArrayList[3],strtoToothArrayList[2]);
		Tooth toothListDB3 = new Tooth(strtoToothArrayList[5],strtoToothArrayList[4]);
		Tooth toothListDB4 = new Tooth(strtoToothArrayList[7],strtoToothArrayList[6]);

		toothListDB.add(toothListDB4);
		toothListDB.add(toothListDB3);
		toothListDB.add(toothListDB2);
		toothListDB.add(toothListDB1);
		rpd.components.CombinationAnteriorPosteriorPalatalStrapConnector combinationAnteriorPosteriorPalatalStrapConnector =new CombinationAnteriorPosteriorPalatalStrapConnector(toothListDB, maxillary);
		return combinationAnteriorPosteriorPalatalStrapConnector;
	}

	public rpd.components.CombinationAnteriorPosteriorPalatalStrapConnector modifyCombinationAnteriorPosteriorPalatalStrapConnector(String toothPosition, Maxillary maxillary, String lingualConfrontation) {
		rpd.components.CombinationAnteriorPosteriorPalatalStrapConnector combinationAnteriorPosteriorPalatalStrapConnector = modifyCombinationAnteriorPosteriorPalatalStrapConnector(toothPosition, maxillary);
		int[] strtoToothList = getToothList(lingualConfrontation);
		HashSet<Tooth> toothListTemp = combinationAnteriorPosteriorPalatalStrapConnector.getLingualConfrontation();
		ArrayList<Tooth> removedLingualConfrontation = new ArrayList(toothListTemp);
		combinationAnteriorPosteriorPalatalStrapConnector.removeLingualConfrontation(removedLingualConfrontation);
		Tooth toothList = new Tooth(strtoToothList[1], strtoToothList[0]);
		combinationAnteriorPosteriorPalatalStrapConnector.addLingualConfrontation(toothList);
		return combinationAnteriorPosteriorPalatalStrapConnector;
	}

	public rpd.components.CombinationAnteriorPosteriorPalatalStrapConnector modifyCombinationAnteriorPosteriorPalatalStrapConnector(String toothPosition, Maxillary maxillary,  List<String> lingualConfrontation, int num) {
		rpd.components.CombinationAnteriorPosteriorPalatalStrapConnector combinationAnteriorPosteriorPalatalStrapConnector = modifyCombinationAnteriorPosteriorPalatalStrapConnector(toothPosition, maxillary);

		int[] strtoToothArrayList = getToothArrayListUnsigned(lingualConfrontation,num);
		ArrayList<Tooth> toothList = new ArrayList<>();
		for(int i = 0;i < num *2; i+=2 ){
			Tooth tooth = new Tooth(strtoToothArrayList[i+1],strtoToothArrayList[i]);
			toothList.add(tooth);
		}

		HashSet<Tooth> toothListTemp = combinationAnteriorPosteriorPalatalStrapConnector.getLingualConfrontation();
		ArrayList<Tooth> removedLingualConfrontation = new ArrayList(toothListTemp);
		combinationAnteriorPosteriorPalatalStrapConnector.removeLingualConfrontation(removedLingualConfrontation);
		combinationAnteriorPosteriorPalatalStrapConnector.addLingualConfrontation(toothList);
		return combinationAnteriorPosteriorPalatalStrapConnector;
	}
	/***********************************************************		ModifiedPalatalPlateConnector		************************************************************************/

	public rpd.components.ModifiedPalatalPlateConnector modifyModifiedPalatalPlateConnector(String toothPosition, Maxillary maxillary) {
		int[] strtoToothArrayList = getToothArrayListEight(toothPosition);
		Set<Tooth> toothListDB = new HashSet<Tooth>();

		Tooth toothListDB1 = new Tooth(strtoToothArrayList[1],strtoToothArrayList[0]);
		Tooth toothListDB2 = new Tooth(strtoToothArrayList[3],strtoToothArrayList[2]);
		Tooth toothListDB3 = new Tooth(strtoToothArrayList[5],strtoToothArrayList[4]);
		Tooth toothListDB4 = new Tooth(strtoToothArrayList[7],strtoToothArrayList[6]);

		toothListDB.add(toothListDB4);
		toothListDB.add(toothListDB3);
		toothListDB.add(toothListDB2);
		toothListDB.add(toothListDB1);
		rpd.components.ModifiedPalatalPlateConnector modifiedPalatalPlateConnector =new ModifiedPalatalPlateConnector(toothListDB, maxillary);
		return modifiedPalatalPlateConnector;
	}
	public rpd.components.ModifiedPalatalPlateConnector modifyModifiedPalatalPlateConnector(String toothPosition, Maxillary maxillary, String lingualConfrontation) {
		rpd.components.ModifiedPalatalPlateConnector modifiedPalatalPlateConnector = modifyModifiedPalatalPlateConnector(toothPosition, maxillary);
		int[] strtoToothList = getToothList(lingualConfrontation);
		HashSet<Tooth> toothListTemp = modifiedPalatalPlateConnector.getLingualConfrontation();
		ArrayList<Tooth> removedLingualConfrontation = new ArrayList(toothListTemp);
		modifiedPalatalPlateConnector.removeLingualConfrontation(removedLingualConfrontation);
		Tooth toothList = new Tooth(strtoToothList[1], strtoToothList[0]);
		modifiedPalatalPlateConnector.addLingualConfrontation(toothList);
		return modifiedPalatalPlateConnector;
	}

	public rpd.components.ModifiedPalatalPlateConnector modifyModifiedPalatalPlateConnector(String toothPosition, Maxillary maxillary,  List<String> lingualConfrontation, int num) {
		rpd.components.ModifiedPalatalPlateConnector modifiedPalatalPlateConnector = modifyModifiedPalatalPlateConnector(toothPosition, maxillary);

		int[] strtoToothArrayList = getToothArrayListUnsigned(lingualConfrontation,num);
		ArrayList<Tooth> toothList = new ArrayList<>();
		for(int i = 0;i < num *2; i+=2 ){
			Tooth tooth = new Tooth(strtoToothArrayList[i+1],strtoToothArrayList[i]);
			toothList.add(tooth);
		}

		HashSet<Tooth> toothListTemp = modifiedPalatalPlateConnector.getLingualConfrontation();
		ArrayList<Tooth> removedLingualConfrontation = new ArrayList(toothListTemp);
		modifiedPalatalPlateConnector.removeLingualConfrontation(removedLingualConfrontation);
		modifiedPalatalPlateConnector.addLingualConfrontation(toothList);
		return modifiedPalatalPlateConnector;
	}
	/***********************************************************		LingualBarConnector		************************************************************************/

	public rpd.components.LingualBarConnector modifyLingualBarConnector(String toothPosition , Mandibular mandibular) {
		int[] strtoToothArrayList = getToothArrayList(toothPosition);
		Tooth toothListDB1 = new Tooth(strtoToothArrayList[1],strtoToothArrayList[0]);
		Tooth toothListDB2 = new Tooth(strtoToothArrayList[3],strtoToothArrayList[2]);
		Set<Tooth> toothListDB = new HashSet<Tooth>();
		toothListDB.add(toothListDB2);
		toothListDB.add(toothListDB1);
		rpd.components.LingualBarConnector lingualBarConnector =new LingualBarConnector(toothListDB, mandibular);
		return lingualBarConnector;
	}

	public rpd.components.LingualBarConnector modifyLingualBarConnector(String toothPosition, Mandibular mandibular, String lingualConfrontation) {

		rpd.components.LingualBarConnector lingualBarConnector = modifyLingualBarConnector(toothPosition, mandibular);
		int[] strtoToothList = getToothList(lingualConfrontation);
		HashSet<Tooth> toothListTemp = lingualBarConnector.getLingualConfrontation();
		if(toothListTemp != null){
			ArrayList<Tooth> removedLingualConfrontation = new ArrayList(toothListTemp);
			lingualBarConnector.removeLingualConfrontation(removedLingualConfrontation);
		}
		Tooth toothList = new Tooth(strtoToothList[1], strtoToothList[0]);
		if (toothList != null)
			try {
				lingualBarConnector.addLingualConfrontation(toothList);
			}catch (NullPointerException e){
				e.printStackTrace();
			}
		return lingualBarConnector;
	}

	public rpd.components.LingualBarConnector modifyLingualBarConnector(String toothPosition, Mandibular mandibular,  List<String> lingualConfrontation, int num) {
		rpd.components.LingualBarConnector lingualBarConnector = modifyLingualBarConnector(toothPosition, mandibular);


		int[] strtoToothArrayList = getToothArrayListUnsigned(lingualConfrontation,num);
		ArrayList<Tooth> toothList = new ArrayList<>();
		for(int i = 0;i < num *2; i+=2 ){
			Tooth tooth = new Tooth(strtoToothArrayList[i+1],strtoToothArrayList[i]);
			toothList.add(tooth);
		}

		HashSet<Tooth> toothListTemp = lingualBarConnector.getLingualConfrontation();
		if(toothListTemp != null) {
			ArrayList<Tooth> removedLingualConfrontation = new ArrayList(toothListTemp);
			lingualBarConnector.removeLingualConfrontation(removedLingualConfrontation);
		}
		try {
			lingualBarConnector.addLingualConfrontation(toothList);
		}catch(NullPointerException e){
			e.printStackTrace();
		}
		return lingualBarConnector;
	}
	/***********************************************************		LingualPlateConnector		************************************************************************/

	public rpd.components.LingualPlateConnector modifyLingualPlateConnector(String toothPosition, Mandibular mandibular) {
		int[] strtoToothArrayList = getToothArrayList(toothPosition);
		Tooth toothListDB1 = new Tooth(strtoToothArrayList[1],strtoToothArrayList[0]);
		Tooth toothListDB2 = new Tooth(strtoToothArrayList[3],strtoToothArrayList[2]);
		Set<Tooth> toothListDB = new HashSet<Tooth>();
		toothListDB.add(toothListDB2);
		toothListDB.add(toothListDB1);
		rpd.components.LingualPlateConnector lingualPlateConnector =new LingualPlateConnector(toothListDB, mandibular);
		return lingualPlateConnector;
	}

	public rpd.components.LingualPlateConnector modifyLingualPlateConnector(String toothPosition, Mandibular mandibular, String lingualConfrontation) {

		rpd.components.LingualPlateConnector lingualPlateConnector = modifyLingualPlateConnector(toothPosition, mandibular);
		int[] strtoToothList = getToothList(lingualConfrontation);
		HashSet<Tooth> toothListTemp = lingualPlateConnector.getLingualConfrontation();

		if(toothListTemp != null) {
			ArrayList<Tooth> removedLingualConfrontation = new ArrayList(toothListTemp);
			lingualPlateConnector.removeLingualConfrontation(removedLingualConfrontation);
		}
		Tooth toothList = new Tooth(strtoToothList[1], strtoToothList[0]);
		try {
			lingualPlateConnector.addLingualConfrontation(toothList);
		}catch(NullPointerException e){
			e.printStackTrace();
		}
		return lingualPlateConnector;
	}

	public rpd.components.LingualPlateConnector modifyLingualPlateConnector(String toothPosition, Mandibular mandibular, List<String> lingualConfrontation, int num) {
		rpd.components.LingualPlateConnector lingualPlateConnector = modifyLingualPlateConnector(toothPosition, mandibular);

		int[] strtoToothArrayList = getToothArrayListUnsigned(lingualConfrontation,num);
		ArrayList<Tooth> toothList = new ArrayList<>();
		for(int i = 0;i < num *2; i+=2 ){
			Tooth tooth = new Tooth(strtoToothArrayList[i+1],strtoToothArrayList[i]);
			toothList.add(tooth);
		}

		HashSet<Tooth> toothListTemp = lingualPlateConnector.getLingualConfrontation();
//		ArrayList<Tooth> removedLingualConfrontation = new ArrayList(toothListTemp);
//		lingualPlateConnector.removeLingualConfrontation(removedLingualConfrontation);
//		lingualPlateConnector.addLingualConfrontation(toothList);
		if(toothListTemp != null) {
			ArrayList<Tooth> removedLingualConfrontation = new ArrayList(toothListTemp);
			lingualPlateConnector.removeLingualConfrontation(removedLingualConfrontation);
		}
		try {
			lingualPlateConnector.addLingualConfrontation(toothList);
		}catch(NullPointerException e){
			e.printStackTrace();
		}
		return lingualPlateConnector;
	}

	/***********************************************************************************************************************************/

	public rpd.components.DentureBase modifyDentureBase(String toothPosition) {
		int count = 0;
		for(int i =0 ;i<toothPosition.length();i++)
				if(toothPosition.charAt(i)>=48 && toothPosition.charAt(i)<=57)
					count++;
		if (count == 2)
			toothPosition += toothPosition;
		int[] strtoToothArrayList = getToothArrayList(toothPosition);
		Tooth toothListDB1 = new Tooth(strtoToothArrayList[1],strtoToothArrayList[0]);
		Tooth toothListDB2 = new Tooth(strtoToothArrayList[3],strtoToothArrayList[2]);
		ArrayList<Tooth> toothListDB = new ArrayList<Tooth>(){{add(toothListDB2); add(toothListDB1);}};
		rpd.components.DentureBase dentureBase =new DentureBase(toothListDB);
		return dentureBase;
	}

	/*********************************************************************************************************************************/

	public List<RPDPlan> savePlan(List<JTable> planTableList, List<RPDPlan> planList) {
		int planIndex = 0;
		List<RPDPlan> planListNew = buildNewDentalPlan(planList);
		Set<rpd.components.Component> component_new = new HashSet<>();
		for (JTable plan_table : planTableList) {
			DefaultTableModel model = (DefaultTableModel) plan_table.getModel();
			int row = model.getRowCount();
			int col = model.getColumnCount();
			for (int i = 0; i < row; i++) {
				if (model.getValueAt(i, 3) != null
						&& !model.getValueAt(i, 3).toString().isEmpty()
						&& model.getValueAt(i, 1) != null) {
					String toothPositionStr = null;//col 1
					String componentName = null;// col 3
					ClaspMaterial material = null;//col 4
					Position tip_direction = null; //col 5
					List<String> lingualConfrontation = new ArrayList<String>();// col 6~
					if (model.getValueAt(i, 1) != null)
						toothPositionStr = model.getValueAt(i, 1).toString();
					if (model.getValueAt(i, 3) != null)
						componentName = (String) model.getValueAt(i, 3);//component name
					if (model.getValueAt(i, 4) != null)
						switch (model.getValueAt(i, 4).toString()) {
							case "弯制材料":
								material = ClaspMaterial.WW;
								break;
							case "铸造材料":
								material = ClaspMaterial.Cast;
								break;
						}
					if (model.getValueAt(i, 5) != null)
						switch (model.getValueAt(i, 5).toString()) {
							case "卡环臂尖朝向近中":
							case "近中":
								tip_direction = Position.Mesial;
								break;
							case "卡环臂尖朝向远中":
							case "远中":
								tip_direction = Position.Distal;
								break;
						}
					for (int j = 6; j < col; j++) {
						if (model.getValueAt(i, j) != null && model.getValueAt(i, j).toString().contains("tooth"))
							lingualConfrontation.add(model.getValueAt(i, j).toString());
					}
					switch (componentName) {
						case "Aker卡环":
							if (material == null)
								component_new.add(modifyAkerClasp(toothPositionStr, tip_direction));
							else
								component_new.add(modifyAkerClasp(toothPositionStr, tip_direction, material));
							break;
						case "间隙（Embrasure）卡环":
							component_new.add(modifyEmbrasureClasp(toothPositionStr));
							break;
						case "连续（Continuous）卡环":
							if (material == null)
								component_new.add(modifyContinuousClasp(toothPositionStr));
							else
								component_new.add(modifyContinuousClasp(toothPositionStr, material));
							break;
						case "弯制Aker（Wrought Wire Aker）卡环":
							component_new.add(modifyWroughtWireClasp(toothPositionStr, tip_direction));
							break;
						case "结合（Combination）卡环":
							if (tip_direction != null)
								component_new.add(modifyCombinationClasp(toothPositionStr, tip_direction));
							else
								component_new.add(modifyCombinationClasp(toothPositionStr));
							break;
						case "尖牙(Canine)卡环":
							if (material == null)
								component_new.add(modifyCanineClasp(toothPositionStr));
							else
								component_new.add(modifyCanineClasp(toothPositionStr, material));
							break;
						case "尖牙Aker（Canine Aker）卡环":
							if (material == null)
								component_new.add(modifyCanineAkerClasp(toothPositionStr, tip_direction));
							else
								component_new.add(modifyCanineAkerClasp(toothPositionStr, tip_direction, material));
							break;
						case "对半（Half and Half）卡环":
							if (material == null)
								component_new.add(modifyHalfHalfClasp(toothPositionStr));
							else
								component_new.add(modifyHalfHalfClasp(toothPositionStr, material));
							break;
						case "回力（Back Action）卡环":
							if (material == null)
								component_new.add(modifyBackActionClasp(toothPositionStr));
							else
								component_new.add(modifyBackActionClasp(toothPositionStr, material));
							break;
						case "反回力（Reverse Back Action）卡环":
							if (material == null)
								component_new.add(modifyReverseBackActionClasp(toothPositionStr));
							else
								component_new.add(modifyReverseBackActionClasp(toothPositionStr, material));
							break;
						case "圈形（Ring）卡环":
							if (material == null)
								component_new.add(modifyRingClasp(toothPositionStr));
							else
								component_new.add(modifyRingClasp(toothPositionStr, material));
							break;
						case "联合（Combined）卡环":
							if (material == null)
								component_new.add(modifyCombinedClasp(toothPositionStr));
							else
								component_new.add(modifyCombinedClasp(toothPositionStr, material));
							break;
						case "RPA卡环":
							component_new.add(modifyRPAClasp(toothPositionStr, material));
							break;
						case "合支托":
							component_new.add(modifyOcclusalRest(toothPositionStr, tip_direction));
							break;
						case "舌支托":
							component_new.add(modifyLingualRest(toothPositionStr));
							break;
						case"上颌腭带（Single Palatal Strap）":
							if(lingualConfrontation.size() > 1)//more than one tooth
								component_new.add(modifySinglePalatalStrapConnector(toothPositionStr, mouth.getMaxillary(),lingualConfrontation,lingualConfrontation.size()));
							else if(lingualConfrontation.size() == 1)//one tooth
								component_new.add(modifySinglePalatalStrapConnector(toothPositionStr, mouth.getMaxillary(),lingualConfrontation.get(0)));
							else//none tooth
								component_new.add(modifySinglePalatalStrapConnector(toothPositionStr, mouth.getMaxillary()));
							break;
						case"上颌前后腭带（Combination Anterior and Posterior Palatal Strap）":
							if(lingualConfrontation.size() > 1)//more than one tooth
								component_new.add(modifyCombinationAnteriorPosteriorPalatalStrapConnector(toothPositionStr, mouth.getMaxillary(),lingualConfrontation,lingualConfrontation.size()));
							else if(lingualConfrontation.size() == 1)//one tooth
								component_new.add(modifyCombinationAnteriorPosteriorPalatalStrapConnector(toothPositionStr, mouth.getMaxillary(),lingualConfrontation.get(0)));
							else
								component_new.add(modifyCombinationAnteriorPosteriorPalatalStrapConnector(toothPositionStr, mouth.getMaxillary()));
							break;
						case"上颌腭板（Palatal Plate）":
							if(lingualConfrontation.size() > 1)//more than one tooth
								component_new.add(modifyPalatalPlateConnector(toothPositionStr, mouth.getMaxillary(),lingualConfrontation,lingualConfrontation.size()));
							else if(lingualConfrontation.size() == 1)//one tooth
								component_new.add(modifyPalatalPlateConnector(toothPositionStr, mouth.getMaxillary(),lingualConfrontation.get(0)));
							else
								component_new.add(modifyPalatalPlateConnector(toothPositionStr, mouth.getMaxillary()));
							break;
						case"上颌全腭板（Full Palatal Plate）":
							if(lingualConfrontation.size() > 1)//more than one tooth
								component_new.add(modifyFullPalatalPlateConnector(toothPositionStr, mouth.getMaxillary(),lingualConfrontation,lingualConfrontation.size()));
							else if(lingualConfrontation.size() == 1)//one tooth
								component_new.add(modifyFullPalatalPlateConnector(toothPositionStr, mouth.getMaxillary(),lingualConfrontation.get(0)));
							else
								component_new.add(modifyFullPalatalPlateConnector(toothPositionStr, mouth.getMaxillary()));
							break;
						case"上颌变异腭板（Modified Palatal Plate）":
							if(lingualConfrontation.size() > 1)//more than one tooth
								component_new.add(modifyModifiedPalatalPlateConnector(toothPositionStr, mouth.getMaxillary(),lingualConfrontation,lingualConfrontation.size()));
							else if(lingualConfrontation.size() == 1)//one tooth
								component_new.add(modifyModifiedPalatalPlateConnector(toothPositionStr, mouth.getMaxillary(),lingualConfrontation.get(0)));
							else
								component_new.add(modifyModifiedPalatalPlateConnector(toothPositionStr, mouth.getMaxillary()));
							break;

						case "下颌舌杆（Lingual Bar）":
//							if (lingualConfrontation.size() > 1)//more than one tooth
//								component_new.add(modifyLingualBarConnector(toothPositionStr, mouth.getMandibular(), lingualConfrontation, lingualConfrontation.size()));
//							else if (lingualConfrontation.size() == 1)//one tooth
//								component_new.add(modifyLingualBarConnector(toothPositionStr, mouth.getMandibular(), lingualConfrontation.get(0)));
//							else
								component_new.add(modifyLingualBarConnector(toothPositionStr, mouth.getMandibular()));
							break;
						case "下颌舌板（Lingual Plate）":
							if (lingualConfrontation.size() > 1)//more than one tooth
								component_new.add(modifyLingualPlateConnector(toothPositionStr, mouth.getMandibular(), lingualConfrontation, lingualConfrontation.size()));
							else if (lingualConfrontation.size() == 1)//one tooth
								component_new.add(modifyLingualPlateConnector(toothPositionStr, mouth.getMandibular(), lingualConfrontation.get(0)));
							else
								component_new.add(modifyLingualPlateConnector(toothPositionStr, mouth.getMandibular()));
							break;
						case "基托（Denture Base）":
							component_new.add(modifyDentureBase(toothPositionStr));
							break;
						default:
							break;
					}
				}
				else {
					JOptionPane.showMessageDialog(null, "在方案" + (planIndex + 1) + "第" + (i + 1) + "行，请选择组件并输入牙位", "数据缺失", JOptionPane.ERROR_MESSAGE);
				}
			}//end of this plan
			for (rpd.components.Component comAdd : component_new) {
//						System.out.print("comAdd = " + comAdd + "\t\n");
				if (comAdd.getToothPos() != null){
					planListNew.get(planIndex).addComponent(comAdd);
//							System.out.print("mandibular_rpd_plans_new = " + mandibular_rpd_plans_new + "\t\n");
				}
			}
			component_new.clear();
			planIndex++;
		}
		return planListNew;
	}


	/*********************************************************************************************************************************/

	public List<RPDPlan> buildNewDentalPlan(List<RPDPlan> rpd_plans) {
		int planIndex = 0;
		List<RPDPlan> rpd_plans_new = new ArrayList<>();
//		for(int i =0;i<3;i++)
//			if(rpd_plans.get(i) != null)
//				rpd_plans_new.add(rpd_plans.get(i));
		for (RPDPlan plan : rpd_plans)
			rpd_plans_new.add(plan);
//			rpd_plans_new.add(rpd_plans.get(1));
//			rpd_plans_new.add(rpd_plans.get(2));
		for (RPDPlan plan : rpd_plans) {
			Map<ArrayList<Tooth>, Set<rpd.components.Component>> tooth_components = plan.getToothComponents();
			ArrayList<ArrayList<Tooth>> plan_teeth = new ArrayList<>(tooth_components.keySet());
			for (ArrayList<Tooth> tooth : plan_teeth) {
				Set<rpd.components.Component> components = tooth_components.get(tooth);
				for (rpd.components.Component component : components)
					if (component != null)
						rpd_plans_new.get(planIndex).removeComponent(component);

			}
			planIndex++;
		}
		return  rpd_plans_new;
	}



	public JTable setRowEditors(RPDPlan plan, JTable table , int plan_count) {
		JComboBox comComponentsName = new JComboBox(CString.mandibularComponentsName);
		JComboBox comClaspMaterial = new JComboBox(CString.cClaspMaterial);
		JComboBox comClaspPosition = new JComboBox(CString.cClaspPosition);
		JComboBox comRestPosition = new JComboBox(CString.cRestPosition);
		JComboBox comMandibularConnecterToothPosition = new JComboBox(CString.cMandibularConnecterToothPosition);
		JComboBox comClaspComponentName = new JComboBox(CString.classClaspComponentName);
		JComboBox comRestComponentName = new JComboBox(CString.classRestComponentName);
		JComboBox comMandibularConnecterComponentName = new JComboBox(CString.classMandibularConnecterComponentName);
		JComboBox comBaseComponentName = new JComboBox(CString.classBaseComponentName);
		JComboBox comMaxillaryConnecterComponentName = new JComboBox(CString.classMaxillaryConnecterComponentName);
		JComboBox comMaxillaryConnecterToothPosition = new JComboBox(CString.cMaxillaryConnecterToothPosition);
		JComboBox comNull = new JComboBox(CString.stringnull);

		comNull.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				final JComponent c = (JComponent) e.getSource();
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						c.requestFocus();
						System.out.println(c);
						if (c instanceof JComboBox) {
							System.out.println("a");
						}
					}
				});
			}
		});
		comComponentsName.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				final JComponent c = (JComponent) e.getSource();
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						c.requestFocus();
						System.out.println(c);
						if (c instanceof JComboBox) {
							System.out.println("a");
						}
					}
				});
			}
		});

		comClaspMaterial.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				final JComponent c = (JComponent) e.getSource();
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						c.requestFocus();
						System.out.println(c);
						if (c instanceof JComboBox) {
							System.out.println("a");
						}
					}
				});
			}
		});

		comClaspPosition.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				final JComponent c = (JComponent) e.getSource();
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						c.requestFocus();
						System.out.println(c);
						if (c instanceof JComboBox) {
							System.out.println("a");
						}
					}
				});
			}
		});

		comRestPosition.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				final JComponent c = (JComponent) e.getSource();
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						c.requestFocus();
						System.out.println(c);
						if (c instanceof JComboBox) {
							System.out.println("a");
						}
					}
				});
			}
		});

		comMandibularConnecterToothPosition.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				final JComponent c = (JComponent) e.getSource();
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						c.requestFocus();
						System.out.println(c);
						if (c instanceof JComboBox) {
							System.out.println("a");
						}
					}
				});
			}
		});

		comClaspComponentName.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				final JComponent c = (JComponent) e.getSource();
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						c.requestFocus();
						System.out.println(c);
						if (c instanceof JComboBox) {
							System.out.println("a");
						}
					}
				});
			}
		});

		comRestComponentName.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				final JComponent c = (JComponent) e.getSource();
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						c.requestFocus();
						System.out.println(c);
						if (c instanceof JComboBox) {
							System.out.println("a");
						}
					}
				});
			}
		});

		comMandibularConnecterComponentName.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				final JComponent c = (JComponent) e.getSource();
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						c.requestFocus();
						System.out.println(c);
						if (c instanceof JComboBox) {
							System.out.println("a");
						}
					}
				});
			}
		});

		comBaseComponentName.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				final JComponent c = (JComponent) e.getSource();
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						c.requestFocus();
						System.out.println(c);
						if (c instanceof JComboBox) {
							System.out.println("a");
						}
					}
				});
			}
		});

		comMaxillaryConnecterToothPosition.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				final JComponent c = (JComponent) e.getSource();
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						c.requestFocus();
						System.out.println(c);
						if (c instanceof JComboBox) {
							System.out.println("a");
						}
					}
				});
			}
		});


		comMaxillaryConnecterComponentName.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				final JComponent c = (JComponent) e.getSource();
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						c.requestFocus();
						System.out.println(c);
						if (c instanceof JComboBox) {
							System.out.println("a");
						}
					}
				});
			}
		});

		EachRowEditor rowEditor1 = new EachRowEditor(table);
		EachRowEditor rowEditor2 = new EachRowEditor(table);
		EachRowEditor rowEditor3 = new EachRowEditor(table);
		EachRowEditor rowEditor4 = new EachRowEditor(table);
		EachRowEditor rowEditor5 = new EachRowEditor(table);
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		Map<ArrayList<Tooth>, Set<rpd.components.Component>> tooth_components = plan.getToothComponents();
		ArrayList<ArrayList<Tooth>> plan_teeth = new ArrayList<>(tooth_components.keySet());
		Collections.sort(plan_teeth, new Comparator<ArrayList<Tooth>>() {
			public int compare(ArrayList<Tooth> left, ArrayList<Tooth> right) {
				return left.get(0).compareTo(right.get(0));
			}
		});

		int rowNum = model.getRowCount();
		for (ArrayList<Tooth> tooth : plan_teeth) {
			Set<rpd.components.Component> components = tooth_components.get(tooth);
			for (rpd.components.Component component : components) {
				String[] arrComponents;
				String[] arr1 = new String[3];
				String[] arr2;
				try {
					arr1[0] = Integer.toString(plan_count);
					arr1[1] = component.getToothPos().toString();
					arr1[2] = component.getClass().getName();
					arr2 = component.addComponents();
					arrComponents = ArrayUtils.addAll(arr1, arr2);
					model.addRow(arrComponents);
					String className = component.getClass().getName();
					switch (className) {
						case "rpd.components.AkerClasp":
						case "rpd.components.WroughtWireClasp":
						case "rpd.components.CombinationClasp":
						case "rpd.components.CanineClasp":
						case "rpd.components.CanineAkerClasp":
						case "rpd.components.HalfHalfClasp":
						case "rpd.components.BackActionClasp":
						case "rpd.components.ReverseBackActionClasp":
						case "rpd.components.RingClasp":
						case "rpd.components.CombinedClasp":
						case "rpd.components.EmbrasureClasp":
						case "rpd.components.ContinuousClasp":
						case "rpd.components.RPAClasp":
							rowEditor1.setEditorAt(rowNum, new DefaultCellEditor(comClaspMaterial));
							table.getColumn("属性1").setCellEditor(rowEditor1);
							rowEditor2.setEditorAt(rowNum, new DefaultCellEditor(comClaspPosition));
							table.getColumn("属性2").setCellEditor(rowEditor2);
							rowEditor5.setEditorAt(rowNum, new DefaultCellEditor(comClaspComponentName));
							table.getColumn("组件名称").setCellEditor(rowEditor5);
							break;
						case "rpd.components.OcclusalRest":
						case "rpd.components.LingualRest":
							rowEditor2.setEditorAt(rowNum, new DefaultCellEditor(comRestPosition));
							table.getColumn("属性2").setCellEditor(rowEditor2);
							rowEditor5.setEditorAt(rowNum, new DefaultCellEditor(comRestComponentName));
							table.getColumn("组件名称").setCellEditor(rowEditor5);
							break;
						case "rpd.components.LingualBarConnector":
//							rowEditor5.setEditorAt(rowNum, new DefaultCellEditor(comMandibularConnecterComponentName));
//							table.getColumn("组件名称").setCellEditor(rowEditor5);
//							break;
						case "rpd.components.LingualPlateConnector":
							rowEditor3.setEditorAt(rowNum, new DefaultCellEditor(comMandibularConnecterToothPosition));
							table.getColumn("舌侧对抗#1").setCellEditor(rowEditor3);
							rowEditor4.setEditorAt(rowNum, new DefaultCellEditor(comMandibularConnecterToothPosition));
							table.getColumn("舌侧对抗#2").setCellEditor(rowEditor4);
							rowEditor5.setEditorAt(rowNum, new DefaultCellEditor(comMandibularConnecterComponentName));
							table.getColumn("组件名称").setCellEditor(rowEditor5);
							break;
						case "rpd.components.SinglePalatalStrapConnector":
						case "rpd.components.CombinationAnteriorPosteriorPalatalStrapConnector":
						case "rpd.components.PalatalPlateConnector":
						case "rpd.components.FullPalatalPlateConnector":
						case "rpd.components.ModifiedPalatalPlateConnector":
							rowEditor3.setEditorAt(rowNum, new DefaultCellEditor(comMaxillaryConnecterToothPosition));
							table.getColumn("舌侧对抗#1").setCellEditor(rowEditor3);
							rowEditor4.setEditorAt(rowNum, new DefaultCellEditor(comMaxillaryConnecterToothPosition));
							table.getColumn("舌侧对抗#2").setCellEditor(rowEditor4);
							rowEditor5.setEditorAt(rowNum, new DefaultCellEditor(comMaxillaryConnecterComponentName));
							table.getColumn("组件名称").setCellEditor(rowEditor5);
							break;
						case "rpd.components.DentureBase":
							rowEditor5.setEditorAt(rowNum, new DefaultCellEditor(comBaseComponentName));
							table.getColumn("组件名称").setCellEditor(rowEditor5);
							break;
					}
				} catch (Exception e) {
					System.out.println("Got a Exception：" + e.getMessage());
					e.printStackTrace();
				}
//				rowNum = model.getRowCount();
				rowNum++;
//					mandibular_plan_table.invalidate();
			}
		}
		return  table;
	}

	public int[] getToothList (String toothPosition){
		int[] digitList = new int[2];
		String digitString = "";

		if(toothPosition != null && !"".equals(toothPosition)){
			for(int i =0 ;i<toothPosition.length();i++){
				if(toothPosition.charAt(i)>=48 && toothPosition.charAt(i)<=57){
					digitString += toothPosition.charAt(i);
				}
			}
		}
		int temp = Integer.parseInt(digitString);
		digitList[0] = temp % 10 ;
		digitList[1] = temp / 10 ;

		return digitList;
	}

	public int[] getToothArrayList (String toothPosition) {
		int[] digitList = new int[4];
		String digitString = "";

		if(toothPosition != null && !"".equals(toothPosition)){
			for(int i =0 ;i<toothPosition.length();i++){
				if(toothPosition.charAt(i)>=48 && toothPosition.charAt(i)<=57){
					digitString += toothPosition.charAt(i);
				}
			}
		}
		int temp = Integer.parseInt(digitString);
		for(int i = 0;temp>0;i++){
			digitList[i] = temp % 10 ;
			temp /= 10;
		}
		return digitList;
	}


	public int[] getToothArrayListEight (String toothPosition) {
		int[] digitList = new int[8];
		String digitString = "";

		if(toothPosition != null && !"".equals(toothPosition)){
			for(int i =0 ;i<toothPosition.length();i++){
				if(toothPosition.charAt(i)>=48 && toothPosition.charAt(i)<=57){
					digitString += toothPosition.charAt(i);
				}
			}
		}
		int temp = Integer.parseInt(digitString);
		for(int i = 0;temp>0;i++){
			digitList[i] = temp % 10 ;
			temp /= 10;
		}
		return digitList;
	}

	public int[] getToothArrayListUnsigned (List<String> lingualConfrontation, int size) {
		int[] digitList = new int[size*2];
		String digitString = "";

		for(String toothPosition:lingualConfrontation){
			if(toothPosition != null && !"".equals(toothPosition)){
				for(int i =0 ;i<toothPosition.length();i++){
					if(toothPosition.charAt(i)>=48 && toothPosition.charAt(i)<=57){
						digitString += toothPosition.charAt(i);
					}
				}
			}
		}

		int temp = Integer.parseInt(digitString);
//		System.out.print("getToothArrayListUnsigned = " + digitString + " temp = "+temp+" digitList = ");

		for(int i = 0; temp >0;i++){
			digitList[i] = temp % 10 ;
			temp /= 10;
//			System.out.print(digitList[i]);
		}
//		System.out.print("\t\n");
		return digitList;
	}

	public boolean includeDigit(String content) {
		boolean flag = false;
		Pattern p = Pattern.compile(".*\\d+.*");
		Matcher m = p.matcher(content);
		if (m.matches()) {
			flag = true;
		}
		return flag;
	}

	private static void hideTableColumn(JTable table, int column)
	{
		TableColumn tc = table.getColumnModel().getColumn(column);
		tc.setMinWidth(0);
		tc.setMaxWidth(0);
	}
	private static void showTableColumn(JTable table, int column)
	{
		TableColumn tc = table.getColumnModel().getColumn(column);
		tc.setMinWidth(200);
		tc.setMaxWidth(200);
	}

}
