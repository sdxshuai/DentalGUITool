package GUI;

import emrpaser.rule.ruleParserWithStats;
import exceptions.PropertyValueException;
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.opencv.core.Mat;
import rpd.RPDPlan;
import rpd.SearchRPDPlan;
import rpd.components.*;
import rpd.conceptions.ClaspMaterial;
import rpd.conceptions.Position;
import rpd.oral.EdentulousSpace;
import rpd.oral.Instantialize;
import rpd.oral.Mouth;
import rpd.oral.Tooth;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static rpd.conceptions.Position.Mesial;

public class LabelTool_forPresent {
    static {
        System.loadLibrary("RpdDesignLib");
        System.loadLibrary("opencv_java330");
    }

    public File owlFile = null;
    public File modifierFile = null;
    public String pictureOutputDir = null;

    /**
     * Create the application.
     *
     * @throws PropertyValueException
     * @throws IOException
     */
    public LabelTool_forPresent(File inputOwlFile, File inputModifierFile, String inputPictureOutputDir)
            throws PropertyValueException, IOException {
        owlFile = inputOwlFile;
        modifierFile = inputModifierFile;
        pictureOutputDir = inputPictureOutputDir;
    }

    public static native Mat getRpdDesign(OntModel ontModel, Mat mat);

    public static native Mat getRpdDesign(OntModel ontModel);

    public static void main(String[] args)
            throws PropertyValueException, IOException,
            emrpaser.exceptions.PropertyValueException,
            javax.xml.parsers.ParserConfigurationException,
            javax.xml.transform.TransformerException,
            org.xml.sax.SAXException,
            exceptions.ToothMapException,
            exceptions.PropertyValueException,
            exceptions.ToothModifierException,
            exceptions.rpd.RuleException,
            exceptions.rpd.ToothPosException,
            exceptions.rpd.ClaspAssemblyException,
            exceptions.rpd.EdentulousTypeException {
        // get instanced xml file
        File inputOwlFile = new File("res\\base_template.owl");
        File inputModifierFile = new File("res\\label_modifier_description.txt");
        String inputPictureOutputDir = "F:\\forPresent\\picture";
        LabelTool_forPresent labelTool = new LabelTool_forPresent(inputOwlFile, inputModifierFile, inputPictureOutputDir);
        File inputEmr = new File("data//54.txt");

        String inputEmrCanonicalPath = inputEmr.getCanonicalPath();
        int dotIndex = inputEmrCanonicalPath.lastIndexOf(".");
        int gangIndex = inputEmrCanonicalPath.lastIndexOf("\\");
        String inputEmrFileName = inputEmrCanonicalPath.substring(gangIndex+1, dotIndex);
        File xmlFile = labelTool.getXml(inputEmr, inputEmrFileName);

        // get rpd plans
        OntModel mouth_ont = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        mouth_ont.read("file:" + labelTool.owlFile.getCanonicalPath());
        Instantialize.convertXmlToOnt(mouth_ont, xmlFile);
        Mouth mouth = new Mouth(mouth_ont);
        List<RPDPlan> mandibularRpdPlans = labelTool.getMandibularRpdPlans(mouth);
        List<RPDPlan> maxillaryRpdPlans = labelTool.getMaxillaryRpdPlans(mouth);

        // get descriptions
        if (!(mandibularRpdPlans == null || mandibularRpdPlans.size() == 0)) {
            labelTool.getDescription(mandibularRpdPlans, inputEmrFileName + "_Mandibular");
        }
        if (!(maxillaryRpdPlans == null || maxillaryRpdPlans.size() == 0)) {
            labelTool.getDescription(maxillaryRpdPlans, inputEmrFileName + "_Maxillary");
        }

        // get pictures
        if (!(mandibularRpdPlans == null || mandibularRpdPlans.size() == 0)) {
            labelTool.generateAndSaveRPDPlanPicture(
                    mandibularRpdPlans, inputEmrFileName, labelTool.pictureOutputDir);
        }
        if (!(maxillaryRpdPlans == null || maxillaryRpdPlans.size() == 0)) {
            labelTool.generateAndSaveRPDPlanPicture(
                    maxillaryRpdPlans, inputEmrFileName, labelTool.pictureOutputDir);
        }
        int a = 0;

    }

    public File getXml(File inputEmr, String inputEmrFileName)
            throws IOException, emrpaser.exceptions.PropertyValueException,
            javax.xml.parsers.ParserConfigurationException, javax.xml.transform.TransformerException {
        File resDir = new File("res");
        File excelFile = new File(resDir.getCanonicalPath() + "\\ontology_definition_1209.xlsx");
        File ruleFile = new File(resDir.getCanonicalPath() + "\\rules_all_with_value_20170116.txt");
        File generalRegexFile = new File(resDir.getCanonicalPath() + "\\general_regex_with_value.txt");
        ruleParserWithStats p = new ruleParserWithStats(ruleFile, generalRegexFile, owlFile, modifierFile, excelFile);
        String xml_file_path = "F:\\forPresent\\xml\\" + inputEmrFileName + ".xml";
        File xmlFile = new File(xml_file_path);
        p.parseFile(inputEmr, xmlFile);
        return xmlFile;
    }

    public List<RPDPlan> getMandibularRpdPlans(Mouth mouth)
            throws exceptions.rpd.RuleException,
            exceptions.rpd.ToothPosException,
            exceptions.rpd.ClaspAssemblyException,
            exceptions.rpd.EdentulousTypeException{
        return SearchRPDPlan.searchMandibular(mouth);
    }

    public List<RPDPlan> getMaxillaryRpdPlans(Mouth mouth)
            throws exceptions.rpd.RuleException,
            exceptions.rpd.ToothPosException,
            exceptions.rpd.ClaspAssemblyException,
            exceptions.rpd.EdentulousTypeException{
        return SearchRPDPlan.searchMaxillary(mouth);
    }
    
    public void getDescription(List<RPDPlan> plans, String filePrefix) throws java.io.IOException {
        int plan_count = 0;
        String descFilePath = "F:\\forPresent\\description\\" + filePrefix + "_description.txt";
        checkAndCreateFile(descFilePath);
        File descFile = new File(descFilePath);
        FileOutputStream fileWritter = new FileOutputStream(descFile);
        String string2write = null;

        for (RPDPlan plan:plans) {
            plan_count++;
            string2write = "Plan " + String.valueOf(plan_count) + "\n";
            fileWritter.write(string2write.getBytes());
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
                    String componentDesc = component.toString();
                    string2write = componentDesc + "\n";
                    fileWritter.write(string2write.getBytes());
                }
            }
            fileWritter.write("\n".getBytes());
        }
        fileWritter.close();
    }

    public void generateAndSaveRPDPlanPicture(List<RPDPlan> plans, String txtFileName, String outputDir)
            throws java.io.IOException, exceptions.rpd.RuleException {
        if (plans == null || plans.size() == 0) {
            return;
        }

        String plan_position_str = plans.get(0).getPosition().toString();
        int design_count = 0;
        for (RPDPlan plan : plans) {
            design_count++;
            OntModel design_ont = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
            design_ont.read("file:" + owlFile.getCanonicalPath());
            planToOwl(plan, design_ont);
            String output_ont =
                    outputDir + "//" + txtFileName + "_" + plan_position_str + "_RPD_design_" + design_count + ".owl";
            String output_picture =
                    outputDir + "//" + txtFileName + "_" + plan_position_str + "_RPD_design_" + design_count + ".png";
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

    private void planToOwl(RPDPlan plan, OntModel resOnt) throws exceptions.rpd.RuleException {
        String SOURCE = "http://www.semanticweb.org/msiip/ontologies/CDSSinRPD";
        String NS = SOURCE + "#";
        int indCount = 0;

        Map<ArrayList<Tooth>, Set<Component>> tooth_components = plan.getToothComponents();
        for (ArrayList<Tooth> tooth_pos : tooth_components.keySet())
            for (rpd.components.Component component : tooth_components.get(tooth_pos)) {
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
    }

    private void addEdentulousSpace(EdentulousSpace edentulousSpace, OntModel model, String NS, int indCount) {

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
    }

    private void addDentureBase(rpd.components.Component base, OntModel model, String NS, int indCount) {
        DentureBase dentureBase = (DentureBase) base;
        OntClass dentureBaseClass = model.getOntClass(NS + "denture_base");
        Individual indDentureBase = model.createIndividual(NS + indCount, dentureBaseClass);
        setComponentToothPos(model, indDentureBase, dentureBase.getToothPos(), NS);
    }

    private void addAkerClaspToOwl(rpd.components.Component clasp, OntModel model, String NS, int indCount) {
        AkerClasp akerClasp = (AkerClasp) clasp;
        OntClass akerClaspClass = model.getOntClass(NS + "aker_clasp");
        Individual indAkerClasp = model.createIndividual(NS + indCount, akerClaspClass);
        setClaspTipDirection(model, indAkerClasp, akerClasp.getTipDirection(), NS);
        setClaspMaterial(model, indAkerClasp, akerClasp.getMaterial(), NS);
        setComponentToothPos(model, indAkerClasp, akerClasp.getToothPos(), NS);
    }

    private void addWroughtWireClaspToOwl(rpd.components.Component clasp, OntModel model, String NS, int indCount) {
        WroughtWireClasp wroughtWireClasp = (WroughtWireClasp) clasp;
        OntClass wroughtWireClaspClass = model.getOntClass(NS + "wrought_wire_clasp");
        Individual indWroughtWireClasp = model.createIndividual(NS + indCount, wroughtWireClaspClass);
        setClaspTipDirection(model, indWroughtWireClasp, wroughtWireClasp.getTipDirection(), NS);
        setClaspMaterial(model, indWroughtWireClasp, wroughtWireClasp.getMaterial(), NS);
        setComponentToothPos(model, indWroughtWireClasp, wroughtWireClasp.getToothPos(), NS);
    }

    private void addCombinationClaspToOwl(rpd.components.Component clasp, OntModel model, String NS, int indCount) {
        CombinationClasp combinationClasp = (CombinationClasp) clasp;
        OntClass combinationClaspClass = model.getOntClass(NS + "combination_clasp");
        Individual indCombinationClasp = model.createIndividual(NS + indCount, combinationClaspClass);
        setClaspTipDirection(model, indCombinationClasp, combinationClasp.getTipDirection(), NS);
        setComponentToothPos(model, indCombinationClasp, combinationClasp.getToothPos(), NS);
    }

    private void addCanineClaspToOwl(rpd.components.Component clasp, OntModel model, String NS, int indCount) {
        CanineClasp canineClasp = (CanineClasp) clasp;
        OntClass canineClaspClass = model.getOntClass(NS + "canine_clasp");
        Individual indCanineClasp = model.createIndividual(NS + indCount, canineClaspClass);
        setClaspTipDirection(model, indCanineClasp, canineClasp.getTipDirection(), NS);
        setClaspMaterial(model, indCanineClasp, canineClasp.getMaterial(), NS);
    }

    private void addCanineAkerClaspToOwl(rpd.components.Component clasp, OntModel model, String NS, int indCount) {
        CanineAkerClasp canineAkerClasp = (CanineAkerClasp) clasp;
        OntClass canineAkerClaspClass = model.getOntClass(NS + "canine_aker_clasp");
        Individual indCanineAkerClasp = model.createIndividual(NS + indCount, canineAkerClaspClass);
        setClaspTipDirection(model, indCanineAkerClasp, canineAkerClasp.getTipDirection(), NS);
        setClaspMaterial(model, indCanineAkerClasp, canineAkerClasp.getMaterial(), NS);
        setComponentToothPos(model, indCanineAkerClasp, canineAkerClasp.getToothPos(), NS);
    }

    private void addHalfHalfClaspToOwl(rpd.components.Component clasp, OntModel model, String NS, int indCount) {
        HalfHalfClasp halfHalfClasp = (HalfHalfClasp) clasp;
        OntClass halfHalfClaspClass = model.getOntClass(NS + "half_and_half_clasp");
        Individual indHalfHalfClasp = model.createIndividual(NS + indCount, halfHalfClaspClass);
        setClaspMaterial(model, indHalfHalfClasp, halfHalfClasp.getMaterial(), NS);
        setComponentToothPos(model, indHalfHalfClasp, halfHalfClasp.getToothPos(), NS);
    }

    private void addBackActionClaspToOwl(rpd.components.Component clasp, OntModel model, String NS, int indCount) {
        BackActionClasp backActionClasp = (BackActionClasp) clasp;
        OntClass backActionClaspClass = model.getOntClass(NS + "back_action_clasp");
        Individual indBackActionClasp = model.createIndividual(NS + indCount, backActionClaspClass);
        setClaspTipDirection(model, indBackActionClasp, backActionClasp.getTipDirection(), NS);
        setClaspMaterial(model, indBackActionClasp, backActionClasp.getMaterial(), NS);
        setComponentToothPos(model, indBackActionClasp, backActionClasp.getToothPos(), NS);
    }

    private void addReverseBackActionClaspToOwl(
            rpd.components.Component clasp, OntModel model, String NS, int indCount) {
        ReverseBackActionClasp reverseBackActionClasp = (ReverseBackActionClasp) clasp;
        OntClass reverseBackActionClaspClass = model.getOntClass(NS + "reverse_back_action_clasp");
        Individual indReverseBackActionClasp = model.createIndividual(NS + indCount, reverseBackActionClaspClass);
        setClaspTipDirection(model, indReverseBackActionClasp, reverseBackActionClasp.getTipDirection(), NS);
        setClaspMaterial(model, indReverseBackActionClasp, reverseBackActionClasp.getMaterial(), NS);
        setComponentToothPos(model, indReverseBackActionClasp, reverseBackActionClasp.getToothPos(), NS);
    }

    private void addRingClaspToOwl(rpd.components.Component clasp, OntModel model, String NS, int indCount) {
        RingClasp ringClasp = (RingClasp) clasp;
        OntClass ringClaspClass = model.getOntClass(NS + "ring_clasp");
        Individual indRingClasp = model.createIndividual(NS + indCount, ringClaspClass);
        setClaspTipDirection(model, indRingClasp, ringClasp.getTipDirection(), NS);
        setClaspMaterial(model, indRingClasp, ringClasp.getMaterial(), NS);
        setComponentToothPos(model, indRingClasp, ringClasp.getToothPos(), NS);
    }

    private void addCombinedClaspToOwl(rpd.components.Component clasp, OntModel model, String NS, int indCount) {
        CombinedClasp combinedClasp = (CombinedClasp) clasp;
        OntClass combinedClaspClass = model.getOntClass(NS + "combined_clasp");
        Individual indCombinedClasp = model.createIndividual(NS + indCount, combinedClaspClass);
        setClaspMaterial(model, indCombinedClasp, combinedClasp.getMaterial(), NS);
        setComponentToothPos(model, indCombinedClasp, combinedClasp.getToothPos(), NS);
    }

    private void addEmbrasureClaspToOwl(rpd.components.Component clasp, OntModel model, String NS, int indCount) {
        EmbrasureClasp embrasureClasp = (EmbrasureClasp) clasp;
        OntClass embrasureClaspClass = model.getOntClass(NS + "embrasure_clasp");
        Individual indEmbrasureClasp = model.createIndividual(NS + indCount, embrasureClaspClass);
        setClaspMaterial(model, indEmbrasureClasp, embrasureClasp.getMaterial(), NS);
        setComponentToothPos(model, indEmbrasureClasp, embrasureClasp.getToothPos(), NS);
    }

    private void addContinuousClaspToOwl(rpd.components.Component clasp, OntModel model, String NS, int indCount) {
        ContinuousClasp continuousClasp = (ContinuousClasp) clasp;
        OntClass continuousClaspClass = model.getOntClass(NS + "continuous_clasp");
        Individual indContinuousClasp = model.createIndividual(NS + indCount, continuousClaspClass);
        setClaspMaterial(model, indContinuousClasp, continuousClasp.getMaterial(), NS);
        setComponentToothPos(model, indContinuousClasp, continuousClasp.getToothPos(), NS);
    }

    private void addRPAClaspToOwl(rpd.components.Component clasp, OntModel model, String NS, int indCount) {
        RPAClasp RPAClasp = (RPAClasp) clasp;
        OntClass RPAClaspClass = model.getOntClass(NS + "RPA_clasps");
        Individual indRPAClasp = model.createIndividual(NS + indCount, RPAClaspClass);
        setClaspTipDirection(model, indRPAClasp, RPAClasp.getTipDirection(), NS);
        setClaspMaterial(model, indRPAClasp, RPAClasp.getMaterial(), NS);
        setComponentToothPos(model, indRPAClasp, RPAClasp.getToothPos(), NS);
    }

    private void addOcclusalRestToOwl(rpd.components.Component rest, OntModel model, String NS, int indCount) {
        OcclusalRest occlusalRest = (OcclusalRest) rest;
        OntClass occlusalRestClass = model.getOntClass(NS + "occlusal_rest");
        Individual indOcclusalRest = model.createIndividual(NS + indCount, occlusalRestClass);
        setRestMesialOrDistal(model, indOcclusalRest, occlusalRest.getMesialOrDistal(), NS);
        setComponentToothPos(model, indOcclusalRest, occlusalRest.getToothPos(), NS);
    }

    private void addLingualRestToOwl(rpd.components.Component rest, OntModel model, String NS, int indCount) {
        LingualRest lingualRest = (LingualRest) rest;
        OntClass lingualRestClass = model.getOntClass(NS + "lingual_rest");
        Individual indLingualRest = model.createIndividual(NS + indCount, lingualRestClass);
        setComponentToothPos(model, indLingualRest, lingualRest.getToothPos(), NS);
    }

    private void addSinglePalatalStrapConnectorToOwl(
            rpd.components.Component connector, OntModel model, String NS, int indCount) {
        SinglePalatalStrapConnector singlePalatalStrapConnector = (SinglePalatalStrapConnector) connector;
        OntClass singlePalatalStrapConnectorClass = model.getOntClass(NS + "single_palatal_strap");
        Individual indSinglePalatalStrapConnector
                = model.createIndividual(NS + indCount, singlePalatalStrapConnectorClass);
        setComponentToothPos(model, indSinglePalatalStrapConnector, singlePalatalStrapConnector.getToothPos(), NS);
    }

    private void addCombinationAnteriorPosteriorPalatalStrapConnectorToOwl(
            rpd.components.Component connector, OntModel model, String NS, int indCount) {
        CombinationAnteriorPosteriorPalatalStrapConnector CAPPSConnector
                = (CombinationAnteriorPosteriorPalatalStrapConnector) connector;
        OntClass CAPPSConnectorClass = model.getOntClass(NS + "combination_anterior_posterior_palatal_strap");
        Individual indCAPPSConnector = model.createIndividual(NS + indCount, CAPPSConnectorClass);
        setComponentToothPos(model, indCAPPSConnector, CAPPSConnector.getToothPos(), NS);
    }

    private void addPalatalPlateConnectorToOwl(
            rpd.components.Component connector, OntModel model, String NS, int indCount) {
        PalatalPlateConnector palatalPlateConnector = (PalatalPlateConnector) connector;
        OntClass palatalPlateConnectorClass = model.getOntClass(NS + "palatal_plate");
        Individual indPalatalPlateConnector = model.createIndividual(NS + indCount, palatalPlateConnectorClass);
        setComponentToothPos(model, indPalatalPlateConnector, palatalPlateConnector.getToothPos(), NS);
        setMajorConnectorLingualConfrontation(
                model, indPalatalPlateConnector, palatalPlateConnector.getLingualConfrontation(), NS);
    }

    private void addFullPalatalPlateConnectorToOwl(
            rpd.components.Component cnnector, OntModel model, String NS, int indCount) {
        FullPalatalPlateConnector fullPalatalPlateConnector = (FullPalatalPlateConnector) cnnector;
        OntClass fullPalatalPlateConnectorClass = model.getOntClass(NS + "full_palatal_plate");
        Individual indFullPalatalPlateConnector
                = model.createIndividual(NS + indCount, fullPalatalPlateConnectorClass);
        setComponentToothPos(model, indFullPalatalPlateConnector, fullPalatalPlateConnector.getToothPos(), NS);
        setMajorConnectorLingualConfrontation(
                model, indFullPalatalPlateConnector, fullPalatalPlateConnector.getLingualConfrontation(), NS);
    }

    private void addModifiedPalatalPlateConnectorToOwl(
            rpd.components.Component connector, OntModel model, String NS, int indCount) {
        ModifiedPalatalPlateConnector modifiedPalatalPlateConnector = (ModifiedPalatalPlateConnector) connector;
        OntClass modifiedPalatalPlateConnectorClass = model.getOntClass(NS + "modified_palatal_plate");
        Individual indModifiedPalatalPlateConnector
                = model.createIndividual(NS + indCount, modifiedPalatalPlateConnectorClass);
        setComponentToothPos(model, indModifiedPalatalPlateConnector, modifiedPalatalPlateConnector.getToothPos(), NS);
        setMajorConnectorLingualConfrontation(
                model, indModifiedPalatalPlateConnector, modifiedPalatalPlateConnector.getLingualConfrontation(), NS);
    }

    private void addLingualBarConnectorToOwl(
            rpd.components.Component connector, OntModel model, String NS, int indCount) {
        LingualBarConnector lingualBarConnector = (LingualBarConnector) connector;
        OntClass lingualBarConnectorClass = model.getOntClass(NS + "lingual_bar");
        Individual indLingualBarConnector = model.createIndividual(NS + indCount, lingualBarConnectorClass);
        setComponentToothPos(model, indLingualBarConnector, lingualBarConnector.getToothPos(), NS);
    }

    private void addLingualPlateConnectorToOwl(
            rpd.components.Component connector, OntModel model, String NS, int indCount) {
        LingualPlateConnector lingualPlateConnector = (LingualPlateConnector) connector;
        OntClass lingualPlateConnectorClass = model.getOntClass(NS + "lingual_plate");
        Individual indLingualPlateConnector = model.createIndividual(NS + indCount, lingualPlateConnectorClass);
        setComponentToothPos(model, indLingualPlateConnector, lingualPlateConnector.getToothPos(), NS);
        setMajorConnectorLingualConfrontation(
                model, indLingualPlateConnector, lingualPlateConnector.getLingualConfrontation(), NS);
    }

    private void setClaspTipDirection(OntModel model, Individual indClasp, Position tip_direction, String NS) {
        OntProperty clasp_tip_direction = model.getOntProperty(NS + "clasp_tip_direction");
        if (tip_direction == Mesial) {
            indClasp.addProperty(clasp_tip_direction, model.createTypedLiteral(0));
        } else if (tip_direction == Position.Distal) {
            indClasp.addProperty(clasp_tip_direction, model.createTypedLiteral(1));
        }
    }

    private void setClaspMaterial(OntModel model, Individual indClasp, ClaspMaterial material, String NS) {
        OntProperty clasp_material = model.getOntProperty(NS + "clasp_material");
        if (material == ClaspMaterial.Cast) {
            indClasp.addProperty(clasp_material, model.createTypedLiteral(0));
        } else if (material == ClaspMaterial.WW) {
            indClasp.addProperty(clasp_material, model.createTypedLiteral(1));
        }
    }

    private void setRestMesialOrDistal(OntModel model, Individual indRest, Position mesial_or_distal, String NS) {
        OntProperty rest_mesial_or_distal = model.getOntProperty(NS + "rest_mesial_or_distal");
        if (mesial_or_distal == Mesial) {
            indRest.addProperty(rest_mesial_or_distal, model.createTypedLiteral(0));
        } else if (mesial_or_distal == Position.Distal) {
            indRest.addProperty(rest_mesial_or_distal, model.createTypedLiteral(1));
        }
    }

    private void setComponentToothPos(OntModel model, Individual indComponent, ArrayList<Tooth> tooth_pos, String NS) {
        OntProperty component_position = model.getObjectProperty(NS + "component_position");
        for (Tooth tooth : tooth_pos) {
            indComponent.addProperty(component_position, model.getIndividual(NS + tooth.toString()));
        }
    }

    private void setMajorConnectorLingualConfrontation(
            OntModel model, Individual indMajorConnector, HashSet<Tooth> lingualConfrontation, String NS) {
        OntProperty lingual_confrontation = model.getObjectProperty(NS + "lingual_confrontation");
        for (Tooth tooth:lingualConfrontation) {
            indMajorConnector.addProperty(lingual_confrontation, model.getIndividual(NS + tooth.toString()));
        }
    }
}
