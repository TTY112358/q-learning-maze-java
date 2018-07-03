import ImageCutter.MazeCutter;
import QLearningLearner.MazeProblemLearner;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String args[]){
        MazeCutter mazeCutter = new MazeCutter();
        System.out.println("cut complete");
        boolean[][][] cellInfo = mazeCutter.getCellInfo();
        MazeProblemLearner learner = new MazeProblemLearner(cellInfo);
        learner.train(5000000);
        learner.writeValues();
        while (true){
            Scanner sc = new Scanner(System.in);
            int wIndex = sc.nextInt();
            int hIndex = sc.nextInt();
            List<int[]> moves = learner.playTo(new int[]{wIndex, hIndex});
//            for(int[] move: moves){
//                System.out.println(move[0] + "," + move[1]);
//            }
            if(moves==null){
                System.out.println("no solution");
                continue;
            }
            mazeCutter.drawMovesOnNewMaze(new int[]{wIndex, hIndex},moves);
        }
    }
}
