package ProjectA;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SingleThread {

    private static int[][] sourceGray;
    private static int[][] templateGray;
    private static int width;
    private static int height;

    public static void main(String[] args) {
        try {
            String templatePath = "C:\\Users\\msakr\\Desktop\\Template.jpg";
            String sourcePath = "C:\\Users\\msakr\\Desktop\\TenCardG.jpg";
            String resultPath = "C:\\Users\\msakr\\Desktop\\SingleThread.jpg";

            BufferedImage templateImage = readImage(templatePath);
            BufferedImage sourceImage = readImage(sourcePath);

            long startTime = System.currentTimeMillis();
            BufferedImage resultImage = performTemplateMatching(templateImage, sourceImage);
            long endTime = System.currentTimeMillis();

            saveImage(resultImage, resultPath);

            System.out.println("Single Thread execution time: " + (endTime - startTime) + " milliseconds");

        } catch (IOException e) {
            handleImageReadError(e);
        }
    }

    private static BufferedImage readImage(String path) throws IOException {
        return ImageIO.read(new File(path));
    }

    private static BufferedImage performTemplateMatching(BufferedImage templateImage, BufferedImage sourceImage) {
        sourceGray = convertToGrayscale(sourceImage);
        templateGray = convertToGrayscale(templateImage);

        int sourceHeight = sourceGray.length;
        int sourceWidth = sourceGray[0].length;
        int templateHeight = templateGray.length;
        int templateWidth = templateGray[0].length;

        double minDiff = Double.MAX_VALUE;
        double[][] diffMatrix = new double[sourceHeight - templateHeight + 1][sourceWidth - templateWidth + 1];

        Graphics2D graphics = sourceImage.createGraphics();
        graphics.setColor(Color.ORANGE);

        for (int i = 0; i <= sourceHeight - templateHeight; i++) {
            for (int j = 0; j <= sourceWidth - templateWidth; j++) {
                double diff = calculateDifference(i, j, templateHeight, templateWidth);
                diffMatrix[i][j] = diff;
                if (diff < minDiff) {
                    minDiff = diff;
                }
            }
        }

        double threshold = 10 * minDiff;

        for (int i = 0; i <= sourceHeight - templateHeight; i++) {
            for (int j = 0; j <= sourceWidth - templateWidth; j++) {
                if (diffMatrix[i][j] <= threshold) {
                    graphics.drawRect(j, i, templateWidth, templateHeight);
                }
            }
        }

        graphics.dispose();
        return sourceImage;
    }

    private static int[][] convertToGrayscale(BufferedImage image) {
        width = image.getWidth();
        height = image.getHeight();
        int[][] grayImage = new int[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = (rgb) & 0xFF;
                grayImage[y][x] = (int) (0.299 * r + 0.587 * g + 0.114 * b);
            }
        }

        return grayImage;
    }

    private static double calculateDifference(int startX, int startY, int templateHeight, int templateWidth) {
        double sum = 0;

        for (int i = 0; i < templateHeight; i++) {
            for (int j = 0; j < templateWidth; j++) {
                sum += Math.abs(sourceGray[startX + i][startY + j] - templateGray[i][j]);
            }
        }

        return sum / (templateHeight * templateWidth);
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
