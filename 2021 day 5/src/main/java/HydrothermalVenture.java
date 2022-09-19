import org.apache.commons.geometry.euclidean.internal.Matrices;
import org.apache.commons.numbers.fraction.Fraction;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.apache.commons.numbers.fraction.Fraction.ONE;
import static org.apache.commons.numbers.fraction.Fraction.ZERO;

public class HydrothermalVenture {
  public static void main(String[] args) {
    List<LatticeLine2D> segments = new ArrayList<>(500);
    try (Scanner scanner = new Scanner(System.in)) {
      scanner.useDelimiter("\\D+");
      while (scanner.hasNextInt()) {
        int x1 = scanner.nextInt();
        int y1 = scanner.nextInt();
        int x2 = scanner.nextInt();
        int y2 = scanner.nextInt();
        segments.add(
            new LatticeLine2D(
                new LatticePoint2D(x1, y1),
                new LatticePoint2D(x2, y2)
            )
        );
      }
    }
    Map<LatticePoint2D, Long> vents = segments.stream()
        .map(LatticeLine2D::getConnecting)
        .flatMap(List::stream)
        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    List<LatticePoint2D> dangerousVents = vents.entrySet()
        .stream().filter(k -> k.getValue() > 1)
        .map(Map.Entry::getKey)
        .toList();
    System.out.println(dangerousVents.size());
  }
}

record LatticePoint2D(int x, int y) {
  // this - other
  LatticePoint2D subtract(LatticePoint2D other) {
    return new LatticePoint2D(x - other.x, y - other.y);
  }

  static final LatticePoint2D ZERO = new LatticePoint2D(0, 0);
}

record LatticeLine2D(LatticePoint2D a, LatticePoint2D b) {
  List<LatticePoint2D> getConnecting() {
    int xMultiplier = Integer.compare(b.x(), a.x());
    int yMultiplier = Integer.compare(b.y(), a.y());
    int steps =
        Math.max(
            Math.abs(a.x() - b.x()),
            Math.abs(a.y() - b.y())
        )
            + 1;
    return IntStream.range(0, steps)
        .mapToObj(i -> new LatticePoint2D(a.x() + xMultiplier * i, a.y() + yMultiplier * i))
        .toList();
  }

  boolean contains(LatticePoint2D p) {
    // a - p - b
    LatticePoint2D ap = p.subtract(a);
    LatticePoint2D pb = b.subtract(p);
    return Integer.compare(ap.x(), 0) == Integer.compare(pb.x(), 0)
        && Integer.compare(ap.y(), 0) == Integer.compare(pb.y(), 0)
        && ap.x() * pb.y() == ap.y() * pb.x();
  }

  boolean isParallel() {
    return a.x() == b.x() || a.y() == b.y();
  }
}

record RationalVector2D(Fraction x,
                        Fraction y) {
  @Override
  public String toString() {
    return x + " " + y;
  }

  public RationalVector2D subtract(RationalVector2D other) {
    return new RationalVector2D(x.subtract(other.x), y.subtract(other.y));
  }

  static RationalVector2D ZERO = new RationalVector2D(Fraction.ZERO, Fraction.ZERO);

  // this / other
  public Fraction scalingFactor(RationalVector2D other) {
    // x1/x2 = y1/y2
    if (equals(other)) {
      return ONE;
    }
    if (other.equals(ZERO)) {
      return null;
    }
    if (other.x.equals(Fraction.ZERO)) {
      if (!x.equals(Fraction.ZERO)) {
        return null;
      }
      // other.y won't be zero
      return y.divide(other.y);
    }
    if (other.y.equals(Fraction.ZERO)) {
      if (!y.equals(Fraction.ZERO)) {
        return null;
      }
      // other.x won't be zero
      return x.divide(other.x);
    }
    Fraction rx = x.divide(other.x);
    Fraction ry = y.divide(other.y);
    if (rx.equals(ry)) {
      return rx;
    }
    return null;
  }
}

class RationalLineSegment extends RationalLine {

  RationalLineSegment(int x1, int y1, int x2, int y2) {
    super(x1, y1, x2, y2);
  }

  // a - p - b
  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  boolean contains(RationalVector2D point) {
    RationalVector2D ap = point.subtract(a);
    RationalVector2D ab = b.subtract(a);
    Fraction scalingFactor = ap.scalingFactor(ab);
    if (scalingFactor == null) {
      return false;
    }
    return scalingFactor.compareTo(ZERO) >= 0 && scalingFactor.compareTo(ONE) <= 0;
  }
}

class RationalLine {
  final RationalVector2D a;
  final RationalVector2D b;

  RationalLine(int x1, int y1, int x2, int y2) {
    a = new RationalVector2D(Fraction.of(x1), Fraction.of(y1));
    b = new RationalVector2D(Fraction.of(x2), Fraction.of(y2));
  }

  Fraction getSlope() {
    RationalVector2D delta = a.subtract(b);
    if (delta.x().equals(ZERO)) return null;
    return delta.y().divide(delta.x());
  }

  RationalVector2D intersection(RationalLine other) {
    Fraction x1 = a.x();
    Fraction y1 = a.y();
    Fraction x2 = b.x();
    Fraction y2 = b.y();

    Fraction x3 = other.a.x();
    Fraction y3 = other.a.y();
    Fraction x4 = other.b.x();
    Fraction y4 = other.b.y();

    Fraction x1x2 = new RationalMatrix2x2(x1, ONE, x2, ONE).determinant();
    Fraction y1y2 = new RationalMatrix2x2(y1, ONE, y2, ONE).determinant();
    Fraction x3x4 = new RationalMatrix2x2(x3, ONE, x4, ONE).determinant();
    Fraction y3y4 = new RationalMatrix2x2(y3, ONE, y4, ONE).determinant();
    Fraction d = new RationalMatrix2x2(x1x2, y1y2, x3x4, y3y4).determinant();
    if (d.equals(ZERO)) {
      return null;
    }

    Fraction x1y1x2y2 = new RationalMatrix2x2(x1, y1, x2, y2).determinant();
    Fraction x3y3x4y4 = new RationalMatrix2x2(x3, y3, x4, y4).determinant();
    Fraction px = getCanonical(new RationalMatrix2x2(x1y1x2y2, x1x2, x3y3x4y4, x3x4).determinant().divide(d));
    Fraction py = getCanonical(new RationalMatrix2x2(x1y1x2y2, y1y2, x3y3x4y4, y3y4).determinant().divide(d));
    return new RationalVector2D(px, py);
  }

  private Fraction getCanonical(Fraction f) {
    if (f.getDenominator() > 0) {
      return f;
    }
    return Fraction.of(Math.abs(f.getNumerator()) * f.signum(), Math.abs(f.getDenominator()));
  }
}

class RationalMatrix2x2 {
  /**
   * | m11 m12 |
   * | m21 m22 |
   */
  final Fraction m11;
  final Fraction m12;
  final Fraction m21;
  final Fraction m22;

  RationalMatrix2x2(Fraction m11, Fraction m12, Fraction m21, Fraction m22) {
    this.m11 = m11;
    this.m12 = m12;
    this.m21 = m21;
    this.m22 = m22;
  }

  Fraction determinant() {
    return m11.multiply(m22).subtract(m12.multiply(m21));
  }
}
