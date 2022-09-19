import org.apache.commons.geometry.core.Region;
import org.apache.commons.geometry.core.partitioning.bsp.RegionCutRule;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.ConvexVolume;
import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;
import org.apache.commons.geometry.euclidean.threed.RegionBSPTree3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.shape.Parallelepiped;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ReactorReboot {
  static BigInteger negInfinity;
  static BigInteger posInfinity;
  public static Comparator<BigInteger[]> comparator = Arrays::compare;
  static List<Step> steps = new ArrayList<>();
  static Map<ExtremeCoordinatesPair, Boolean> isOn = new HashMap<>(64000000);
  static DoublePrecisionContext doublePrecisionContext = new EpsilonDoublePrecisionContext(1e-15);

  public static void main(String[] args) {
    List<String> stepInput = new ArrayList<>();
    try (Scanner scanner = new Scanner(System.in)) {
      while (scanner.hasNextLine()) {
        stepInput.add(scanner.nextLine());
      }
    }
    RegionBSPTree3D region = RegionBSPTree3D.empty();
    for (int i = 0; i < stepInput.size(); i++) {
      Step step = Step.parse(stepInput.get(i), i);
      Vector3D from = Vector3D.of(step.dFrom);
      Vector3D to = Vector3D.of(step.dTo);
      Parallelepiped cuboid = Parallelepiped.axisAligned(from, to, doublePrecisionContext);
//      System.out.println(cuboid);
      RegionBSPTree3D toRemove = RegionBSPTree3D.empty();
      toRemove.insert(cuboid);
      if (step.isOn) {
        region.union(toRemove);
//        for (RegionBSPTree3D.RegionNode3D node : region.nodes()) {
//          System.out.println(node.getLocation() + " " + node.getNodeRegion().getSize());
//        }
//        System.out.println();
      } else {
        region.difference(toRemove);
      }
//      steps.add(Step.parse(stepInput.get(i), i));
    }
//    for (RegionBSPTree3D.RegionNode3D node : region.nodes()) {
//      if (node.isInside()) {
//        System.out.println(node.isInside() + " " + node);
//      }
//    }
    BigInteger result = BigInteger.ZERO;
    for (ConvexVolume volume: region.toConvex()) {
      List<Vector3D> vertices = volume.boundaryStream()
          .map(PlaneConvexSubset::getVertices)
          .flatMap(List::stream)
          .toList();
      long[] from = Arrays.stream(vertices.get(0).toArray()).mapToLong(d -> Math.round(Math.ceil(d))).toArray();
      long[] to = Arrays.stream(vertices.get(0).toArray()).mapToLong(d -> Math.round(Math.ceil(d))).toArray();
      for (Vector3D vertex: vertices) {
        for (int i = 0; i < 3; i++) {
          from[i] = Math.min(from[i], Math.round(Math.ceil(vertex.toArray()[i])));
          to[i] = Math.max(to[i], Math.round(Math.ceil(vertex.toArray()[i])));
        }
      }
      BigInteger v = BigInteger.ONE;
      for (int i = 0; i < 3; i++) {
        v = v.multiply(BigInteger.valueOf(to[i] - from[i]));
      }
      result = result.add(v);
    }
    System.out.println(result);
//    steps.sort(Step::compareTo);
    List<Set<BigInteger>> identity = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      identity.add(new TreeSet<>());
    }
    List<Set<BigInteger>> inflectionPoints = steps.stream().map(Step::getInflectionPoints).reduce(identity, (a, b) -> {
      for (int i = 0; i < 3; i++) {
        a.get(i).addAll(b.get(i));
      }
      return a;
    });
    negInfinity = inflectionPoints.stream().map(Set::stream).flatMap(Stream::distinct).min(BigInteger::compareTo).orElse(BigInteger.ZERO);//.subtract(BigInteger.ONE);
//    posInfinity = inflectionPoints.stream().map(Set::stream).flatMap(Stream::distinct).max(BigInteger::compareTo).orElse(BigInteger.ZERO);//.add(BigInteger.ONE);
//    inflectionPoints.get(0).add(posInfinity);
//    inflectionPoints.get(1).add(posInfinity);
//    inflectionPoints.get(2).add(posInfinity);
//    Set<ExtremeCoordinatesPair> cuboids = new HashSet<>();
    BigInteger lastX = null;
    BigInteger pointsInsideCubes = BigInteger.ZERO;
    BigInteger pointsOnFaces = BigInteger.ZERO;
    BigInteger pointsOnEdges = BigInteger.ZERO;
    BigInteger onVerticesCount = BigInteger.ZERO;
    for (BigInteger x : inflectionPoints.get(0)) {
      System.out.println(x);
      if (lastX == null) {
        lastX = x;
        continue;
      }
      BigInteger lastY = null;
      for (BigInteger y : inflectionPoints.get(1)) {
        if (lastY == null) {
          lastY = y;
          continue;
        }
        BigInteger lastZ = null;
        for (BigInteger z : inflectionPoints.get(2)) {
          if (lastZ == null) {
            lastZ = z;
            continue;
          }
          BigInteger[] from = new BigInteger[]{lastX, lastY, lastZ};
          BigInteger[] to = new BigInteger[]{x, y, z};
          ExtremeCoordinatesPair cuboid = new ExtremeCoordinatesPair(from, to);
          pointsInsideCubes = pointsInsideCubes.add(cuboid.countContainedPoints());
          List<ExtremeCoordinatesPair> faces = cuboid.getHyperFaces();
          pointsOnFaces = pointsOnFaces.add(faces.stream().map(ExtremeCoordinatesPair::countContainedPoints).reduce(BigInteger.ZERO, BigInteger::add));
          List<ExtremeCoordinatesPair> edges = faces.stream().map(ExtremeCoordinatesPair::getHyperFaces).flatMap(List::stream).distinct().toList();
          pointsOnEdges = pointsOnEdges.add(edges.stream().map(ExtremeCoordinatesPair::countContainedPoints).reduce(BigInteger.ZERO, BigInteger::add));
          List<ExtremeCoordinatesPair> vertices = edges.stream().map(ExtremeCoordinatesPair::getHyperFaces).flatMap(List::stream).distinct().toList();
          onVerticesCount = onVerticesCount.add(vertices.stream().map(ExtremeCoordinatesPair::countContainedPoints).reduce(BigInteger.ZERO, BigInteger::add));
          lastZ = z;
        }
        lastY = y;
      }
      lastX = x;
    }
//    Set<ExtremeCoordinatesPair> faces = cuboids.stream().map(ExtremeCoordinatesPair::getHyperFaces)
//        .flatMap(List::stream)
//        .collect(Collectors.toSet());
//    Set<ExtremeCoordinatesPair> edges = faces.stream().map(ExtremeCoordinatesPair::getHyperFaces)
//        .flatMap(List::stream)
//        .collect(Collectors.toSet());
//    Set<ExtremeCoordinatesPair> vertices = edges.stream().map(ExtremeCoordinatesPair::getHyperFaces)
//        .flatMap(List::stream)
//        .collect(Collectors.toSet());
//    BigInteger pointsInsideCubes = cuboids.stream().map(ExtremeCoordinatesPair::countContainedPoints)
//        .reduce(BigInteger.ZERO, BigInteger::add);
//    BigInteger pointsOnFaces = faces.stream().map(ExtremeCoordinatesPair::countContainedPoints).reduce(BigInteger.ZERO, BigInteger::add);
//
//    BigInteger pointsOnEdges = edges.stream().map(ExtremeCoordinatesPair::countContainedPoints)
//        .reduce(BigInteger.ZERO, BigInteger::add);
//    BigInteger onVertices = vertices.stream().map(ExtremeCoordinatesPair::countContainedPoints)
//        .reduce(BigInteger.ZERO, BigInteger::add);
    System.out.println(pointsInsideCubes.add(pointsOnFaces).add(pointsOnEdges).add(onVerticesCount));
  }

  public static boolean isOn(BigInteger... coords) {
    ExtremeCoordinatesPair extremeCoordinatesPair = new ExtremeCoordinatesPair(coords, coords);
    if (isOn.containsKey(extremeCoordinatesPair)) {
      return isOn.get(extremeCoordinatesPair);
    }
    boolean answer = steps.stream()
        .filter(s -> s.contains(coords))
        .max(Comparator.comparing(s -> s.order))
        .map(s -> s.isOn)
        .orElse(false);
    isOn.put(extremeCoordinatesPair, answer);
    return answer;
  }
}


class Step implements Comparable<Step> {
  BigInteger[] from;
  BigInteger[] to;
  double[] dFrom;
  double[] dTo;
  boolean isOn;
  int order;

  ExtremeCoordinatesPair toCube() {
    return new ExtremeCoordinatesPair(from, to);
  }

  static Step parse(String input, int order) {
    Step step = new Step();
    step.isOn = input.startsWith("on");
    BigInteger[] numbers = Arrays.stream(input.split("[ .=a-zA-Z,]+")).filter(Predicate.not(String::isBlank)).map(BigInteger::new).toArray(BigInteger[]::new);
    step.from = new BigInteger[]{numbers[0], numbers[2], numbers[4]};
    step.dFrom = Arrays.stream(step.from).mapToDouble(BigInteger::doubleValue)
        .map(d -> d - 0.5d)
        .toArray();
    step.to = new BigInteger[]{numbers[1], numbers[3], numbers[5]};
    step.dTo = Arrays.stream(step.to).mapToDouble(BigInteger::doubleValue)
        .map(d -> d + 0.5d)
        .toArray();
    step.order = order;
    return step;
  }

  List<Set<BigInteger>> getInflectionPoints() {
    List<Set<BigInteger>> result = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      Set<BigInteger> inflections = Set.of(from[i].subtract(BigInteger.ONE), from[i], to[i], to[i].add(BigInteger.ONE));
      result.add(inflections);
    }
    return result;
  }

  boolean contains(BigInteger... coords) {
    return IntStream.range(0, 3)
        .allMatch(i ->
            from[i].compareTo(coords[i]) <= 0 && coords[i].compareTo(to[i]) <= 0
        );
  }

  @Override
  public int compareTo(Step o) {
    return ReactorReboot.comparator.compare(to, o.to);
  }
}

class ExtremeCoordinatesPair implements Comparable<ExtremeCoordinatesPair> {
  BigInteger[] from;
  BigInteger[] to;

  ExtremeCoordinatesPair(BigInteger[] f, BigInteger[] t) {
    from = f;
    to = t;
  }

  @Override
  public int hashCode() {
    return Objects.hash(Arrays.hashCode(from), Arrays.hashCode(to));
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof ExtremeCoordinatesPair
        && Arrays.equals(from, ((ExtremeCoordinatesPair) obj).from)
        && Arrays.equals(to, ((ExtremeCoordinatesPair) obj).to);
  }

  BigInteger countContainedPoints() {
    if (!(ReactorReboot.isOn(from) && ReactorReboot.isOn(to))) {
      return BigInteger.ZERO;
    }
    BigInteger count = BigInteger.ONE;
    for (int i = 0; i < 3; i++) {
      if (!from[i].equals(to[i])) {
        count = count.multiply(to[i].subtract(from[i]).subtract(BigInteger.ONE));
      }
    }
    return count;
  }

//  boolean contains(BigInteger... coord) {
//    return IntStream.range(0, 3).allMatch(i -> from[i].compareTo(coord[i]) < 0 && coord[i].compareTo(to[i]) < 0);
//  }

//  List<ExtremeCoordinatesPair> split(BigInteger... coord) {
//    if (!contains(coord)) return Collections.singletonList(this);
//    return getVertices().stream()
//        .map(vertex -> {
//          BigInteger[] f = new BigInteger[3];
//          BigInteger[] t = new BigInteger[3];
//          for (int i = 0; i < 3; i++) {
//            if (vertex.from[i].compareTo(coord[i]) < 0) {
//              f[i] = vertex.from[i];
//              t[i] = coord[i];
//            } else {
//              t[i] = vertex.from[i];
//              f[i] = coord[i];
//            }
//          }
//          return new ExtremeCoordinatesPair(f, t);
//        }).toList();
//  }
//
//  List<ExtremeCoordinatesPair> intersection(ExtremeCoordinatesPair other) {
//    if (equals(other)) return Collections.singletonList(other);
//    Set<ExtremeCoordinatesPair> thisInOther = getVertices().stream().filter(v -> other.contains(v.from)).collect(Collectors.toSet());
//    Set<ExtremeCoordinatesPair> otherInThis = getVertices().stream().filter(v -> contains(v.from)).collect(Collectors.toSet());
//    List<ExtremeCoordinatesPair> result = new ArrayList<>();
//    result.addAll(thisInOther.stream().map(v -> other.split(v.from)).flatMap(List::stream).toList());
//    result.addAll(otherInThis.stream().map(v -> split(v.from)).flatMap(List::stream).toList());
//    return result;
//  }
//
//  List<ExtremeCoordinatesPair> getVertices() {
//    return getHyperFaces()
//        .stream()
//        .map(ExtremeCoordinatesPair::getHyperFaces)
//        .flatMap(List::stream)
//        .map(ExtremeCoordinatesPair::getHyperFaces)
//        .flatMap(List::stream)
//        .toList();
//  }
//
  List<ExtremeCoordinatesPair> getHyperFaces() {
    List<ExtremeCoordinatesPair> faces = new ArrayList<>();
    // starting at from
    for (int i = 0; i < 3; i++) {
      if (from[i].equals(to[i])) continue;
      BigInteger[] f = Arrays.copyOf(from, 3);
      BigInteger[] t = Arrays.copyOf(to, 3);
      t[i] = f[i];
      faces.add(new ExtremeCoordinatesPair(f, t));
    }

//    // ending at to
//    for (int i = 0; i < 3; i++) {
//      if (from[i].equals(to[i])) continue;
//      BigInteger[] f = Arrays.copyOf(from, 3);
//      BigInteger[] t = Arrays.copyOf(to, 3);
//      f[i] = t[i];
//      faces.add(new ExtremeCoordinatesPair(f, t));
//    }
    return faces;
  }

  @Override
  public int compareTo(ExtremeCoordinatesPair o) {
    if (!Arrays.equals(from, o.from)) {
      return Arrays.compare(from, o.from);
    }
    return Arrays.compare(to, o.to);
  }
}