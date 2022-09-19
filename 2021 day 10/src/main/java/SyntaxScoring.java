import java.math.BigInteger;
import java.util.*;

public class SyntaxScoring {
  public static void main(String[] args) {
    List<String> input = new ArrayList<>();
    try (Scanner scanner = new Scanner(System.in)) {
      while (scanner.hasNextLine()) {
        input.add(scanner.nextLine());
      }
    }
    BigInteger three = BigInteger.valueOf(3);
    BigInteger four = BigInteger.valueOf(4);
    Map<Character, BigInteger> pointTable = Map.of(
        ')', BigInteger.ONE,
        ']', BigInteger.TWO,
        '}', three,
        '>', four
    );
    Map<Character, Character> closingBracket = Map.of(
        '(', ')',
        '[', ']',
        '{', '}',
        '<', '>'
    );
    String openingBraces = "([{<";
    List<BigInteger> autocompletionScores = new ArrayList<>();
    BigInteger five = BigInteger.valueOf(5);
    for (String s : input) {
      Stack<Character> stack = new Stack<>();
      for (char c : s.toCharArray()) {
        if (openingBraces.contains(c + "")) {
          stack.push(c);
        } else {
          char openingBracket = stack.pop();
          if (closingBracket.get(openingBracket) != c) {
            stack.clear();
            break;
          }
        }
      }
      if (stack.empty()) {
        continue;
      }
      BigInteger score = BigInteger.ZERO;
      while (!stack.empty()) {
        char c = stack.pop();
        score = score.multiply(five);
        score = score.add(pointTable.get(closingBracket.get(c)));
      }
      autocompletionScores.add(score);
    }
    Collections.sort(autocompletionScores);
    int n = autocompletionScores.size();
    System.out.println(autocompletionScores.get(n / 2));
  }
}
