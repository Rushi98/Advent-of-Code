import java.util.*;

public class DumboOctopus {
  static int[][] deltas = {{-1, 0}, {-1, -1}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};

  public static void main(String[] args) {
    List<String> input = new ArrayList<>(10);
    try (Scanner scanner = new Scanner(System.in)) {
      while (scanner.hasNextLine()) {
        input.add(scanner.nextLine());
      }
    }
    int[][] grid = new int[12][12];
    Arrays.fill(grid[0], Integer.MIN_VALUE + 10);
    Arrays.fill(grid[11], Integer.MIN_VALUE + 10);
    for (int i = 0; i < 10; i++) {
      grid[i + 1][0] = Integer.MIN_VALUE + 10;
      grid[i + 1][11] = Integer.MIN_VALUE + 10;
      char[] c = input.get(i).toCharArray();
      for (int j = 0; j < 10; j++) {
        grid[i + 1][j + 1] = c[j] - '0';
      }
    }
    int i;
    for (i = 1; i < Integer.MAX_VALUE - 1; i++) {
      if(advance(grid) == 100) {
        break;
      }
    }
    System.out.println(i);
  }

  static long advance(int[][] grid) {
    Stack<int[]> todo = new Stack<>();
    boolean[][] visited = new boolean[12][12];
    for (boolean[] row : visited) {
      Arrays.fill(row, false);
    }
    for (int i = 1; i <= 10; i++) {
      for (int j = 1; j <= 10; j++) {
        grid[i][j]++;
        if (grid[i][j] > 9) {
          todo.push(new int[]{i, j});
          visited[i][j] = true;
        }
      }
    }
    while (!todo.empty()) {
      int[] cur = todo.pop();
      for (int[] delta : deltas) {
        int i = cur[0] + delta[0];
        int j = cur[1] + delta[1];
        grid[i][j]++;
        if (!visited[i][j] && grid[i][j] > 9) {
          visited[i][j] = true;
          todo.push(new int[]{i, j});
        }
      }
    }
    long highlightCount = Arrays.stream(grid)
        .map(row ->
            Arrays.stream(row)
                .filter(i -> i > 9)
                .count()
        ).reduce(Long::sum)
        .orElse(0L);
    Arrays.stream(grid).forEach(row -> {
      for (int i = 1; i <= 10; i++) {
        if (row[i] > 9) {
          row[i] = 0;
        }
      }
    });
    return highlightCount;
  }
}
