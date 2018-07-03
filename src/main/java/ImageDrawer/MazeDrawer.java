package ImageDrawer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class MazeDrawer {
    boolean[][][] cellInfo;
    int xCount;
    int yCount;
    File resultImage = new File("./files/drawnImages/resultImage.jpg");

    public MazeDrawer(boolean[][][] cellInfo){
        if(!resultImage.getParentFile().exists()){
            resultImage.getParentFile().mkdirs();
        }
        this.cellInfo = cellInfo;
        this.yCount = cellInfo.length;
        this.xCount = cellInfo[0].length;
    }

    public void drawMovesOnNewMaze(int[] start, List<int[]> moves){
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
            g.setColor(new Color(0x6485FF));
            int startCellInnerX = start[0] * cellWidth + (start[0] + 1) * lineWidth;
            int startCellInnerY = start[1] * cellHeight + (start[1] + 1) * lineWidth;
            g.fillRect(startCellInnerX  , startCellInnerY, cellWidth, cellHeight);
            g.setColor(new Color(0xA9FFA1));
            for(int[] move:moves){
                int cellInnerX = move[0] * cellWidth + (move[0] + 1) * lineWidth;
                int cellInnerY = move[1] * cellHeight + (move[1] + 1) * lineWidth;
                g.fillRect(cellInnerX  , cellInnerY, cellWidth, cellHeight);
            }
            ImageIO.write(bufferedImage, "jpg", resultImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
