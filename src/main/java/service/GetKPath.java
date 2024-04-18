package service;

import config.Constants;
import model.NetworkGraph;
import model.Request;
import model.Vertex;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

public class GetKPath {
    public static void getOnePath(List<Vertex> path) {
        for(Vertex r : path) {
            System.out.print(r.getLabel() + " ");
        }
        System.out.println();
    }
    public static void getMorePath(List<List<Vertex>> path, BufferedWriter w) throws IOException {
        for(var lsit : path) {
            for(var c : lsit) {
                w.write(c.getLabel() + " ");
            }
            w.newLine();
        }

    }
    public static void getMorePathV2(List<List<Vertex>> path) {
        for(var lsit : path) {
            for(var c : lsit) {
                System.out.print(c.getLabel() + " ");
            }
            System.out.println();
        }
    }
    public static List<List<Vertex>> getV2(NetworkGraph graph, Request rq) {
        long startTime = System.nanoTime();
        FindPath ag = new FindPath();

        var shortestPath = ag.findOrder(graph, rq);
        if(shortestPath.size() > 0) {
            List<List<Vertex>> kPath = new ArrayList<>();

            Queue<List<Vertex>> temp = new LinkedList<>(shortestPath.get(0));
            for (int i = 1; i < shortestPath.size(); i++) {
                while (!temp.isEmpty()) {
                    var list = temp.poll();
                    for (int j = 0; j < shortestPath.get(i).size(); j++) {
                        var copy = new ArrayList<>(list);
                        copy.addAll(shortestPath.get(i).get(j));
                        kPath.add(copy);
                    }
                }
                temp.addAll(kPath);
                kPath.clear();
            }


//            for (var a : temp) {
//                for (model.Vertex ver : a) {
//                    System.out.print(ver.getLabel() + " ");
//                }
//                System.out.println();
//            }
//            System.out.println("co " + temp.size() + " duong");
            var rs = new ArrayList<>(temp);
            Collections.shuffle(rs);
            if(rs.size() > Constants.KPath) {
                return rs.subList(0, Constants.KPath);
            } else {
                return rs;
            }
        } else {
//            System.out.println("Khong co duong");
        }
//        long endTime = System.nanoTime();
//        long executionTime = endTime - startTime;
//        System.out.println("Thời gian chạy: " + executionTime + " milliseconds");
        return null;
    }
}
