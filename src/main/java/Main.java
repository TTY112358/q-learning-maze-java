import ImageCutter.MazeCutter;
import ImageDrawer.MazeDrawer;
import QLearningLearner.MazeProblemLearner;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String args[]) {
        boolean coldStart = true;
        boolean[][][] cellInfo;
        if (coldStart) {
            MazeCutter mazeCutter = new MazeCutter();
            System.out.println("cut complete");
            cellInfo = mazeCutter.getCellInfo();
        }else {
            cellInfo = readFromTSV();
        }
        MazeProblemLearner learner = new MazeProblemLearner(cellInfo);
        MazeDrawer drawer = new MazeDrawer(cellInfo);
        learner.train(5000000);
        learner.writeValues();
        while (true) {
            Scanner sc = new Scanner(System.in);
            int wIndex = sc.nextInt();
            int hIndex = sc.nextInt();
            List<int[]> moves = learner.playTo(new int[]{wIndex, hIndex});
//            for(int[] move: moves){
//                System.out.println(move[0] + "," + move[1]);
//            }
            if (moves == null) {
                System.out.println("no solution");
                continue;
            }
            drawer.drawMovesOnNewMaze(new int[]{wIndex, hIndex}, moves);
        }
    }

    private  static boolean[][][] readFromTSV() {
        File tsv = new File("./files/maze.tsv");
        Map<Integer, Map<Integer, boolean[]>> res = new HashMap<>();
        int maxWidthIndex = -1, maxHeightIndex = -1;
        try {
            BufferedReader fileReader = new BufferedReader(new FileReader(tsv));
            String line;
            while ((line = fileReader.readLine()) != null) {
                String[] stringValues = line.split("\t");
                int rowIndex = Integer.parseInt(stringValues[0]);
                int colIndex = Integer.parseInt(stringValues[1]);
                if (rowIndex > maxHeightIndex) {
                    maxHeightIndex = rowIndex;
                }
                if (colIndex > maxWidthIndex) {
                    maxWidthIndex = colIndex;
                }
                boolean up = Integer.parseInt(stringValues[2]) == 1;
                boolean right = Integer.parseInt(stringValues[3]) == 1;
                boolean bottom = Integer.parseInt(stringValues[4]) == 1;
                boolean left = Integer.parseInt(stringValues[5]) == 1;
                boolean[] values = new boolean[]{up, right, bottom, left};
                res.putIfAbsent(rowIndex, new HashMap<>());
                res.get(rowIndex).put(colIndex, values);
            }
            fileReader.close();

            boolean[][][] toReturn = new boolean[maxHeightIndex][maxWidthIndex][4];
            for (Integer i : res.keySet()) {
                for (Integer j : res.get(i).keySet()) {
                    toReturn[i][j] = res.get(i).get(j);
                }
            }
            return toReturn;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
