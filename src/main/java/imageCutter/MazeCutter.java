package imageCutter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MazeCutter {
    int xCount = 52;
    int yCount = 29;
    File originalImage = new File("./files/originalFile/maze.jpg");
    File cutMazeImage = new File("./files/processedImages/cutMaze.jpg");
    File binarizedImage = new File("./files/processedImages/binMaze.jpg");
    File testImage = new File("./files/processedImages/testImage.jpg");
    File resultImage = new File("./files/processedImages/resultImage.jpg");
    File cellDir = new File("./files/processedImages/cells");
    File resultTsv = new File("./files/maze.tsv");
    boolean cellInfo[][][] = new boolean[yCount][xCount][4];

    public MazeCutter() {
        if(!cellDir.exists()){
            cellDir.mkdirs();
        }
        if(!resultImage.getParentFile().exists()){
            resultImage.getParentFile().mkdirs();
        }
        System.out.println("Cutting valid area from the image.");
        cutMazeFromPicture();
        System.out.println("Doing binarization.");
        binarization();
        System.out.println("Processing cells.");
        cutCells();
        System.out.println("Ensuring the correctness of identification results.");
        correctWrongValue();
        System.out.println("Saving structured data to file.");
        writeValue();
        System.out.println("Drawing back the image for future check(if needed).");
        drawAgain();
        System.out.println("Image processed!");
    }

    public boolean[][][] getCellInfo() {
        return cellInfo;
    }

    private void cutMazeFromPicture() {
        try {
            BufferedImage bufferedImage = ImageIO.read(originalImage);
            BufferedImage subimage = bufferedImage.getSubimage(1, 1, 1498, 842);
            ImageIO.write(subimage, "jpg", cutMazeImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void binarization() {
        try {
            BufferedImage bufferedImage = ImageIO.read(cutMazeImage);
            int height = bufferedImage.getHeight();
            int width = bufferedImage.getWidth();
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    int rgb = bufferedImage.getRGB(j, i);
                    int r = (rgb & 0x00ff0000) >> 16;
                    int g = (rgb & 0x0000ff00) >> 8;
                    int b = (rgb & 0x000000ff);
                    int avg = (r + g + b) / 3;
                    if (avg >= 200) {
                        bufferedImage.setRGB(j, i, 0xffffff);
                    } else {
                        bufferedImage.setRGB(j, i, 0);
                    }
                }
            }
            ImageIO.write(bufferedImage, "jpg", binarizedImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cutCells() {
        try {
            BufferedImage bufferedImage = ImageIO.read(binarizedImage);
            double lineWidth = 4;
            int height = bufferedImage.getHeight();
            int width = bufferedImage.getWidth();
            double cellHeight = ((double) height) / yCount;
            double cellWidth = ((double) width) / xCount;
            for (int j = 0; j < yCount; j++) {
                for (int i = 0; i < xCount; i++) {
                    int startingX = (int) (cellWidth * i - lineWidth / 2);
                    int startingY = (int) (cellHeight * j - lineWidth / 2);
                    int endingX = (int) (cellWidth * (i + 1) + lineWidth / 2);
                    int endingY = (int) (cellHeight * (j + 1) + lineWidth / 2);
                    startingX = Math.max(startingX, 0);
                    startingY = Math.max(startingY, 0);
                    endingX = Math.min(endingX, width);
                    endingY = Math.min(endingY, height);
                    BufferedImage cellImage = bufferedImage.getSubimage(startingX, startingY, endingX - startingX, endingY - startingY);
                    ImageIO.write(cellImage, "jpg", new File(cellDir, j + "-" + i + ".jpg"));


                    int cellActualWidth = cellImage.getWidth();
                    int cellActualHeight = cellImage.getHeight();
                    int threshold = 150;
                    //top
                    long topVoteResult = 0;
                    for (int x = 0; x < cellActualWidth; x++) {
                        topVoteResult += (cellImage.getRGB(x, 0) & 0x000000ff);
                        topVoteResult += (cellImage.getRGB(x, 1) & 0x000000ff);
                    }
                    topVoteResult /= cellActualWidth * 2;
                    boolean topToGo = topVoteResult > threshold;
                    //right
                    long rightVoteResult = 0;
                    for (int y = 0; y < cellActualHeight; y++) {
                        rightVoteResult += (cellImage.getRGB(cellActualWidth - 1, y) & 0x000000ff);
                        rightVoteResult += (cellImage.getRGB(cellActualWidth - 2, y) & 0x000000ff);
                    }
                    rightVoteResult /= cellActualHeight * 2;
                    boolean rightToGo = rightVoteResult > threshold;
                    //bottom
                    long bottomVoteResult = 0;
                    for (int x = 0; x < cellActualWidth; x++) {
                        bottomVoteResult += (cellImage.getRGB(x, cellActualHeight - 1) & 0x000000ff);
                        bottomVoteResult += (cellImage.getRGB(x, cellActualHeight - 2) & 0x000000ff);
                    }
                    bottomVoteResult /= cellActualWidth * 2;
                    boolean bottomToGo = bottomVoteResult > threshold;
                    //left
                    long leftVoteResult = 0;
                    for (int y = 0; y < cellActualHeight; y++) {
                        leftVoteResult += (cellImage.getRGB(0, y) & 0x000000ff);
                        leftVoteResult += (cellImage.getRGB(1, y) & 0x000000ff);
                    }
                    leftVoteResult /= cellActualHeight * 2;
                    boolean leftToGo = leftVoteResult > threshold;
                    cellInfo[j][i] = new boolean[]{topToGo, rightToGo, bottomToGo, leftToGo};
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void drawAgain() {
        try {
            int lineWidth = 4;
            int cellWidth = 30;
            int cellHeight = 30;
            int width = lineWidth * (xCount + 1) + cellWidth * xCount;
            int height = lineWidth * (yCount + 1) + cellHeight * yCount;
            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics g = bufferedImage.getGraphics();
            g.setColor(new Color(0xffffff));
            g.fillRect(0, 0, width, height);
            g.setColor(new Color(0x000000));
            for (int i = 0; i < yCount; i++) {
                for (int j = 0; j < xCount; j++) {
                    int cellInnerX = j * cellWidth + (j + 1) * lineWidth;
                    int cellInnerY = i * cellHeight + (i + 1) * lineWidth;
                    if (!cellInfo[i][j][0]) {
                        g.fillRect(cellInnerX - lineWidth, cellInnerY - lineWidth, cellWidth + 2 * lineWidth, lineWidth);
                    }
                    if (!cellInfo[i][j][1]) {
                        g.fillRect(cellInnerX + cellWidth, cellInnerY - lineWidth, lineWidth, cellHeight + 2 * lineWidth);
                    }
                    if (!cellInfo[i][j][2]) {
                        g.fillRect(cellInnerX - lineWidth, cellInnerY + cellHeight, cellWidth + 2 * lineWidth, lineWidth);
                    }
                    if (!cellInfo[i][j][3]) {
                        g.fillRect(cellInnerX - lineWidth, cellInnerY - lineWidth, lineWidth, cellHeight + 2 * lineWidth);
                    }
                }
            }
            ImageIO.write(bufferedImage, "jpg", testImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void correctWrongValue() {
        for (int i = 0; i < xCount; i++) {
            for (int j = 0; j < yCount; j++) {
//                if (i == 0) {
//                    cellInfo[j][i][3] = false;
//                }
//                if (i == xCount - 1) {
//                    cellInfo[j][i][1] = false;
//                }
//                if (j == 0) {
//                    cellInfo[j][i][0] = false;
//                }
//                if (j == yCount - 1) {
//                    cellInfo[j][i][2] = false;
//                }
                if (i > 0) {
                    boolean res = cellInfo[j][i][3] & cellInfo[j][i - 1][1];
                    cellInfo[j][i][3] = res;
                    cellInfo[j][i - 1][1] = res;
                }
                if (j > 0) {
                    boolean res = cellInfo[j][i][0] & cellInfo[j - 1][i][2];
                    cellInfo[j][i][0] = res;
                    cellInfo[j - 1][i][2] = res;
                }
            }
        }
    }

    private void writeValue() {
        try {
            FileWriter fileWriter = new FileWriter(resultTsv);
            fileWriter.write("row\tcol\ttop\tright\tbottom\tleft\n");
            for (int y = 0; y < yCount; y++) {
                for (int x = 0; x < xCount; x++) {
                    fileWriter.write(y + "\t" + x);
                    for (int i = 0; i < cellInfo[y][x].length; i++) {
                        boolean canGo = cellInfo[y][x][i];
                        fileWriter.write("\t" + (canGo ? 1 : 0));
                    }
                    fileWriter.write("\n");
                }
            }
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
