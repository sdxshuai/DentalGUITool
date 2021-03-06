package misc;

import java.io.FileInputStream;
import java.io.IOException;

import javax.print.*;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.PrintQuality;

public class PrintImage {
	public void drawImage(String fileNameList[], int count) {
		try {
			DocFlavor dof = DocFlavor.INPUT_STREAM.PNG;
			PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();

			PrintService printService[] = PrintServiceLookup.lookupPrintServices(dof, pras);
			PrintService defaultPrintService = PrintServiceLookup.lookupDefaultPrintService();
			PrintService ps = ServiceUI.printDialog(
					null, 200, 200, printService,
					 defaultPrintService, dof, pras);

			pras.add(OrientationRequested.PORTRAIT);
			pras.add(new Copies(count));
			pras.add(PrintQuality.HIGH);
			DocAttributeSet das = new HashDocAttributeSet();

			// 设置打印纸张的大小（以毫米为单位）
			das.add(new MediaPrintableArea(0, 0, 210, 296, MediaPrintableArea.MM));
			for (String fileName:fileNameList) {
				FileInputStream fin = new FileInputStream(fileName);

				Doc doc = new SimpleDoc(fin, dof, das);

				DocPrintJob job = ps.createPrintJob();

				job.print(doc, pras);
				fin.close();
			}
		} catch (IOException ie) {
			ie.printStackTrace();
		} catch (PrintException pe) {
			pe.printStackTrace();
		}
	}


	public static void main(String[] args) {

	}
}
