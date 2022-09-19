import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.ZERO;

public class PacketDecoder {

  public static final String ANSI_RESET = "\u001B[0m";
  public static final String ANSI_BLACK = "\u001B[30m";
  public static final String ANSI_RED = "\u001B[31m";
  public static final String ANSI_GREEN = "\u001B[32m";
  public static final String ANSI_YELLOW = "\u001B[33m";
  public static final String ANSI_BLUE = "\u001B[34m";
  public static final String ANSI_PURPLE = "\u001B[35m";
  public static final String ANSI_CYAN = "\u001B[36m";
  public static final String ANSI_WHITE = "\u001B[37m";

  static Map<Character, String> hex = new HashMap<>();

  static {
    hex.put('0', "0000");
    hex.put('1', "0001");
    hex.put('2', "0010");
    hex.put('3', "0011");
    hex.put('4', "0100");
    hex.put('5', "0101");
    hex.put('6', "0110");
    hex.put('7', "0111");
    hex.put('8', "1000");
    hex.put('9', "1001");
    hex.put('A', "1010");
    hex.put('B', "1011");
    hex.put('C', "1100");
    hex.put('D', "1101");
    hex.put('E', "1110");
    hex.put('F', "1111");
  }

  public static void main(String[] args) {
    String rootPacketHex;
    try (Scanner scanner = new Scanner(System.in)) {
      rootPacketHex = scanner.nextLine();
    }
    String data = Arrays.stream(rootPacketHex.split("")).map(c -> hex.get(c.charAt(0))).collect(Collectors.joining());
    Packet root = parse(data);
    System.out.println(root.value());
  }

  static Packet parse(String data) {
    int curPos = 0;
    int version = Integer.parseInt("0" + data.substring(curPos, curPos + 3), 2);
    curPos += 3;

    int type = Integer.parseInt("0" + data.substring(curPos, curPos + 3), 2);
    curPos += 3;
    BigInteger value = null;
    List<Packet> subPackets = new ArrayList<>();
    if (type == 4) {
      List<String> chunks = new ArrayList<>();
      chunks.add("0");
      while (true) {
        String chunk = data.substring(curPos, curPos + 5);
        curPos += 5;
        chunks.add(chunk.substring(1));
        if (chunk.charAt(0) == '0') {
          break;
        }
      }
      String val = String.join("", chunks);
      value = new BigInteger(val, 2);
    } else {
      int lengthType = data.charAt(curPos) - '0';
      curPos++;
      if (lengthType == 0) {
        int subPacketLen = Integer.parseInt("0" + data.substring(curPos, curPos + 15), 2);
        curPos += 15;
        Packet kid;
        for (int remainingData = subPacketLen; remainingData > 0; remainingData -= kid.size()) {
          kid = parse(data.substring(curPos));
          subPackets.add(kid);
          curPos += kid.size();
        }
      } else {
        int subPacketCount = Integer.parseInt("0" + data.substring(curPos, curPos + 11), 2);
        curPos += 11;
        for (int i = 0; i < subPacketCount; i++) {
          Packet kid = parse(data.substring(curPos));
          subPackets.add(kid);
          curPos += kid.size();
        }
      }
    }
    return switch (type) {
      case 0 -> new SumPacket(version, type, subPackets, curPos);
      case 1 -> new ProductPacket(version, type, subPackets, curPos);
      case 2 -> new MinPacket(version, type, subPackets, curPos);
      case 3 -> new MaxPacket(version, type, subPackets, curPos);
      case 4 -> new ValuePacket(version, type, value, curPos);
      case 5 -> new GtPacket(version, type, subPackets, curPos);
      case 6 -> new LtPacket(version, type, subPackets, curPos);
      case 7 -> new EqPacket(version, type, subPackets, curPos);
      default -> throw new IllegalStateException("Unexpected value: " + type);
    };
  }
}

abstract class Packet {
  private final int version;
  private final int type;
  private final List<Packet> subPackets;
  private final int size;

  Packet(int version, int type, List<Packet> subPackets, int size) {
    this.version = version;
    this.type = type;
    this.subPackets = subPackets;
    this.size = size;
  }

  public int version() {
    return version;
  }

  public int type() {
    return type;
  }

  public List<Packet> subPackets() {
    return subPackets;
  }

  public int size() {
    return size;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj == null || obj.getClass() != this.getClass()) return false;
    var that = (Packet) obj;
    return this.version == that.version &&
        this.type == that.type &&
        Objects.equals(this.subPackets, that.subPackets) &&
        this.size == that.size;
  }

  @Override
  public int hashCode() {
    return Objects.hash(version, type, subPackets, size);
  }

  @Override
  public String toString() {
    return "Packet[" +
        "version=" + version + ", " +
        "type=" + type + ", " +
        "subPackets=" + subPackets + ", " +
        "size=" + size + ']';
  }

  abstract BigInteger value();
}

class SumPacket extends Packet {

  SumPacket(int version, int type, List<Packet> subPackets, int size) {
    super(version, type, subPackets, size);
  }

  @Override
  public BigInteger value() {
    return subPackets().stream()
        .map(Packet::value)
        .reduce(BigInteger::add)
        .orElse(ZERO);
  }
}

class ProductPacket extends Packet {

  ProductPacket(int version, int type, List<Packet> subPackets, int size) {
    super(version, type, subPackets, size);
  }

  @Override
  BigInteger value() {
    return subPackets().stream().map(Packet::value).reduce(BigInteger::multiply).orElse(ZERO);
  }
}

class MinPacket extends Packet {

  MinPacket(int version, int type, List<Packet> subPackets, int size) {
    super(version, type, subPackets, size);
  }

  @Override
  BigInteger value() {
    return subPackets()
        .stream().map(Packet::value)
        .min(BigInteger::compareTo)
        .orElse(ZERO);
  }
}

class MaxPacket extends Packet {

  MaxPacket(int version, int type, List<Packet> subPackets, int size) {
    super(version, type, subPackets, size);
  }

  @Override
  BigInteger value() {
    return subPackets()
        .stream().map(Packet::value)
        .max(BigInteger::compareTo)
        .orElse(ZERO);
  }
}

class GtPacket extends Packet {

  GtPacket(int version, int type, List<Packet> subPackets, int size) {
    super(version, type, subPackets, size);
  }

  @Override
  BigInteger value() {
    return subPackets().get(0).value().compareTo(subPackets().get(1).value()) > 0 ? ONE : ZERO;
  }
}

class LtPacket extends Packet {

  LtPacket(int version, int type, List<Packet> subPackets, int size) {
    super(version, type, subPackets, size);
  }

  @Override
  BigInteger value() {
    return subPackets().get(0).value().compareTo(subPackets().get(1).value()) < 0 ? ONE : ZERO;
  }
}

class EqPacket extends Packet {

  EqPacket(int version, int type, List<Packet> subPackets, int size) {
    super(version, type, subPackets, size);
  }

  @Override
  BigInteger value() {
    return subPackets().get(0).value().equals(subPackets().get(1).value()) ? ONE : ZERO;
  }
}

class ValuePacket extends Packet {

  BigInteger value;

  ValuePacket(int version, int type, BigInteger value, int size) {
    super(version, type, Collections.emptyList(), size);
    this.value = value;
  }

  @Override
  BigInteger value() {
    return value;
  }
}