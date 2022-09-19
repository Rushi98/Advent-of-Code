import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.abs;

public final class Configuration {
  /*
  h h x h x h x h x h h
      r   r   r   r
      r   r   r   r
      r   r   r   r
      r   r   r   r
  */

  public static final int[] nonEntryHallways = {0, 1, 3, 5, 7, 9, 10};
  public final Occupancy[] hallways;
  public final Occupancy[][] rooms;

  public Configuration(Occupancy[] hallways, Occupancy[][] rooms) {
    this.hallways = hallways;
    this.rooms = rooms;
  }

  List<State> next(int offset) {
    System.out.println("next: offset = " + offset);
    System.out.println(this);
    System.out.println("-------------------------");
    List<State> result = new ArrayList<>();
    Configuration c;
    // room to hallway
    for (int r = 0; r < 4; r++) {
      if (isRoomClear(r)) continue;
      final int rEntry = 2 * r + 2;
      int[] H = Arrays.stream(nonEntryHallways).filter(h ->
          range(h, rEntry).mapToObj(i -> hallways[i]).allMatch(Occupancy.EMPTY::equals)
          && hallways[h] == Occupancy.EMPTY
      ).toArray();
      int i = getEmptyPlaceInRoom(r) + 1;
      Occupancy o = rooms[r][i];
      for (int h : H) {
        int dist = (i + 1) + abs(rEntry - h);
        c = copy();
        c.hallways[h] = o;
        c.rooms[r][i] = Occupancy.EMPTY;
        System.out.println("cost : " + o.cost * dist);
        System.out.println(c);
        result.add(new State(c, offset + o.cost * dist));

      }
    }
    // hallway to room
    for (int h : nonEntryHallways) {
      Occupancy o = hallways[h];
      if (o == Occupancy.EMPTY) continue;
      int r = o.room;
      if (!isRoomClear(r)) continue;
      int rEntry = 2 * r + 2;
      if (!range(h, rEntry).mapToObj(i -> hallways[i]).allMatch(Occupancy.EMPTY::equals)) continue;
      int i = getEmptyPlaceInRoom(o.room);
      int dist = (i + 1) + abs(rEntry - h);
      c = copy();
      c.hallways[h] = Occupancy.EMPTY;
      c.rooms[r][i] = hallways[h];
      System.out.println("cost : " + o.cost * dist);
      System.out.println(c);
      result.add(new State(c, offset + o.cost * dist));
    }
    return result;
  }

  public IntStream range(int a, int b) {
    if (a < b) return IntStream.rangeClosed(a + 1, b - 1);
    else return IntStream.rangeClosed(b + 1, a - 1);
  }

  boolean canSwapHallways(int src, int dest) {
    return hallways[src] != Occupancy.EMPTY && hallways[dest] == Occupancy.EMPTY;
  }

  State swapHallways(int src, int dest) {
    Configuration c = copy();
    c.hallways[src] = Occupancy.EMPTY;
    c.hallways[dest] = hallways[src];
    int cost = abs(src - dest) * hallways[src].cost;
    return new State(c, cost);
  }

  boolean canEnterRoom(int h, int r) {
    return hallways[h].room == r && isRoomClear(r);
  }

  State enterRoom(int h, int r) {
    Configuration c = copy();
    int i = getEmptyPlaceInRoom(r);
    c.hallways[h] = Occupancy.EMPTY;
    c.rooms[r][i] = hallways[h];
    int cost = (2 + i) * hallways[h].cost;
    return new State(c, cost);
  }

  boolean isRoomClear(int room) {
    return Arrays.stream(rooms[room]).allMatch(o -> o == Occupancy.EMPTY || o.room == room);
  }

  boolean canLeaveRoom(int h, int r) {
    return !isRoomClear(r) && hallways[h] == Occupancy.EMPTY;
  }

  State leaveRoom(int h, int r) {
    int i = getEmptyPlaceInRoom(r) + 1;
    Configuration c = copy();
    c.rooms[r][i] = Occupancy.EMPTY;
    c.hallways[h] = rooms[r][i];
    int cost = (i + 2) * rooms[r][i].cost;
    return new State(c, cost);
  }

  int getEmptyPlaceInRoom(int room) {
    for (int i = 0; i < 4; i++) {
      if (rooms[room][i] != Occupancy.EMPTY) {
        return i - 1;
      }
    }
    return 3;
  }

  public Configuration copy() {
    Occupancy[] h = Arrays.copyOf(hallways, 11);
    Occupancy[][] r = new Occupancy[4][];
    for (int i = 0; i < 4; i++) {
      r[i] = Arrays.copyOf(rooms[i], 4);
    }
    return new Configuration(h, r);
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof Configuration
        && Arrays.deepEquals(((Configuration) obj).rooms, rooms)
        && Arrays.equals(((Configuration) obj).hallways, hallways);
  }

  @Override
  public int hashCode() {
    return Objects.hash(Arrays.hashCode(hallways), Arrays.deepHashCode(rooms));
  }

  public Occupancy[] hallways() {
    return hallways;
  }

  public Occupancy[][] rooms() {
    return rooms;
  }

  @Override
  public String toString() {
    String firstLine = Arrays.stream(hallways).map(o -> o.s).collect(Collectors.joining());
    String[] rem = new String[4];
    for (int i = 0; i < 4; i++) {
      rem[i] = "  ";
      for (int j = 0; j < 4; j++) {
        rem[i] = rem[i] + rooms[j][i].s + " ";
      }
    }
    return firstLine + "\n" + String.join("\n", rem);
  }

}
