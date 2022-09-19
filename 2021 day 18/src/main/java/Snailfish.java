import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.math.BigInteger.TWO;
import static java.math.BigInteger.ZERO;

public class Snailfish {
  public static void main(String[] args) {
    List<String> input = new ArrayList<>();
    try (Scanner scanner = new Scanner(System.in)) {
      while (scanner.hasNextLine()) {
        input.add(scanner.nextLine());
      }
    }
    final List<SnailfishNumber> numbers = input.stream().map(SnailfishNumber::fromString).toList();
    BigInteger maxMagnitude = numbers.stream().map(a ->
        numbers.stream()
            .filter(b -> a != b)
            .map(b -> {
              try {
                return SnailfishNumber.add(a, b);
              } catch (CloneNotSupportedException e) {
                e.printStackTrace();
                return a;
              }
            })
    ).flatMap(Stream::distinct)
        .map(n -> n.data.magnitude())
        .max(BigInteger::compareTo)
        .orElse(ZERO);
    System.out.println(maxMagnitude);
  }
}

class Node implements Cloneable {
  static final BigInteger THREE = BigInteger.valueOf(3);
  int value = -1;
  Node left = null;
  Node right = null;

  BigInteger magnitude() {
    if (value != -1) {
      return BigInteger.valueOf(value);
    }
    return left.magnitude().multiply(THREE).add(right.magnitude().multiply(TWO));
  }

  @Override
  public String toString() {
    if (left != null && right != null) {
      return "[" + left + "," + right + "]";
    }
    return Integer.toString(value);
  }

  public Node clone() throws CloneNotSupportedException {
    Node clone = (Node) super.clone();
    clone.value = value;
    if (value == -1) {
      clone.left = left.clone();
      clone.right = right.clone();
    }
    return clone;
  }
}

class StackUtil {
  static <T> Stack<T> cloneStack(Stack<T> stack) {
    Stack<T> clone = new Stack<>();
    for (int i = 0; i < stack.size(); i++) {
      clone.add(stack.elementAt(i));
    }
    return clone;
  }
}

class SnailfishNumber implements Cloneable {
  Node data;

  static SnailfishNumber fromString(String number) {
    Node root = new Node();
    Stack<Node> nodeStack = new Stack<>();
    nodeStack.push(root);
    for (char c : number.toCharArray()) {
      switch (c) {
        case '[' -> {
          Node kid = new Node();
          nodeStack.peek().left = kid;
          nodeStack.push(kid);
        }
        case ']' -> nodeStack.pop();
        case ',' -> {
          nodeStack.pop();
          Node kid = new Node();
          nodeStack.peek().right = kid;
          nodeStack.push(kid);
        }
        default -> nodeStack.peek().value = c - '0';
      }
    }
    SnailfishNumber result = new SnailfishNumber();
    result.data = root;
//    result.reduce();
    return result;
  }

  static SnailfishNumber add(SnailfishNumber a, SnailfishNumber b) throws CloneNotSupportedException {
    a = a.clone();
    b = b.clone();
    System.out.println("\t" + a);
    System.out.println("+\t" + b);
//    a.reduce();
//    b.reduce();
    Node data = new Node();
    data.left = a.data;
    data.right = b.data;
    SnailfishNumber result = new SnailfishNumber();
    result.data = data;
    result.reduce();
    System.out.println("=\t" + result);
    System.out.println();
    return result;
  }

  @Override
  public String toString() {
    return data.toString();
  }

  Stack<Node> findExploding() {
    Stack<Node> path = dfsTillCondition(s -> s.size() == 5 && s.peek().left != null);
    System.out.println("path: " + path);
    if (!path.empty()) {
      return path;
    } else {
      return null;
    }
  }

  Node findPrev(Stack<Node> path) {
    path = StackUtil.cloneStack(path);
    Node prev = path.pop();
    while (!path.empty() && path.peek().left == prev) {
      prev = path.pop();
    }
    if (path.empty() || path.peek().left == null) {
      return null;
    }
    Node n = path.pop().left;
    while (n.right != null) {
      n = n.right;
    }
    return n;
  }

  Node findNext(Stack<Node> path) {
    path = StackUtil.cloneStack(path);
    Node prev = path.pop();
    while (!path.empty() && path.peek().right == prev) {
      prev = path.pop();
    }
    if (path.empty() || path.peek().right == null) {
      return null;
    }
    Node n = path.pop().right;
    while (n.left != null) {
      n = n.left;
    }
    return n;
  }

  Stack<Node> findSplit() {
    Stack<Node> path = dfsTillCondition(s -> s.peek().value >= 10);
    if (path.empty()) {
      return null;
    }
    return path;
  }

  Stack<Node> dfsTillCondition(Predicate<Stack<Node>> condition) {
    Node prev = null;
    Stack<Node> dfs = new Stack<>();
    dfs.push(data);
    while (!dfs.empty() && !condition.test(dfs)) {
      Node cur = dfs.peek();
      if (cur.value != -1 || cur.right == prev) {
        dfs.pop();
      } else if (cur.left == prev) {
        dfs.push(cur.right);
      } else {
        dfs.push(cur.left);
      }
      prev = cur;
    }
    return dfs;
  }

  void reduce() {
    Stack<Node> path;
    while (true) {
      path = findExploding();
      if (path != null) {
        explode(path);
        System.out.println(data);
        continue;
      }
      path = findSplit();
      if (path != null) {
        split(path);
        System.out.println(data);
        continue;
      }
      break;
    }
  }

  void explode(Stack<Node> path) {
    Node n = path.peek();
    Node prev = findPrev(path);
    Node next = findNext(path);
    if (prev != null) {
      prev.value += n.left.value;
    }
    if (next != null) {
      next.value += n.right.value;
    }
    n.left = null;
    n.right = null;
    n.value = 0;
  }

  void split(Stack<Node> path) {
    Node n = path.pop();
    n.left = new Node();
    n.left.value = n.value / 2;
    n.right = new Node();
    n.right.value = n.value - n.left.value;
    n.value = -1;
  }

  @Override
  protected SnailfishNumber clone() throws CloneNotSupportedException {
    SnailfishNumber clone = (SnailfishNumber) super.clone();
    clone.data = data.clone();
    return clone;
  }
}