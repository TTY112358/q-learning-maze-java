package QLearningLearner;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class MazeProblemLearner {
    File result = new File("./files/learnedResult.tsv");
    private double epsilon = 0.5;
    private double gamma = 0.92;
    private double alpha = 0.1;
    Double[][] qValues;
    //    HashMap<String, Double> qValues = new HashMap<>();
    ArrayList<int[]> moves = new ArrayList<>();
    private int[] agentPosition;
    Random rn = new Random(new Date().getTime());
    private double epsilonDecay = 0.0001;

    //go-able [height][width][top right bottom left]
    int height;
    int width;
    int[] targetPos;
    boolean[][][] goableMatrix;

    public MazeProblemLearner(boolean[][][] goableMatrix) {
        this.goableMatrix = goableMatrix;
        this.height = goableMatrix.length;
        this.width = goableMatrix[0].length;
        targetPos = new int[]{width - 1, height - 1};
        qValues = new Double[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                qValues[y][x] = null;
            }
        }
    }

    private ArrayList<int[]> getLegalMoves(int[] xy) {
        ArrayList<int[]> legalMoves = new ArrayList<>();
        int x = xy[0];
        int y = xy[1];
        boolean goableArray[] = goableMatrix[y][x];
        if (goableArray[0]) {
            if (y > 0) {
                legalMoves.add(new int[]{x, y - 1});
            }
        }
        if (goableArray[1]) {
            if (x < width - 1) {
                legalMoves.add(new int[]{x + 1, y});
            }
        }
        if (goableArray[2]) {
            if (y < height - 1) {
                legalMoves.add(new int[]{x, y + 1});
            }
        }
        if (goableArray[3]) {
            if (x > 0) {
                legalMoves.add(new int[]{x - 1, y});
            }
        }
        return legalMoves;
    }

    private int[] getMaxQValueMove(ArrayList<int[]> legalMoves) {
        double maxValue = 0.0;
        int maxIndex = 0;
        int index = 0;
        for (int[] move : legalMoves) {
            int x = move[0];
            int y = move[1];
            if (qValues[y][x] != null) {
                double value = qValues[y][x];
                if (value > maxValue) {
                    maxValue = value;
                    maxIndex = index;
                }
            }
            index++;
        }
        if (maxValue == 0.0) {
            int randomMove = this.rn.nextInt(legalMoves.size());
            return legalMoves.get(randomMove);
        }
        int[] bestMove = legalMoves.get(maxIndex);
        return bestMove;
    }

    private double getMaxQValue(int[] state) {
        double maxValue = 0.0;
        ArrayList<int[]> legalMoves = getLegalMoves(state);
        for (int[] move : legalMoves) {
            int x = move[0];
            int y = move[1];
            if (qValues[y][x] != null) {
                double value = qValues[y][x];
                if (value > maxValue) {
                    maxValue = value;
                }
            }
        }
        if (maxValue == 0.0) {
            return 0;
        }
        return maxValue;
    }

    private int[] getMove() {
        ArrayList<int[]> legalMoves = getLegalMoves(this.agentPosition);
        if (rn.nextDouble() < epsilon) {
            int randomMove = rn.nextInt(legalMoves.size());
            return legalMoves.get(randomMove);
        } else {
            return getMaxQValueMove(legalMoves);
        }
    }

    private void updateQValues(int[] prevState, float reward, int[] nextState) {
        int x = prevState[0];
        int y = prevState[1];
        Double value = this.qValues[y][x];
        double oldValue = (value != null) ? value : 0.0;
        double newValue = oldValue + alpha * (reward + gamma * getMaxQValue(nextState) - oldValue);
        this.qValues[y][x] = newValue;
    }

    private void updateAgent(int[] move) {
        int[] prevState = this.agentPosition;
        this.agentPosition[0] = move[0];
        this.agentPosition[1] = move[1];
        updateQValues(prevState, reachedPosition() ? 1 : 0, agentPosition);
    }

    private boolean reachedPosition() {
        return (agentPosition[0] == targetPos[0]) && (agentPosition[1] == targetPos[1]);
    }

    public void train(int max) {
        System.out.println("Starting to train:");
        int madeCount = 0;
        int gameCount = 0;
        while (gameCount < max) {
            if (gameCount % (max / 1000) == 0) {
                System.out.println("Epoch: " + gameCount + "/" + max + " Game finish rate:" + ((double) madeCount) / gameCount);
            }
            int[] startingPos = new int[]{rn.nextInt(width), rn.nextInt(height)};
            agentPosition = startingPos;
            moves = new ArrayList<>();
            int walkCount = 0;
            while (true) {
                int[] move = getMove();
                moves.add(move);
                walkCount++;
                updateAgent(move);
                if (reachedPosition()) {
                    break;
                }
                if (walkCount > 3 * width * height) {
                    break;
                }
            }
            gameCount++;
            if (reachedPosition()) {
                madeCount++;
                this.epsilon -= this.epsilonDecay;
            }
        }
    }

    public List<int[]> playTo(int[] xy) {
        agentPosition = xy;
        moves = new ArrayList<>();
        int startX = xy[0];
        int startY = xy[1];
        if (startX < 0 || startX >= width || startY < 0 || startY >= height) {
            return null;
        }
        int walkCount = 0;
        while (true) {
            int[] move = getMove();
            moves.add(move);
            walkCount++;
            updateAgent(move);
            if (reachedPosition()) {
                break;
            }
            if (walkCount > 3 * width * height) {
                break;
            }
        }
        return moves;
    }

    public void writeValues() {
        try {
            FileWriter fileWriter = new FileWriter(result);
            fileWriter.write("#");
            for (int i = 0; i < width; i++) {
                fileWriter.write("\t" + i);
            }
            fileWriter.write("\n");

            for (int j = 0; j < height; j++) {
                fileWriter.write(j + "");
                for (int i = 0; i < width; i++) {
                    Double value = qValues[j][i];
                    fileWriter.write("\t" + (value == null ? "0.0" : value));
                }
                fileWriter.write("\n");
            }

            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

