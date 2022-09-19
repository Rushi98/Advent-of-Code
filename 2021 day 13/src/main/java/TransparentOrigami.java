import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TransparentOrigami {
  public static void main(String[] args) {
    List<String> dotsInput = new ArrayList<>();
    List<String> foldsInput = new ArrayList<>();
    try (Scanner scanner = new Scanner(System.in)) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        if (line.isBlank()) {
          break;
        }
        dotsInput.add(line);
      }
      while (scanner.hasNextLine()) {
        foldsInput.add(scanner.nextLine());
      }
    }
    List<int[]> dots = dotsInput.stream().map(s -> {
      String[] split = s.split(",");
      return new int[]{Integer.parseInt(split[0]), Integer.parseInt(split[1])};
    }).toList();
    List<Function<int[], int[]>> folds = foldsInput.stream().map(s -> {
      int line = Integer.parseInt(s.split("=")[1]);
      if (s.startsWith("fold along x=")) {
        return (Function<int[], int[]>) c -> alongX(line, c);
      } else {
        return (Function<int[], int[]>) c -> alongY(line, c);
      }
    }).toList();
    List<int[]> finalConfig = dots.stream().map(c -> {
      for (Function<int[], int[]> f : folds) {
        c = f.apply(c);
      }
      return Arrays.asList(c[0], c[1]);
    })
        .distinct()
        .map(a -> a.toArray(new Integer[]{}))
        .map(i -> new int[]{i[0], i[1]})
        .toList();
    int xMin = finalConfig.stream().map(a -> a[0]).min(Integer::compareTo).orElse(Integer.MIN_VALUE);
    int xMax = finalConfig.stream().map(a -> a[0]).max(Integer::compare).orElse(Integer.MAX_VALUE);
    int yMin = finalConfig.stream().map(a -> a[1]).min(Integer::compareTo).orElse(Integer.MIN_VALUE);
    int yMax = finalConfig.stream().map(a -> a[1]).max(Integer::compareTo).orElse(Integer.MAX_VALUE);
    boolean[][] paper = new boolean[yMax - yMin + 1][xMax - xMin + 1];
    for (boolean[] row: paper) {
      Arrays.fill(row, false);
    }
    for (int[] dot: finalConfig) {
      paper[dot[1] - yMin][dot[0] - xMin] = true;
    }
    for (boolean[] row: paper) {
      for (boolean b: row) {
        System.out.print(b ? "█" : " ");
      }
      System.out.println();
    }
  }

  static int[] alongX(int x, int[] coordinates) {
    if (coordinates[0] <= x) {
      return coordinates;
    }
    return new int[]{2 * x - coordinates[0], coordinates[1]};
  }

  static int[] alongY(int y, int[] coordinates) {
    if (coordinates[1] <= y) {
      return coordinates;
    }
    return new int[]{coordinates[0], 2 * y - coordinates[1]};
  }
}