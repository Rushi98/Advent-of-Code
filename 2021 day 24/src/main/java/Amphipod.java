import java.util.*;

public class Amphipod {

  static Map<Configuration, Integer> minCost = new HashMap<>();

  public static void main(String[] args) {
    String[] initConfig = {
        "DDDB",
        "DCBA",
        "CBAB",
        "CACA"
    };
    String[] targetConfig = {
        "AAAA", "BBBB", "CCCC", "DDDD"
    };
    Configuration init = parse(initConfig);
    Configuration target = parse(targetConfig);
    minCost.put(init, 0);
    Queue<State> todo = new PriorityQueue<>(Comparator.comparing(State::cost));
    todo.add(new State(init, 0));
    minCost.put(init, 0);
    while (!todo.isEmpty()) {
      State cur = todo.poll();
      if (cur.configuration().equals(target)) break;
      if (cur.cost() != minCost.get(cur.configuration())) continue;
      for (State s : cur.configuration().next(cur.cost())) {
        if (!minCost.containsKey(s.configuration()) || minCost.get(s.configuration()) > s.cost()) {
          minCost.put(s.configuration(), s.cost());
          todo.add(s);
        }
      }
    }
    System.out.println(minCost.get(target));
  }

  static Configuration parse(String[] c) {
    Occupancy[][] r = Arrays.stream(c).map(rs ->
        Arrays.stream(rs.split("")).map(Occupancy::valueOf).toArray(Occupancy[]::new)
    ).toArray(Occupancy[][]::new);
    Occupancy[] h = {
        Occupancy.EMPTY,
        Occupancy.EMPTY,
        Occupancy.EMPTY,
        Occupancy.EMPTY,
        Occupancy.EMPTY,
        Occupancy.EMPTY,
        Occupancy.EMPTY,
        Occupancy.EMPTY,
        Occupancy.EMPTY,
        Occupancy.EMPTY,
        Occupancy.EMPTY
    };
    return new Configuration(h, r);
  }
}

