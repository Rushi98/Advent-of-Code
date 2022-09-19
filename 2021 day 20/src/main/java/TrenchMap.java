import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class TrenchMap {
  static boolean[] algorithm = new boolean[512];

  static boolean[][] enhance(boolean[][] image, boolean infinity) {
    int m = image.length;
    int n = image[0].length;
    boolean[][] paddedImage = new boolean[m + 4][n + 4];
    for (boolean[] row: paddedImage) {
      Arrays.fill(row, infinity);
    }
    for (int i = 0; i < m; i++) {
      System.arraycopy(image[i], 0, paddedImage[i + 2], 2, n);
    }
    boolean[][] result = new boolean[m + 2][n + 2];
    for (int i = 0; i <= m + 1; i++) {
      for (int j = 0; j <= n + 1; j++) {
        int index = 0;
        for (int di = 0; di < 3; di++) {
          for (int dj = 0; dj < 3 ; dj++) {
            index = (2 * index) + (paddedImage[i + di][j + dj] ? 1 : 0);
          }
        }
        result[i][j] = algorithm[index];
      }
    }
    return result;
  }

  static int passes = 50;

  public static void main(String[] args) {
    String algorithmInput;
    List<String> imageInput = new ArrayList<>();
    try (Scanner scanner = new Scanner(System.in)) {
      algorithmInput = scanner.nextLine();
      scanner.nextLine();
      while (scanner.hasNextLine()) {
        imageInput.add(scanner.nextLine());
      }
    }
    for (int i = 0; i < 512; i++) {
      algorithm[i] = algorithmInput.charAt(i) == '#';
    }
    int m = imageInput.size();
    int n = imageInput.get(0).length();
    boolean[][] image = new boolean[m][n];

    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        image[i][j] = imageInput.get(i).charAt(j) == '#';
      }
    }

    print(image);
    boolean infinity = false;
    for (int i = 0; i < passes; i++) {
      image = enhance(image, infinity);
      if (infinity) {
        infinity = algorithm[511];
      } else {
        infinity = algorithm[0];
      }
      print(image);
    }

    long litCount = Arrays.stream(image).mapToInt(row -> {
      int count = 0;
      for (boolean b: row) {
        if (b) count++;
      }
      return count;
    }).summaryStatistics().getSum();
    System.out.println(litCount);
  }

  static void print(boolean[][] image) {
    for (boolean[] row: image) {
      for (boolean b: row) {
        if (b) System.out.print('#');
        else System.out.print('.');
      }
      System.out.println();
    }
    System.out.println();
  }
}
