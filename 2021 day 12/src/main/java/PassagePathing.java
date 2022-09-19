import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PassagePathing {
  static Map<String, List<String>> connections;
  static Map<String, Boolean> visited;
  static LinkedList<String> path = new LinkedList<>();
  static boolean usedDoubleVisit = false;

  public static void main(String[] args) {
    List<String> input = new ArrayList<>();
    try (Scanner scanner = new Scanner(System.in)) {
      while (scanner.hasNextLine()) {
        input.add(scanner.nextLine());
      }
    }
    connections = input.stream()
        .map(i -> {
          String[] split = i.split("-");
          return Arrays.stream(new String[][]{{split[0], split[1]}, {split[1], split[0]}});
        })
        .flatMap(Stream::distinct)
        .collect(
            Collectors.groupingBy(
                a -> a[0],
                Collectors.mapping(
                    a -> a[1],
                    Collectors.toList())
            )
        );
    visited = connections.keySet().stream()
        .filter(s -> Character.isLowerCase(s.charAt(0)))
        .collect(Collectors.toMap(s -> s, s -> false));
    visited.put("start", true);
    path.add("start");
    System.out.println(dfs("start"));
  }

  static int dfs(String source) {
    if (source.equals("end")) {
      System.out.println(path);
      return 1;
    }
    List<String> neighbors = connections.get(source)
        .stream()
        .filter(s -> !usedDoubleVisit || Character.isUpperCase(s.charAt(0)) || !visited.get(s))
        .toList();
    int result = 0;
    for (String n : neighbors) {
      if (n.equals("start")) {
        continue;
      }
      boolean usingDoubleVisit = false;
      if (Character.isLowerCase(n.charAt(0))) {
        if (visited.get(n)) {
          usingDoubleVisit = true;
          usedDoubleVisit = true;
        }
        visited.put(n, true);
      }
      path.add(n);
      result += dfs(n);
      path.removeLast();
      if (Character.isLowerCase(n.charAt(0))) {
        if (usingDoubleVisit) {
          usedDoubleVisit = false;
        } else {
          visited.put(n, false);
        }
      }
    }
    return result;
  }
}
