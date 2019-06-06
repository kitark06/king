package model;

import java.util.Comparator;
import java.util.Objects;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.PriorityBlockingQueue;

public class Composite implements Comparable<Composite> {
    String userid;
    int score;
    private static TreeMap<Composite, Composite> set;

    public Composite(String userid, int score) {
        this.userid = userid;
        this.score = score;
    }

    @Override
    public int compareTo(Composite o) {
        if (this.userid.equals(((Composite) o).userid))
            return 0;
        else if (this.score > o.score)
            return -1;
        else if (this.score < o.score)
            return 1;
        else
            return 0;
    }

    @Override
    public boolean equals(Object o) {
        return this.userid.equals(((Composite) o).userid);
    }

    @Override
    public int hashCode() {
        return userid.hashCode();
    }

    @Override
    public String toString() {
        return "Composite{" +
                "userid='" + userid + '\'' +
                ", score=" + score +
                '}';
    }

    public static void main(String[] args) {
        Composite a = new Composite("a", 2);
        Composite b = new Composite("b", 2);
        Composite bg = new Composite("b", 3);
        Composite ag = new Composite("a", 3);
        Composite al = new Composite("a", 1);

        set = new TreeMap<>();

        addx(a);
        System.out.println(set.containsKey(ag));
        addx(b);
        addx(bg);
        addx(ag);
        addx(al);


//        PriorityBlockingQueue<Composite> queue = new PriorityBlockingQueue<>(15);

//        if(queue.contains(a)) {
//            Composite existing = queue.remove(a);
//            a.compareTo(existing);
//        }

//        queue.add(a);
//        queue.add(b);
//        queue.add(bg);
//        queue.add(ag);
//        queue.add(al);


        System.out.println("----");

        set.keySet().forEach(composite -> {
            System.out.println(set.get(composite));
        });
    }

    public static void addx(Composite c) {
        Composite x;
        if (set.containsKey(c)){
            x = set.get(c);
            if(c.score > x.score)
            {
                set.remove(x);
                set.put(c,c);
            }
        }
    }
}

/*class Comp implements Comparator<Composite> {
    @Override
    public int compare(Composite o1, Composite o2) {
        if (o1.score > o2.score)
            return -1;
        else if (o1.score < o2.score)
            return 1;
        else return 0;
    }

    @Override
    public boolean equals(Object obj) {
        return this.userid.equals(((Composite) o).userid);
    }
}*/
