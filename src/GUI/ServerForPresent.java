package GUI;
import exceptions.PropertyValueException;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import rpd.RPDPlan;
import rpd.oral.Instantialize;
import rpd.oral.Mouth;

import java.io.*;
import java.net.*;
import java.util.*;

public class ServerForPresent {
    /**
     * @param args
     */
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
        // TODO Auto-generated method stub
        while(true)
        {

            ServerSocket serverSocket = new ServerSocket(9955);
            System.out.println("Swait for connect......");
            Socket socket=serverSocket.accept();
            System.out.println("S connet from:"+socket.getInetAddress().getHostAddress());

            BufferedReader br=new BufferedReader(new InputStreamReader(socket.getInputStream()));//br.readLine();// success...
            PrintWriter pw=new PrintWriter(socket.getOutputStream(),true);
            //pw.println(string);

            boolean goon=true;
            int count = 0;
            while(goon){
                try {
                    String string = br.readLine();
                    if (string == null) continue;
                    count += 1;
                    myFunc(string, count);
                    if (string.equals("end") == false) {
                        pw.println(string);
                    }
                    break;
                }
                catch (Exception e){
                    continue;
                }
            }

            serverSocket.close();
            br.close();
            pw.close();

        }
    }

    public static void myFunc(String inputTextFileName, int count)
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
        File inputEmr = new File("data\\" + inputTextFileName);
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
}
