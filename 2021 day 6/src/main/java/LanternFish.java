import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

public class LanternFish {
  public static void main(String[] args) {

  }
}

class Matrix {
  BigInteger[][] m;
  Matrix(int n) {
    m = new BigInteger[n][n];
    for (BigInteger[] bigIntegers : m) {
      Arrays.fill(bigIntegers, BigInteger.ZERO);
    }
  }
  Matrix() {}
  void set(int i, int j, int v) {
    m[i][j] = BigInteger.valueOf(v);
  }

  Matrix multiply(Matrix other) {
    int n = m.length;
    Matrix p = new Matrix(n);
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        for (int k = 0; k < n; k++) {
          p.m[i][k] = p.m[i][k].add(
              m[i][j].multiply(other.m[j][k])
          );
        }
      }
    }
    return p;
  }

  Matrix identity() {
    int n = m.length;
    Matrix i = new Matrix();
    for (int j = 0; j < n; j++) {
      i.m[j][j] = BigInteger.ONE;
    }
    return i;
  }

  Matrix power(int power) {
    Matrix answer = identity();
    Matrix a = this;
    while (power > 0) {
      if (power % 2 == 1) {
        answer = answer.add(a);
      }
      a = a.multiply(a);
      power /= 2;
    }
    return answer;
  }

  Matrix add(Matrix other) {
    int n = m.length;
    Matrix sum = new Matrix(n);
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        sum.m[i][j] = m[i][j].add(other.m[i][j]);
      }
    }
    return sum;
  }

  public Matrix getClone() {
    Matrix clone = new Matrix();
    int n = m.length;
    clone.m = new BigInteger[n][n];
    for (int i = 0; i < n; i++) {
      System.arraycopy(m[i], 0, clone.m[i], 0, n);
    }
    return clone;
  }
}
