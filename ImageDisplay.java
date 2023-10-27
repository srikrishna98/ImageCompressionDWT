
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;


public class ImageDisplay {

	JFrame frame;
	JLabel lbIm1;
	BufferedImage imgOne;
	int width = 512; // default image width and height
	int height = 512;

	public ImageDisplay() {
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		lbIm1 = new JLabel();
		lbIm1.setHorizontalAlignment(JLabel.CENTER);
		lbIm1.setVerticalAlignment(JLabel.CENTER);

		frame.add(lbIm1);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setVisible(true);
	}

	/** Read Image RGB
	 *  Reads the image of given width and height at the given imgPath into the provided BufferedImage.
	 */
	private int[][][] readImageRGB(int width, int height, String imgPath) {
		int[][][] imageRGB = new int[width][height][3];
		try
		{
			int frameLength = width*height*3;

			File file = new File(imgPath);
			RandomAccessFile raf = new RandomAccessFile(file, "r");
			raf.seek(0);

			long len = frameLength;
			byte[] bytes = new byte[(int) len];

			raf.read(bytes);

			int ind = 0;
			for(int y = 0; y < height; y++)
			{
				for(int x = 0; x < width; x++)
				{
					byte a = 0;
					byte r = bytes[ind];
					byte g = bytes[ind+height*width];
					byte b = bytes[ind+height*width*2];

					imageRGB[x][y][0] = (r & 0xff);
					imageRGB[x][y][1] = (g & 0xff);
					imageRGB[x][y][2] = (b & 0xff);
					ind++;
				}
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return imageRGB;
	}

	public void displayImage(int [][][] imageRGB, String title){
		int width = imageRGB.length;
		int height = imageRGB[0].length;
		imgOne = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int pix = 0xff000000 | ((imageRGB[x][y][0]) << 16) | ((imageRGB[x][y][1]) << 8) | (imageRGB[x][y][2]);
				imgOne.setRGB(x, y, pix);
			}
		}

		lbIm1.setIcon(new ImageIcon(imgOne));
		frame.setTitle(title);
		frame.repaint();
	}

	public int[][][] waveletProcess(int[][][] img, int rBound, int cBound){
		int [][][] waveletImg = new int[width][height][3];
		int [][][] tempImg = new int[width][height][3];
//		Copy img to waveletImg
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				waveletImg[i][j][0] = img[i][j][0];
				waveletImg[i][j][1] = img[i][j][1];
				waveletImg[i][j][2] = img[i][j][2];
			}
		}

		for(int i = 0; i < 3; i++) {
			for(int x = 0; x < rBound/2; x++) {
				for(int y = 0; y < cBound; y++) {
//					Low pass averaging of adjacent cells on the same row
					tempImg[x][y][i] = (img[2*x][y][i] + img[2*x+1][y][i])/2;
					tempImg[rBound/2 + x][y][i] = (img[2*x][y][i] - img[2*x+1][y][i])/2;
				}
			}
		}

		for(int i = 0; i < 3; i++) {
			for(int x = 0; x < rBound; x++) {
				for(int y = 0; y < cBound/2; y++) {

//					Low pass averaging of adjacent cells on the same row
					waveletImg[x][y][i] = (tempImg[x][2*y][i] + tempImg[x][2*y+1][i])/2;
					waveletImg[x][cBound/2 + y][i] = (tempImg[x][2*y][i] - tempImg[x][2*y+1][i])/2;
				}
			}
		}
		return waveletImg;
	}

	public int[][][] compressImage(int[][][] ogImage, int n) {
		int[][][] imageRGB = new int[width][height][3];
		int rBound, cBound;
//		Copy ogImage to imageRGB
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				imageRGB[i][j][0] = ogImage[i][j][0];
				imageRGB[i][j][1] = ogImage[i][j][1];
				imageRGB[i][j][2] = ogImage[i][j][2];
			}
		}
		for(int i = 9; i>n; i--) {
			rBound = (int) Math.pow(2, i);
			cBound = (int) Math.pow(2, i);
			imageRGB = waveletProcess(imageRGB, rBound, cBound);
		}

		return imageRGB;
	}

	public int[][][] prepareDecode(int [][][]compressedImage, int n){
		// Copy 2^n x 2^n from compressedImage to tempImg
		//		Make all other pixels zero
		int [][][] tempImg = new int[width][height][3];
		int bound = (int) Math.pow(2, n);
		for(int i = 0;i<3;i++){
			for(int x = 0; x<bound; x++){
				for(int y = 0; y<bound; y++){
					tempImg[x][y][i] = compressedImage[x][y][i];
				}
			}
		}
		return tempImg;
	}

	public void decodeImage(int[][][] compressedImage, int n){

		for(int k = n; k <= 9; k++){
			int currBound = (int) Math.pow(2, k);
			int [][][]temp = new int[currBound][currBound][3];

			for(int i = 0 ; i<3; i++){
				for(int x = 0; x < currBound; x++){
					for(int y = 0; y < currBound/2; y++) {
						temp[x][2*y][i] = (compressedImage[x][y][i] + compressedImage[x][currBound/2 + y][i]);
						temp[x][2*y+1][i] = (compressedImage[x][y][i] - compressedImage[x][currBound/2 + y][i]);
					}
				}
			}
			for(int i = 0 ; i<3; i++){
				for(int y = 0; y < currBound; y++) {
					for(int x = 0; x < currBound/2; x++){
						compressedImage[2*x][y][i] = (temp[x][y][i] + temp[currBound/2 + x][y][i]);
						compressedImage[2*x + 1][y][i] = (temp[x][y][i] - temp[currBound/2 + x][y][i]);
					}
				}
			}
		}
	}

	public void processImage(String[] args){

		// Read a parameter from command line
		String param1 = args[1];
		int num = Integer.parseInt(param1);

		// Read in the specified image
		imgOne = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		frame = new JFrame();

		int [][][] inputImageRGB;

		inputImageRGB = readImageRGB(width, height, args[0]);
		if(num>0) {
			inputImageRGB = compressImage(inputImageRGB, num);
			if (num < 9) {
				inputImageRGB = prepareDecode(inputImageRGB, num + 1);
				decodeImage(inputImageRGB, num + 1);
			}
			// Display the image
			displayImage(inputImageRGB, "Decompressed");
		}else{

			int [][][] temp;

			for(int i = 0; i < 9; i++) {
				temp = compressImage(inputImageRGB, i);
				temp = prepareDecode(temp, i + 1);
				decodeImage(temp, i + 1);
				displayImage(temp, "Decompressed"+i);
			}
			displayImage(inputImageRGB, "Decompressed" + 9);
		}
	}

	public static void main(String[] args) {
		ImageDisplay ren = new ImageDisplay();
		ren.processImage(args);

	}

}
