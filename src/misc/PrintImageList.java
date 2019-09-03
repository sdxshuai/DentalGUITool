package misc;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

public class PrintImageList {
    public void drawImage(String fileNameList[], int count) {
        try {
            DocFlavor dof = DocFlavor.INPUT_STREAM.PNG;
            PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();

            PrintService printService[] = PrintServiceLookup.lookupPrintServices(dof, pras);
            PrintService defaultPrintService = PrintServiceLookup.lookupDefaultPrintService();
            PrintService ps = ServiceUI.printDialog(
                    null, 200, 200, printService,
                    defaultPrintService, dof, pras);

            pras.add(OrientationRequested.LANDSCAPE);
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

    public void mergeImage(String[] files, int type, String targetFile) {
        int len = files.length;
        if (len < 1) {
            throw new RuntimeException("图片数量小于1");
        }
        File[] src = new File[len];
        BufferedImage[] images = new BufferedImage[len];
        int[][] ImageArrays = new int[len][];
        for (int i = 0; i < len; i++) {
            try {
                src[i] = new File(files[i]);
                images[i] = ImageIO.read(src[i]);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            int width = images[i].getWidth();
            int height = images[i].getHeight();
            ImageArrays[i] = new int[width * height];
            ImageArrays[i] = images[i].getRGB(0, 0, width, height, ImageArrays[i], 0, width);
        }
        int newHeight = 0;
        int newWidth = 0;
        for (int i = 0; i < images.length; i++) {
            // 横向
            if (type == 1) {
                newHeight = newHeight > images[i].getHeight() ? newHeight : images[i].getHeight();
                newWidth += images[i].getWidth();
            } else if (type == 2) {// 纵向
                newWidth = newWidth > images[i].getWidth() ? newWidth : images[i].getWidth();
                newHeight += images[i].getHeight();
            }
        }
        if (type == 1 && newWidth < 1) {
            return;
        }
        if (type == 2 && newHeight < 1) {
            return;
        }
        // 生成新图片
        try {
            BufferedImage ImageNew = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            int height_i = 0;
            int width_i = 0;
            for (int i = 0; i < images.length; i++) {
                if (type == 1) {
                    ImageNew.setRGB(width_i, 0, images[i].getWidth(), newHeight, ImageArrays[i], 0,
                            images[i].getWidth());
                    width_i += images[i].getWidth();
                } else if (type == 2) {
                    ImageNew.setRGB(0, height_i, newWidth, images[i].getHeight(), ImageArrays[i], 0, newWidth);
                    height_i += images[i].getHeight();
                }
            }
            //输出想要的图片
            ImageIO.write(ImageNew, targetFile.split("\\.")[1], new File(targetFile));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) {
        String picture_dir = "out//picture//";
        String base_image_name = "res//base_print.png";

        File picture_dir_file = new File(picture_dir);
        File[] pictures_file_list = picture_dir_file.listFiles();
        Map<String, ArrayList<String>> case_pictures_map = new HashMap<>();

        for (File picture_file : pictures_file_list) {
            String picture_file_name = picture_dir + picture_file.getName();
            if (picture_file_name.indexOf("print") == -1) {
                continue;
            }
            int key_end_index = picture_file_name.indexOf("_RPD");
            if (key_end_index == -1) {
                continue;
            }

            String key_str = picture_file_name.substring(0, key_end_index);
            if (case_pictures_map.containsKey(key_str)) {
                case_pictures_map.get(key_str).add(picture_file_name);
            } else {
                ArrayList<String> cur_picture_names = new ArrayList<>();
                cur_picture_names.add(picture_file_name);
                case_pictures_map.put(key_str, cur_picture_names);
            }
        }


        PrintImageList test = new PrintImageList();
        for (Map.Entry<String, ArrayList<String>> entry : case_pictures_map.entrySet()) {
            ArrayList<String> input_picture_names = entry.getValue();
            input_picture_names.add(0, base_image_name);
            String[] array = new String[input_picture_names.size()];
            String[] input_picture_names_array = input_picture_names.toArray(array);
            String targetFile = entry.getKey() + ".png";
            test.mergeImage(input_picture_names_array, 1, targetFile);
//            test.drawImage(new String[]{targetFile}, 1);
        }
    }
}
