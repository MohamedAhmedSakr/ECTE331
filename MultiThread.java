package ProjectA;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MultiThread {

    private static int width;
    private static int height;
    private static int[][] sourceGray;
    private static int[][] templateGray;

    public static void main(String[] args) {
        try {
            String templatePath = "C:\\Users\\msakr\\Desktop\\Template.jpg";
            String sourcePath = "C:\\Users\\msakr\\Desktop\\TenCardG.jpg";
            
            // Specify the number of threads to use
            int numberOfThreads = 10;

            BufferedImage templateImage = readImage(templatePath);
            BufferedImage sourceImage = readImage(sourcePath);

            measureExecutionTimeMultiThread(templateImage, sourceImage, numberOfThreads);

        } catch (IOException e) {
            handleImageReadError(e);
        }
    }

    public static void measureExecutionTimeMultiThread(BufferedImage templateImage, BufferedImage sourceImage, int numberOfThreads) {
        long startTime = System.currentTimeMillis();
        try {
            BufferedImage resultImage = performMultiThreadedTemplateMatching(templateImage, sourceImage, numberOfThreads);
            saveImage(resultImage, "C:\\Users\\msakr\\Desktop\\MultiThread.jpg");
        } catch (IOException e) {
            System.err.println("Error during template matching (multi-threaded).");
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();

        System.out.println("MultiThread execution time: " + (endTime - startTime) + " milliseconds");
    }

    public static BufferedImage performMultiThreadedTemplateMatching(BufferedImage templateImage, BufferedImage sourceImage, int numberOfThreads) throws IOException {
        sourceGray = convertToGrayscale(sourceImage);
        templateGray = convertToGrayscale(templateImage);

        int r1 = sourceGray.length;
        int c1 = sourceGray[0].length;
        int r2 = templateGray.length;
        int c2 = templateGray[0].length;
        int tempSize = r2 * c2;
        double ratio = 10; // Calibration ratio

        double[][] absDiffMat = new double[r1 - r2 + 1][c1 - c2 + 1];
        Graphics2D g2d = sourceImage.createGraphics();
        g2d.setColor(Color.BLACK);

        // Use an array to hold the minimum value to allow updates within the lambda expression
        double[] minimum = {Double.MAX_VALUE};

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        for (int i = 0; i <= r1 - r2; i++) {
            int finalI = i;
            executor.submit(() -> {
                for (int j = 0; j <= c1 - c2; j++) {
                    double absDiff = calculateAbsDiff(sourceGray, templateGray, finalI, j, r2, c2) / tempSize;
                    absDiffMat[finalI][j] = absDiff;

                    synchronized (minimum) {
                        if (absDiff < minimum[0]) {
                            minimum[0] = absDiff;
                        }
                    }
                }
            });
        }
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        double threshold = ratio * minimum[0];

        for (int i = 0; i <= r1 - r2; i++) {
            for (int j = 0; j <= c1 - c2; j++) {
                if (absDiffMat[i][j] <= threshold) {
                    g2d.drawRect(j, i, c2, r2);
                }
            }
        }

        g2d.dispose();
        return sourceImage;
    }

    private static double calculateAbsDiff(int[][] sourceImage, int[][] templateImage, int startX, int startY, int height, int width) {
        double sum = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                sum += Math.abs(sourceImage[startX + i][startY + j] - templateImage[i][j]);
            }
        }
        return sum;
    }

    public static int[][] convertToGrayscale(BufferedImage image) {
        width = image.getWidth();
        height = image.getHeight();

        int[][] grayImage = new int[height][width];
        byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

        int coord, pr, pg, pb;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                coord = 3 * (i * width + j);
                pr = pixels[coord] & 0xff;
                pg = pixels[coord + 1] & 0xff;
                pb = pixels[coord + 2] & 0xff;
                grayImage[i][j] = (int) Math.round(0.299 * pr + 0.587 * pg + 0.114 * pb);
            }
        }
        return grayImage;
    }

    private static BufferedImage readImage(String path) throws IOException {
        return ImageIO.read(new File(path));
    }

    private static void saveImage(BufferedImage img, String path) throws IOException {
        File outputfile = new File(path);
        ImageIO.write(img, "jpg", outputfile);
        System.out.println("Result image saved to: " + path);
    }

    private static void handleImageReadError(IOException e) {
        System.err.println("Error reading the image files. Please check the paths.");
        e.printStackTrace();
    }
}
