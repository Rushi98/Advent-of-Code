import java.util.*;

public class SmokeBasin {
  public static void main(String[] args) {
    List<String> input = new ArrayList<>();
    try(Scanner scanner = new Scanner(System.in)) {
      while (scanner.hasNextLine()) {
        input.add(scanner.nextLine());
      }
    }
    int m = input.size();
    int n = input.get(0).length();
    int[][] heightmap = new int[m + 2][n + 2];
    Arrays.fill(heightmap[0], 10);
    Arrays.fill(heightmap[m + 1], 10);
    for (int i = 0; i < m; i++) {
      heightmap[i + 1][0] = 10;
      for (int j = 0; j < n; j++) {
        heightmap[i + 1][j + 1] = input.get(i).charAt(j) - '0';
      }
      heightmap[i + 1][input.get(i).length() + 1] = 10;
    }

    boolean[][] visited = new boolean[m + 2][n + 2];
    for (boolean[] row: visited) {
      Arrays.fill(row, false);
    }

    PriorityQueue<Integer> minHeap = new PriorityQueue<>();

    int[][] deltas = {
        {-1, 0},
        {1, 0},
        {0, 1},
        {0, -1}
    };

    for (int i = 1; i <= m; i++) {
      for (int j = 1; j <= n; j++) {
        int basinSize = 0;
        Stack<int[]> todo = new Stack<>();
        if (heightmap[i][j] < 9 && !visited[i][j]) {
          todo.push(new int[]{i, j});
          visited[i][j] = true;
        }
        while (!todo.empty()) {
          basinSize++;
          int[] cur = todo.pop();
          int curi = cur[0];
          int curj = cur[1];
          for(int[] delta: deltas) {
            int _i = curi + delta[0];
            int _j = curj + delta[1];
            if (!visited[_i][_j] && heightmap[_i][_j] < 9) {
              visited[_i][_j] = true;
              todo.push(new int[]{_i, _j});
            }
          }
        }
        minHeap.add(basinSize);
        if (minHeap.size() > 3) {
          minHeap.poll();
        }
      }
    }
    int p = minHeap.stream().reduce(1, (a, b) -> a * b);
    System.out.println(p);
  }
}
