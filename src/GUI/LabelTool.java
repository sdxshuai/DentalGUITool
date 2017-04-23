package GUI;

import java.awt.EventQueue;
import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.table.DefaultTableModel;
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

import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.XSD;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.Sizes;

import ontologies.LabelModifier;
import ontologies.LabelPosComparator;
import exceptions.PropertyValueException;
import exceptions.ToothMapException;
import exceptions.ToothModifierException;
import exceptions.rpd.ClaspAssemblyException;
import exceptions.rpd.EdentulousTypeException;
import exceptions.rpd.RuleException;
import exceptions.rpd.ToothPosException;
import ontologies.BooleanPropertyValue;
import ontologies.Descriptions;
import ontologies.Descriptions.PropertyDescription;
import misc.ToothMap;
import ontologies.DoublePropertyValue;
import ontologies.IntPropertyValue;
import ontologies.ListPropertyValue;
import ontologies.OntFunc;
import ontologies.PropertyLabel;
import ontologies.PropertyValue;
import ontologies.StringPropertyValue;
import rpd.BeamSearch;
import rpd.RPDPlan;
import rpd.SearchRPDPlan;
import rpd.oral.Instantialize;
import rpd.oral.Mouth;
import rpd.oral.Tooth;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.awt.FlowLayout;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.Font;
import javax.swing.JScrollPane;
import javax.swing.JPopupMenu;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;

import java.awt.event.ItemListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;

public class LabelTool {

	private OntModel dental_ont = null;

	private OntModel mouth_ont = null;
	private Mouth mouth = null;
	private List<RPDPlan> mandibular_rpd_plans = null;
	private List<RPDPlan> maxillary_rpd_plans = null;

	private Descriptions des = null;

	private File owl_file = null;
	private File choosed_file = null;
	private File current_dir = new File("D:\\Codes\\DentalGUITool\\data");

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

	//private ToothMap is_missing_map = null;

	private JComboBox<Integer> plan_choice = null;

	private JPopupMenu rpd_plan_menu = null;
	private JPopupMenu tooth_menu = null;
	private JPopupMenu component_menu = null;

	/**
	 * Launch the application.
	 *
	 * @throws IOException
	 * @throws PropertyValueException
	 */
	public static void main(String[] args) throws IOException, PropertyValueException {

//		File owl_file = new File("res//CDSSinRPD_ontology_161209.owl");
		File owl_file = new File("res//CDSSinRPD_ontology_170406.owl");
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

	/**
	 * Create the application.
	 *
	 * @throws PropertyValueException
	 * @throws IOException
	 */
	public LabelTool(File owl_file, File modifier_file) throws PropertyValueException, IOException {

		dental_ont = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		dental_ont.read("file:" + owl_file.getCanonicalPath());
//		org.apache.jena.ontology.Individual cc = dental_ont.getIndividual("http://www.semanticweb.org/msiip/ontologies/CDSSinRPD#combined_clasp_1");
//		org.apache.jena.ontology.ObjectProperty com_pos = dental_ont.getObjectProperty("component_position");
		des = new Descriptions(dental_ont, modifier_file);
		this.owl_file = owl_file;
		initialize();
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

		JPanel panel_west = new JPanel();
		frame.getContentPane().add(panel_west, BorderLayout.WEST);
		panel_west.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JScrollPane label_scroll_pane = new JScrollPane();
		panel_west.add(label_scroll_pane);

		JCheckBox chckbx_show_all_labels = new JCheckBox("显示全部");
		this.chckbx_show_all_labels = chckbx_show_all_labels;

		label_table = new JTable();
		ListSelectionModel label_table_model = label_table.getSelectionModel();
		label_table_model.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {

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
		panel_center.setLayout(new GridLayout(1, 1, 0, 0));

		JTextPane emr_text = new JTextPane();
		emr_text.setEditable(false);
		this.emr_text = emr_text;

		emr_text.setFont(new Font("微软雅黑", Font.PLAIN, 22));
		JScrollPane emr_scroll_pane = new JScrollPane(emr_text);
		panel_center.add(emr_scroll_pane);

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
						if (xml_file.exists()) {
							label_xml_file = xml_file;
							readLabelsFromXml(xml_file);
						}

					} catch (IOException e) {
						e.printStackTrace();
					} catch (ParserConfigurationException e) {
						e.printStackTrace();
					} catch (SAXException e) {
						e.printStackTrace();
					} catch (PropertyValueException e) {
						e.printStackTrace();
					} catch (BadLocationException e) {
						e.printStackTrace();
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

		JButton gen_design_button = new JButton("生成设计图");
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
					@SuppressWarnings("unused")
					List<RPDPlan> plans_buffer = mandibular_rpd_plans;
					showRPDPlans();
				} catch (ParserConfigurationException | SAXException | IOException | ToothMapException
						| ToothModifierException | PropertyValueException e) {
					e.printStackTrace();
				} catch (RuleException | ClaspAssemblyException | ToothPosException | EdentulousTypeException e) {
					e.printStackTrace();
				}
			}
		});
		panel_north.add(gen_design_button, "8, 2");
		chckbx_show_all_labels.setFont(new Font("微软雅黑", Font.PLAIN, 18));
		panel_north.add(chckbx_show_all_labels, "10, 2");

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

	private void showRPDPlans() throws IOException {

		if (mandibular_rpd_plans == null || mandibular_rpd_plans.size() == 0)
			return;

		JDialog design_dialog = new JDialog(this.frame, "设计图");
		JPanel rpd_plan_panel = new JPanel(new BorderLayout());
		design_dialog.add(rpd_plan_panel);

		rpd_plan_tree = new JTree();
		rpd_plan_tree.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {

				if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3 && rpd_plan_tree.isEnabled()) {

					TreePath path = rpd_plan_tree.getPathForLocation(e.getX(), e.getY());
					if (path != null) {

						rpd_plan_tree.setSelectionPath(path);
						DefaultMutableTreeNode selected_node = (DefaultMutableTreeNode) path.getLastPathComponent();
						Object user_obj = selected_node.getUserObject();
						if (user_obj.getClass().equals(String.class)) {
							String node_str = (String) user_obj;
							if (node_str.startsWith("方案"))
								rpd_plan_menu.show(rpd_plan_tree, e.getX(), e.getY());
						} else if (user_obj.getClass().equals(Tooth.class)) {
							tooth_menu.show(rpd_plan_tree, e.getX(), e.getY());
						} else if (rpd.components.Component.class.isInstance(user_obj)) {
							component_menu.show(rpd_plan_tree, e.getX(), e.getY());
						} else {
						}
					}
				}
			}
		});
		rpd_plan_tree.setFont(new Font("微软雅黑", Font.PLAIN, 18));

		Vector<Integer> plan_index = new Vector<Integer>();
		for (int i = 0; i < mandibular_rpd_plans.size(); i++)
			plan_index.addElement(i);
		plan_choice = new JComboBox<Integer>(plan_index);
		rpd_plan_panel.add(plan_choice, BorderLayout.NORTH);
		rpd_plan_panel.add(rpd_plan_tree, BorderLayout.CENTER);
		plan_choice.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (plan_choice.getSelectedItem() != null) {
					int plan_index = (Integer) (plan_choice.getSelectedItem());
					RPDPlan rpd_plan = mandibular_rpd_plans.get(plan_index);
					current_rpd_plan = rpd_plan;
					JTree new_rpd_plan_tree = buildRPDPlanTree(rpd_plan, plan_index);
					if (new_rpd_plan_tree != null) {
						rpd_plan_tree.setModel(new_rpd_plan_tree.getModel());
						TreeNode root = (TreeNode) rpd_plan_tree.getModel().getRoot();
						expandAll(rpd_plan_tree, new TreePath(root), true);
					}
				}
			}
		});

		design_dialog.setSize(1000, 1000);
		design_dialog.setLocationRelativeTo(null);
		design_dialog.setVisible(true);
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

		for (ArrayList<Tooth> tooth : plan_teeth) {
			Set<rpd.components.Component> components = tooth_components.get(tooth);
			DefaultMutableTreeNode tooth_node = new DefaultMutableTreeNode(tooth);
			top_node.add(tooth_node);
			for (rpd.components.Component component : components) {
				DefaultMutableTreeNode component_node = new DefaultMutableTreeNode(component);
				tooth_node.add(component_node);
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

	private void addEMRTextPopup(Component component, final JPopupMenu popup) {
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
}
