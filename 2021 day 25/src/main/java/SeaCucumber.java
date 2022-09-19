import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.IntStream;

public class SeaCucumber {
  static boolean next(char[][] map) {
    int m = map.length;
    int n = map[0].length;
    boolean[] b = {false};
    IntStream.range(0, m).forEach(i -> {
      int[] movers = IntStream.range(0, n).filter(j -> map[i][j] == '>' && map[i][(j + 1) % n] == '.').toArray();
      for (int j: movers) {
        map[i][j] = '.';
        map[i][(j + 1) % n] = '>';
        b[0] = true;
      }
    });
//    print(map);
    IntStream.range(0, n).forEach(j -> {
      int[] movers = IntStream.range(0, m).filter(i -> map[i][j] == 'v' && map[(i + 1) % m][j] == '.').toArray();
      for (int i: movers) {
        map[i][j] = '.';
        map[(i + 1) % m][j] = 'v';
        b[0] = true;
      }
    });
//    print(map);
    return b[0];
  }

  static void print(char[][] map) {
    for (char[] row: map) {
      for (char c: row) {
        System.out.print(c);
      }
      System.out.println();
    }
    System.out.println();
  }

  public static void main(String[] args) {
    List<String> mapInput = new ArrayList<>();
    try (Scanner scanner = new Scanner(System.in)) {
      while (scanner.hasNextLine()) {
        mapInput.add(scanner.nextLine());
      }
    }
    char[][] map = mapInput.stream().map(String::toCharArray).toArray(char[][]::new);
//    print(map);
    int count = 0;
    while (next(map)) {
      count++;
    }
    System.out.println(count);
  }
}
