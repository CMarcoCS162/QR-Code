import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

public class Backend {
	
	// Generate png file for QR code
	public static void toPNG(String filename, byte[][] image) {		
		int size = 10*image.length;

	    BufferedImage bi = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

	    Graphics2D g2d = bi.createGraphics();

	    for(int i=0; i<image.length; i++) {
	    	for(int j=0; j<image.length; j++) {
	    		g2d.setPaint(image[i][j] == 0 ? Color.white: Color.black);
	    		g2d.fillRect(i*10, j*10, 10, 10);
	    	}
	    }
	    	

	    try {
			ImageIO.write(bi, "PNG", new File(filename));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// Generate ASCII file for QR code
	public static void toAscii(String filename, byte[][] image) {
		String result = "";

		for(int j=0; j<image.length; j++) {
			for(int i=0; i<image.length; i++) {
	    		result += image[i][j] == 0 ? " ": Character.toString((char)219);
	    	}
			result += "\n";
	    }
		

		try {
			Files.write(Paths.get("file1.bin"), result.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
