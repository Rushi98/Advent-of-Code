package com.jogdand;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public class Main {

  public static void main(String[] args) {
    final List<Integer> draws;
    List<Board> boards = new ArrayList<>();
    int drawCount;
    Board lastWinner = null;

    try (Scanner scanner = new Scanner(System.in)) {
      draws = Arrays.stream(scanner.nextLine().split(",")).map(Integer::parseInt).toList();
      while (scanner.hasNextInt()) {
        scanner.nextLine();
        List<List<Integer>> values = IntStream.range(0, 5)
            .mapToObj(i -> Arrays.stream(scanner.nextLine().trim().split(" +"))
                .map(Integer::parseInt).toList())
            .toList();
        boards.add(new Board(values));
      }
    }

    System.out.println("last board is: " + boards.get(boards.size() - 1));

    for (drawCount = 0; drawCount < draws.size(); drawCount++) {
      int draw = draws.get(drawCount);
      boards.forEach(b -> b.mark(draw));
      lastWinner = boards.stream().filter(Board::isWinning).findAny().orElse(null);
      boards = boards.stream().filter(Predicate.not(Board::isWinning)).toList();
      if (boards.isEmpty()) {
        break;
      }
      System.out.println("Draw " + drawCount + ": remaining boards = " + boards.size());
    }

    if (lastWinner == null) return;

    int unmarkedSum = lastWinner.sumUnmarked();
    int winningCall = draws.get(drawCount);
    int score = winningCall * unmarkedSum;
    System.out.println("rounds = " + drawCount);
    System.out.println("winning call = " + winningCall);
    System.out.println("winning board = " + lastWinner);
    System.out.println("unmarked = " + unmarkedSum);
    System.out.println("score = " + score);
  }

}

class Cell {
  int value;
  boolean marked = false;

  public Cell(int value) {
    this.value = value;
  }

  public boolean isMarked() {
    return marked;
  }

  public void mark() {
    marked = true;
  }

  public int getValue() {
    return value;
  }

  @Override
  public String toString() {
    return (marked ? "." : " ") + value;
  }
}

class Board {
  Cell[][] cells;
  int[] rowMarkCount = {0, 0, 0, 0, 0};
  int[] colMarkCount = {0, 0, 0, 0, 0};

  public Board(List<List<Integer>> values) {
    cells = new Cell[5][5];
    for (int i = 0; i < 5; i++) {
      for (int j = 0; j < 5; j++) {
        cells[i][j] = new Cell(values.get(i).get(j));
      }
    }
  }

  public boolean mark(int value) {
    for (int i = 0; i < 5; i++) {
      for (int j = 0; j < 5; j++) {
        if (!cells[i][j].isMarked() && cells[i][j].getValue() == value) {
          cells[i][j].mark();
          rowMarkCount[i]++;
          colMarkCount[j]++;
        }
      }
    }

    return isWinning();
  }

  public boolean isWinning() {
    return Arrays.stream(rowMarkCount).anyMatch(i -> i == 5)
        || Arrays.stream(colMarkCount).anyMatch(i -> i == 5);
  }

  public int sumUnmarked() {
    return Arrays.stream(cells).flatMap(Arrays::stream)
        .filter(Predicate.not(Cell::isMarked))
        .map(Cell::getValue)
        .reduce(Integer::sum)
        .orElse(0);
  }

  @Override
  public String toString() {
    return Arrays.deepToString(cells);
  }
}