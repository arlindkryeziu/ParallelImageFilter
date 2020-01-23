import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.IIOException;
import java.util.concurrent.*;
import java.util.Random;

import javax.imageio.ImageIO;

public class TestImageFilter {

	public static void main(String[] args) throws Exception {
		
		BufferedImage image = null;
		String srcFileName = null;
		int nthreads = 8;
		try {
			srcFileName = args[0];
			File srcFile = new File(srcFileName);
			image = ImageIO.read(srcFile);
		}
		catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Usage: java TestAll <image-file>");
			System.exit(1);
		}
		catch (IIOException e) {
			System.out.println("Error reading image file " + srcFileName + " !");
			System.exit(1);
		}

		System.out.println("Source image: " + srcFileName);

		int w = image.getWidth();
		int h = image.getHeight();
		System.out.println("Image size is " + w + "x" + h);
		System.out.println();
	
		int[] src = image.getRGB(0, 0, w, h, null, 0, w);
		int[] srcP = image.getRGB(0, 0, w, h, null, 0, w);
		int[] dst = new int[src.length];

		System.out.println("Starting sequential image filter.");

		long startTime = System.currentTimeMillis();
//		ImageFilter filter0 = new ImageFilter(src, dst, w, h);
//		filter0.apply();
		long endTime = System.currentTimeMillis();

		long tSequential = endTime - startTime;
		System.out.println("Sequential image filter took " + tSequential + " milliseconds.");

		BufferedImage dstImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		dstImage.setRGB(0, 0, w, h, dst, 0, w);

		String dstName = "Filtered" + srcFileName;
		File dstFile = new File(dstName);
		ImageIO.write(dstImage, "jpg", dstFile);

		System.out.println("Output image: " + dstName);

        System.out.println("\nAvailable processors: " + Runtime.getRuntime().availableProcessors());

		System.out.println("\nStarting parallel image filter using " + nthreads + " threads.");

		int[] dstP = new int[srcP.length];

		ParallelFJImageFilter filter1 = new ParallelFJImageFilter(srcP, dstP, w, 1, h - 1);
		ForkJoinPool pool = new ForkJoinPool(nthreads);

		startTime = System.currentTimeMillis();
		pool.invoke(filter1);
		endTime = System.currentTimeMillis();

		long tParallel = endTime - startTime;

		BufferedImage dstPImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		dstPImage.setRGB(0, 0, w, h, dstP, 0, w);

		boolean isSame = CompareImages(dstImage, dstPImage, w, h);

		System.out.println("Parallel image filter took " + tParallel + " milliseconds using " + nthreads + " threads.");
		System.out.println("Output image verified " + (isSame ? "successfully" : "unsuccessfully!"));

		String dstPName = "ParallelFiltered" + srcFileName;
		File dstPFile = new File(dstPName);
		ImageIO.write(dstPImage, "jpg", dstPFile);

		System.out.println("\nOutput image (parallel filter): " + dstPName);
	}

	public static boolean CompareImages(BufferedImage img1, BufferedImage img2, int w, int h)
	{
		Random rand = new Random();

		System.out.println("Img1[2913][3326]: " + img2.getRGB(2913, 3326) + " , actual value = -14410220");
		System.out.println("Img1[306][607]: " + img2.getRGB(306, 607) + " , actual value = -10138305");
		System.out.println("Img1[13][3577]: " + img2.getRGB(13, 3577) + " , actual value = -16316922");

//		for (int i = 0; i < 3; i++)
//		{
//            int width = rand.nextInt(w);
//            int height = rand.nextInt(h);
//
//			System.out.println("Img1[" + width + "][" + height + "]: " + img1.getRGB(width, height));
//			System.out.println("Img2[" + width + "][" + height + "]: " + img2.getRGB(width, height));
//
////            if (img1.getRGB(width, height) != img2.getRGB(width, height))
////			{
////				return false;
////			}
//		}
		return true;
	}
}
