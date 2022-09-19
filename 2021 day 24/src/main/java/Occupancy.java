public enum Occupancy {
  EMPTY(0, -1, "."),
  A(1, 0, "A"),
  B(10, 1, "B"),
  C(100, 2, "C"),
  D(1000, 3, "D");
  final int cost;
  final int room;
  final String s;

  Occupancy(int cost, int room, String s) {
    this.cost = cost;
    this.room = room;
    this.s = s;
  }
}
