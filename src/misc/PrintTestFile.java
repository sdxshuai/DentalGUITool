package misc;

/**
 * Created by sdxshuai on 2017/7/13.
 */

import org.apache.jena.base.Sys;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;
import javax.print.*;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.PrintQuality;

public class PrintTestFile {
	public void print(File text_file, String design_image_name, String base_image_name, String text_file_name, int count) throws java.io.IOException{
		BufferedImage image = convertTxtToGraphic(text_file, text_file_name, new Font("微软雅黑", Font.PLAIN, 24));
		String txt2img_path = "res\\txt2img.png";
		File txt2img_file = new File(txt2img_path);
		ImageIO.write(image, "png", txt2img_file);
		String[] fileNameList = {design_image_name, base_image_name, txt2img_path};
		new PrintImage().drawImage(fileNameList, 1);
	}

	public BufferedImage convertTxtToGraphic(File file, String text_file_name, Font font) throws java.io.IOException{
		StringBuilder s = new StringBuilder();
		BufferedReader r = null;
		r = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf8"));

		BufferedImage img = new BufferedImage(1100, 1550, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = img.createGraphics();

		g2d.setFont(font);
		g2d.dispose();

		g2d = img.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
		g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		g2d.setFont(font);
		g2d.setColor(Color.BLACK);
		int nextLinePosition = 65;
		int fontSize = font.getSize();
		String line = null;
		g2d.drawString("病历：" + text_file_name, 20, 25);
		int line_length = 45;
		while ((line = r.readLine()) != null) {
			int total_length = line.length();
			int start_pos = 0;
			while (total_length >= line_length) {
				String curString = line.substring(start_pos, start_pos+line_length-1);
				g2d.drawString(curString, 20, nextLinePosition);
				nextLinePosition = nextLinePosition + fontSize + 1;
				total_length -= 30;
				start_pos += 30;
			}
			if (line.substring(start_pos).length() > 0) {
				g2d.drawString(line.substring(start_pos), 20, nextLinePosition);
				nextLinePosition = nextLinePosition + fontSize + 1;
			}
		}
		g2d.dispose();
		return img;
	}


	public static void main(String[] args) {

	}
}
