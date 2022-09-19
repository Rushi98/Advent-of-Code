import java.util.*;

public class BeaconScanner {
  static int cutoff = 12;

  static int[][][] rotations = {{
      {1, 0, 0},
      {0, 1, 0},
      {0, 0, 1},
  }, {
      {0, 1, 0},
      {0, 0, 1},
      {1, 0, 0},
  }, {
      {0, 0, 1},
      {1, 0, 0},
      {0, 1, 0},
  }, {
      {-1, 0, 0},
      {0, 0, -1},
      {0, -1, 0},
  }, {
      {0, -1, 0},
      {-1, 0, 0},
      {0, 0, -1},
  }, {
      {0, 0, -1},
      {0, -1, 0},
      {-1, 0, 0},
  }, {
      {-1, 0, 0},
      {0, 1, 0},
      {0, 0, -1},
  }, {
      {0, -1, 0},
      {0, 0, 1},
      {-1, 0, 0},
  }, {
      {0, 0, -1},
      {1, 0, 0},
      {0, -1, 0},
  }, {
      {1, 0, 0},
      {0, 0, -1},
      {0, 1, 0},
  }, {
      {0, 1, 0},
      {-1, 0, 0},
      {0, 0, 1},
  }, {
      {0, 0, 1},
      {0, -1, 0},
      {1, 0, 0},
  }, {
      {1, 0, 0},
      {0, -1, 0},
      {0, 0, -1},
  }, {
      {0, 1, 0},
      {0, 0, -1},
      {-1, 0, 0},
  }, {
      {0, 0, 1},
      {-1, 0, 0},
      {0, -1, 0},
  }, {
      {-1, 0, 0},
      {0, 0, 1},
      {0, 1, 0},
  }, {
      {0, -1, 0},
      {1, 0, 0},
      {0, 0, 1},
  }, {
      {0, 0, -1},
      {0, 1, 0},
      {1, 0, 0},
  }, {
      {-1, 0, 0},
      {0, -1, 0},
      {0, 0, 1},
  }, {
      {0, -1, 0},
      {0, 0, -1},
      {1, 0, 0},
  }, {
      {0, 0, -1},
      {-1, 0, 0},
      {0, 1, 0},
  }, {
      {1, 0, 0},
      {0, 0, 1},
      {0, -1, 0},
  }, {
      {0, 1, 0},
      {1, 0, 0},
      {0, 0, -1},
  }, {
      {0, 0, 1},
      {0, 1, 0},
      {-1, 0, 0},
  }
  };

  static int[][] multiply(int[][] a, int[][] b) {
    int l = a.length;
    int m = b.length;
    int n = b[0].length;

    int[][] product = new int[l][n];
    for (int[] row : product) {
      Arrays.fill(row, 0);
    }
    for (int i = 0; i < l; i++) {
      for (int j = 0; j < m; j++) {
        for (int k = 0; k < n; k++) {
          product[i][k] += a[i][j] * b[j][k];
        }
      }
    }
    return product;
  }

  public static void main(String[] args) {
    List<List<String>> scannerInputs = new ArrayList<>();
    try (Scanner scanner = new Scanner(System.in)) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        if (line.contains("scanner")) {
          scannerInputs.add(new ArrayList<>());
        } else if (!line.isBlank()) {
          scannerInputs.get(scannerInputs.size() - 1).add(line);
        }
      }
    }
    int[][][] beaconRecords = scannerInputs.stream().map(scannerInput ->
        scannerInput.stream().map(record ->
            Arrays.stream(record.split(","))
                .mapToInt(Integer::parseInt)
                .toArray()
        ).toArray(int[][]::new)
    ).toArray(int[][][]::new);
    Map<Integer, int[][]> orientationMap = new HashMap<>();
    Map<Integer, int[]> shiftMap = new HashMap<>();
    orientationMap.put(0, rotations[0]);
    shiftMap.put(0, new int[]{0, 0, 0});
    Queue<Integer> todo = new ArrayDeque<>();
    todo.add(0);
    while (!todo.isEmpty()) {
      int cur = todo.poll();
      int[][] A = add(multiply(beaconRecords[cur], orientationMap.get(cur)), shiftMap.get(cur));
      for (int i = 0; i < beaconRecords.length; i++) {
        if (orientationMap.containsKey(i)) continue;
        for (int[][] rotation : rotations) {
          int[][] B = multiply(beaconRecords[i], rotation);
          int[] shift = findShift(A, B);
          if (shift != null) {
            orientationMap.put(i, rotation);
            shiftMap.put(i, shift);
            todo.add(i);
            break;
          }
        }
      }
    }
    int[][][] map = new int[scannerInputs.size()][][];
    for (int i = 0; i < map.length; i++) {
      if (orientationMap.containsKey(i)) {
        map[i] = add(multiply(beaconRecords[i], orientationMap.get(i)), shiftMap.get(i));
      } else {
        System.out.println("stray " + i);
        map[i] = beaconRecords[i];
      }
    }
    int[][] points = Arrays.stream(map)
        .flatMap(Arrays::stream)
        .sorted(comparator)
        .toArray(int[][]::new);
//    final List<int[]> distinctPoints = new ArrayList<>();
//    distinctPoints.add(points[0]);
//    for (int i = 1; i < points.length; i++) {
//      if (comparator.compare(points[i], points[i - 1]) != 0) {
//        distinctPoints.add(points[i]);
//      }
//    }
    int farthestDistance = shiftMap.values().stream().mapToInt(a ->
        shiftMap.values().stream().mapToInt(b -> manhattanDistance(a, b)).summaryStatistics().getMax()
    ).summaryStatistics().getMax();
    System.out.println(farthestDistance);
  }

  static int manhattanDistance(int[] a, int[] b) {
    int distance = 0;
    for (int i = 0; i < 3; i++) {
      distance += Math.abs(a[i] - b[i]);
    }
    return distance;
  }

  static int[] subtract(int[] a, int[] b) {
    int m = a.length;
    int[] difference = new int[m];
    for (int i = 0; i < m; i++) {
      difference[i] = a[i] - b[i];
    }
    return difference;
  }

//  static int[][] subtract(int[][] a, int[] b) {
//    return Arrays.stream(a).map(i -> subtract(i, b)).toArray(int[][]::new);
//  }

  static int[] add(int[] a, int[] b) {
    int[] sum = new int[a.length];
    for (int i = 0; i < a.length; i++) {
      sum[i] = a[i] + b[i];
    }
    return sum;
  }

  static int[][] add(int[][] a, int[] b) {
    return Arrays.stream(a).map(i -> add(i, b)).toArray(int[][]::new);
  }

  static Comparator<int[]> comparator = (a, b) -> {
    for (int i = 0; i < 3; i++) {
      if (a[i] != b[i]) {
        return Integer.compare(a[i], b[i]);
      }
    }
    return 0;
  };

  static int countCommon(int[][] A, int[][] B) {
    int count = 0;
    for (int i = 0, j = 0; i < A.length && j < B.length; ) {
      int comp = comparator.compare(A[i], B[j]);
      if (comp == 0) {
        i++;
        j++;
        count++;
      } else if (comp < 0) {
        i++;
      } else {
        j++;
      }
    }
    return count;
  }

  static int[] findShift(int[][] A, int[][] B) {
    Arrays.sort(A, comparator);
    Arrays.sort(B, comparator);
    for (int[] a : A) {
      for (int[] b : B) {
        int[] difference = subtract(a, b); // A - B
        int[][] shiftedB = add(B, difference);
        if (countCommon(A, shiftedB) >= cutoff) {
          return difference;
        }
      }
    }
    return null;
  }
}