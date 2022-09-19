public record State(Configuration configuration, int cost) {
  @Override
  public boolean equals(Object obj) {
    return obj instanceof State
        && configuration.equals(((State) obj).configuration)
        && cost == ((State) obj).cost;
  }
}
