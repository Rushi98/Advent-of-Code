import org.apache.commons.collections4.iterators.PermutationIterator;

import java.util.*;
import java.util.stream.Collectors;

public class SevenSegmentSearch {
  String encode(String data, String domain, String image) {
    Map<Character, Character> encoding = new HashMap<>(domain.length());
    for (int i = 0; i < domain.length(); i++) {
      encoding.put(domain.charAt(i), image.charAt(i));
    }
    char[] result = new char[data.length()];
    for (int i = 0; i < data.length(); i++) {
      result[i] = encoding.get(data.charAt(i));
    }
    return new String(result);
  }

  String hash(String permutation) {
    return Arrays.stream(Digits.values()).map(d ->
        Arrays.stream(
            encode(d.code, "abcdefg", permutation)
                .split("")
        ).sorted().collect(Collectors.joining())
    ).sorted().collect(Collectors.joining("."));
  }

  String hash(List<String> record) {
    return record.stream()
        .map(s -> Arrays.stream(s.split("")).sorted().collect(Collectors.joining()))
        .sorted()
        .collect(Collectors.joining("."));
  }

  Map<String, String> hashPermutation = new HashMap<>();
  {
    List<String> e =  Arrays.stream("abcdefg".split("")).toList();

    for (PermutationIterator<String> it = new PermutationIterator<>(e); it.hasNext(); ) {
      List<String> p = it.next();
      String permutation = String.join("", p);
      String hash = hash(permutation);
      hashPermutation.put(hash, permutation);
    }
  }

  String findPermutation(List<String> record) {
    String hash = hash(record);
    return hashPermutation.get(hash);
  }

  Map<String, Integer> segmentDecode = new HashMap<>(10);
  {
    for (Digits value : Digits.values()) {
      segmentDecode.put(value.code, value.value);
    }
  }

  int decode(String permutation, String encoded) {
    String reversed = encode(encoded, permutation, "abcdefg");
    String sorted = Arrays.stream(reversed.split("")).sorted().collect(Collectors.joining());
    return segmentDecode.get(sorted);
  }

  public static void main(String[] args) {
    SevenSegmentSearch s = new SevenSegmentSearch();
    List<List<String>> patterns = new ArrayList<>(200);
    List<List<String>> inputs = new ArrayList<>(200);
    try (Scanner scanner = new Scanner(System.in)) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        String[] pisplit = line.split(" \\| ");
        List<String> pattern = Arrays.stream(pisplit[0].trim().split("\\s+")).toList();
        patterns.add(pattern);
        List<String> input = Arrays.stream(pisplit[1].trim().split("\\s+")).toList();
        inputs.add(input);
      }
    }
    List<List<Integer>> outputs = new ArrayList<>(200);
    for (int i = 0; i < patterns.size(); i++) {
      List<String> pattern = patterns.get(i);
      List<String> input = inputs.get(i);
      String permutation = s.findPermutation(pattern);
      List<Integer> output = input.stream().map(ip -> s.decode(permutation, ip)).toList();
      outputs.add(output);
    }
    long sum = outputs.stream().map(o ->
        Integer.parseInt(o.stream().map(Object::toString).collect(Collectors.joining()))
    ).reduce(Integer::sum).orElse(0);
    System.out.println(sum);
  }
}

enum Digits {
  ZERO("abcefg", 0),
  ONE("cf", 1),
  TWO("acdeg", 2),
  THREE("acdfg", 3),
  FOUR("bcdf", 4),
  FIVE("abdfg", 5),
  SIX("abdefg", 6),
  SEVEN("acf", 7),
  EIGHT("abcdefg", 8),
  NINE("abcdfg", 9);
  final String code;
  final int value;

  Digits(String code, int value) {
    this.code = code;
    this.value = value;
  }
}