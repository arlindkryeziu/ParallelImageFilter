import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.IIOException;
import java.io.PrintWriter;
import java.util.concurrent.*;
import java.util.Random;

import javax.imageio.ImageIO;

public class TestImageFilter {

	public static void main(String[] args) throws Exception {

		BufferedImage image = null;
		String srcFileName = null;
		String outFileName = null;

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

		outFileName = srcFileName.equals("IMAGE1.JPG") ? "out1.txt" : "out2.txt";

		PrintWriter out = new PrintWriter(outFileName);
		out.print("");

		System.out.println("Source image: " + srcFileName);
		out.println("Source image: " + srcFileName);

		int w = image.getWidth();
		int h = image.getHeight();

		System.out.println("Image size is " + w + "x" + h);
		System.out.println();

		out.println("Image size is " + w + "x" + h);
		out.println();

		int[] src = image.getRGB(0, 0, w, h, null, 0, w);
		int[] dst = new int[src.length];

		System.out.println("Starting sequential image filter.");
		out.println("Starting sequential image filter.");

		long startTime = System.currentTimeMillis();
		ImageFilter filter0 = new ImageFilter(src, dst, w, h);
		filter0.apply();
		long endTime = System.currentTimeMillis();

		long tSequential = endTime - startTime;
		System.out.println("Sequential image filter took " + tSequential + " milliseconds.");
		out.println("Sequential image filter took " + tSequential + " milliseconds.");

		BufferedImage dstImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		dstImage.setRGB(0, 0, w, h, dst, 0, w);

		String dstName = "Filtered" + srcFileName;
		File dstFile = new File(dstName);
		ImageIO.write(dstImage, "jpg", dstFile);

		System.out.println("Output image: " + dstName);
		out.println("Output image: " + dstName);

        System.out.println("\nAvailable processors: " + Runtime.getRuntime().availableProcessors());
        out.println();
		out.println("Available processors: " + Runtime.getRuntime().availableProcessors());

		for (int nthreads = 1; nthreads <= 16; nthreads = nthreads * 2)
		{
			System.out.println("\nStarting parallel image filter using " + nthreads + " threads.");
			out.println();
			out.println("Starting parallel image filter using " + nthreads + " threads.");

			int[] srcP = image.getRGB(0, 0, w, h, null, 0, w);
			int[] dstP = new int[srcP.length];
			int threshold = (h / nthreads) + 2;

            startTime = System.currentTimeMillis();
            for (int NRSTEPS = 0; NRSTEPS < 100; NRSTEPS++)
            {
                ParallelFJImageFilter filter1 = new ParallelFJImageFilter(srcP, dstP, threshold, w, 1, h - 1);
                ForkJoinPool pool = new ForkJoinPool(nthreads);
                pool.invoke(filter1);

				int[] help; help = srcP; srcP = dstP; dstP = help;
            }
			endTime = System.currentTimeMillis();

			long tParallel = endTime - startTime;

			BufferedImage dstPImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
			dstPImage.setRGB(0, 0, w, h, dstP, 0, w);

			boolean isSame = CompareImages(dstImage, dstPImage, w, h);

			System.out.println("Parallel image filter took " + tParallel + " milliseconds using " + nthreads + " threads.");
			System.out.println("Output image verified " + (isSame ? "successfully" : "unsuccessfully!"));
			System.out.println("Speedup: " + SpeedUp(tSequential, tParallel, nthreads));

			out.println("Parallel image filter took " + tParallel + " milliseconds using " + nthreads + " threads.");
			out.println("Output image verified " + (isSame ? "successfully" : "unsuccessfully!"));
			out.println("Speedup: " + SpeedUp(tSequential, tParallel, nthreads));

			if (nthreads == 16)
			{
				String dstPName = "ParallelFiltered" + srcFileName;
				File dstPFile = new File(dstPName);
				ImageIO.write(dstPImage, "jpg", dstPFile);

				System.out.println("\nOutput image (parallel filter): " + dstPName);
				out.println();
				out.println("Output image (parallel filter): " + dstPName);
			}
		}

		out.close();
	}

	public static boolean CompareImages(BufferedImage img1, BufferedImage img2, int w, int h)
    {
        for (int x = 0; x < w; x++)
        {
            for (int y = 0; y < h; y++)
            {
                if (img1.getRGB(x, y) != img2.getRGB(x, y))
                {
                    return false;
                }
            }
        }
        return true;
    }

    public static String SpeedUp(long tSequential, long tParallel, int threadNumber)
	{
		String response = "";
		double speedUp = (double)tSequential / (double)tParallel;
		double threadSpeedUp = (0.7 * threadNumber);

		if (tParallel <= ((double)tSequential / threadSpeedUp))
		{
			response = speedUp + " ok (>= " + threadSpeedUp + ")";
		}
		else
		{
			response = String.valueOf(speedUp);
		}

		return response;
	}
}
