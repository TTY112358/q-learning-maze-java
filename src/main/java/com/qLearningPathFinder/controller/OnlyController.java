package com.qLearningPathFinder.controller;

import com.qLearningPathFinder.service.PathFinderService;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("")
public class OnlyController {


    @Resource
    private PathFinderService pathFinderService;

    @ResponseBody
    @GetMapping("map")
    public String getMap() throws IOException {
        return FileCopyUtils.copyToString(new FileReader(new File("./files/maze.tsv")));
    }

    @ResponseBody
    @GetMapping("path")
    public String getPath(Integer x, Integer y) {
        List<int[]> foundPath = pathFinderService.findPath(new int[]{x, y});
        if (foundPath == null) {
            return "no solution";
        } else {
            StringBuilder builder = new StringBuilder();
            for (int[] move : foundPath) {
                builder.append(move[0]).append("\t").append(move[1]).append("\n");
            }
            return builder.toString();
        }
    }

}
