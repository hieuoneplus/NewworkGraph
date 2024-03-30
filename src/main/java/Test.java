import model.Request;

import java.util.*;
import java.util.HashMap;
import java.util.Map;

public class Test {
    public static void main(String[] args) {
        Map<Request, Integer> m1 = new HashMap<>();
        Queue<String> vnf = new LinkedList<>();
        vnf.add("f1");
        vnf.add("f2");
        vnf.add("f3");
        Request rq = new Request("5","1", vnf, 1.0,1.0,1.0);
        m1.put(rq, 1);


        Map<Request, Integer> m2 = new HashMap<>(m1);
        var c = m2.containsKey(rq);
        Random n = new Random();
        System.out.println(n.nextInt(1));
    }
}
