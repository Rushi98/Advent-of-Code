import java.util.*;

public class Chiton {
  public static void main(String[] args) {
    List<String> input = new ArrayList<>();
    try (Scanner scanner = new Scanner(System.in)) {
      while (scanner.hasNextLine()) {
        input.add(scanner.nextLine());
      }
    }

    int m = input.size();
    int n = input.get(0).length();
    int[][] riskLevels = new int[5 * m + 2][5 * n + 2];
    int[][] tile = new int[m][n];
    for (int i = 0; i < m; i++) {
      String[] split = input.get(i).split("");
      for (int j = 0; j < n; j++) {
        tile[i][j] = split[j].charAt(0) - '0';
      }
    }

    for (int tile_i = 0; tile_i < 5; tile_i++) {
      for (int tile_j = 0; tile_j < 5; tile_j++) {
        for (int i = 0; i < m; i++) {
          for (int j = 0; j < n; j++) {
            riskLevels[tile_i * m + i + 1][tile_j * n + j + 1] = (tile_i + tile_j + tile[i][j] - 1) % 9 + 1;
          }
        }
      }
    }

    Arrays.fill(riskLevels[0], Integer.MAX_VALUE / 10);
    Arrays.fill(riskLevels[5 * m + 1], Integer.MAX_VALUE / 10);
    for (int i = 1; i <= 5 * m ; i++) {
      riskLevels[i][0] = Integer.MAX_VALUE / 10;
      riskLevels[i][5 * n + 1] = Integer.MAX_VALUE / 10;
    }

    int[][] minRisk = new int[5 * m + 2][5 * n + 2];
    for (int[] row : minRisk) {
      Arrays.fill(row, Integer.MAX_VALUE / 10);
    }

    PriorityQueue<int[]> todo = new PriorityQueue<>(Comparator.comparingInt(a -> minRisk[a[0]][a[1]]));
    minRisk[1][1] = 0;
    todo.add(new int[]{1, 1});

    int[][] deltas = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    while (!todo.isEmpty() && minRisk[5 * m][5 * n] == Integer.MAX_VALUE / 10) {
      int[] cur = todo.poll();

      for (int[] delta : deltas) {
        int i = cur[0] + delta[0];
        int j = cur[1] + delta[1];
        if (riskLevels[i][j] == Integer.MAX_VALUE / 10 || minRisk[i][j] != Integer.MAX_VALUE / 10) {
          continue;
        }
        minRisk[i][j] = minRisk[cur[0]][cur[1]] + riskLevels[i][j];
        todo.add(new int[]{i, j});
      }
    }
    System.out.println(minRisk[5 * m][5 * n]);
  }
}
