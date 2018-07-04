package com.qLearningPathFinder.service;

import imageCutter.MazeCutter;
import org.springframework.stereotype.Service;
import qLearningLearner.MazeProblemLearner;

import java.util.List;

@Service
public class PathFinderService {
    private MazeProblemLearner learner;

    public PathFinderService() {
        boolean[][][] cellInfo;
        MazeCutter mazeCutter = new MazeCutter();
        System.out.println("cut complete");
        cellInfo = mazeCutter.getCellInfo();

        learner = new MazeProblemLearner(cellInfo);
        learner.train(1000000);
        learner.writeValues();
    }

    public List<int[]> findPath(int[] xy) {
        return learner.playTo(xy);
    }
}
