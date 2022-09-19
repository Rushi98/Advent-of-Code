import java.math.BigInteger;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.ZERO;

public class ExtendedPolymerization {
  public static void main(String[] args) {
    String polymerTemplate;
    List<String> pairInsertionRulesInput = new ArrayList<>();
    try (Scanner scanner = new Scanner(System.in)) {
      polymerTemplate = scanner.nextLine();
      scanner.nextLine();
      while (scanner.hasNextLine()) {
        pairInsertionRulesInput.add(scanner.nextLine());
      }
    }
    LinkedList<Character> polymer = parseTemplate(polymerTemplate);
    Map<String, Character> insertionRules = parseInsertionRules(pairInsertionRulesInput);

    final int targetSteps = 40;
    BigInteger[][][][] dp = new BigInteger[targetSteps + 1][26][26][26];
    for (BigInteger[][][] l1 : dp) {
      for (BigInteger[][] l2 : l1) {
        for (BigInteger[] l3 : l2) {
          Arrays.fill(l3, ZERO);
        }
      }
    }

    // zero steps
    for (int i = 0; i < 26; i++) {
      for (int j = 0; j < 26; j++) {
        dp[0][i][j][i] = dp[0][i][j][i].add(ONE);
        dp[0][i][j][j] = dp[0][i][j][j].add(ONE);
      }
    }

    for (int step = 1; step <= targetSteps; step++) {
      for (int i = 0; i < 26; i++) {
        for (int j = 0; j < 26; j++) {
          char a = (char) ('A' + i);
          char b = (char) ('A' + j);
          String ab = a + "" + b;
          if (!insertionRules.containsKey(ab)) {
            System.arraycopy(dp[step - 1][i][j], 0, dp[step][i][j], 0, 26);
          } else {
            int k = insertionRules.get(ab) - 'A';
            add(dp[step - 1][i][k], dp[step - 1][k][j], dp[step][i][j]);
            dp[step][i][j][k] = dp[step][i][j][k].subtract(ONE);
          }
        }
      }
    }
    BigInteger[] accumulate = new BigInteger[26];
    Arrays.fill(accumulate, ZERO);
    accumulate[polymer.getFirst() - 'A'] = ONE;

    Iterator<Character> iterator = polymer.iterator();
    char prev = iterator.next();
    while (iterator.hasNext()) {
      char cur = iterator.next();
      add(accumulate, dp[targetSteps][prev - 'A'][cur - 'A'], accumulate);
      accumulate[prev - 'A'] = accumulate[prev - 'A'].subtract(ONE);
      prev = cur;
    }

    BigInteger maxFrequency = Arrays.stream(accumulate).max(BigInteger::compareTo).orElse(ZERO);
    BigInteger minFrequency = Arrays.stream(accumulate)
        .filter(Predicate.not(ZERO::equals))
        .min(BigInteger::compareTo).orElse(ZERO);
    System.out.println(maxFrequency.subtract(minFrequency));
  }

  private static void add(BigInteger[] a, BigInteger[] b, BigInteger[] c) {
    for (int i = 0; i < 26; i++) {
      c[i] = b[i].add(a[i]);
    }
  }

  static LinkedList<Character> parseTemplate(String template) {
    char[] elements = template.toCharArray();
    LinkedList<Character> result = new LinkedList<>();
    for (char e : elements) {
      result.add(e);
    }
    return result;
  }

  static Map<String, Character> parseInsertionRules(List<String> rules) {
    return rules.stream()
        .map(rule -> rule.split(" -> "))
        .collect(Collectors.toMap(
            rule -> rule[0],
            rule -> rule[1].charAt(0)
        ));
  }
}
