import java.math.BigInteger;

public class DiracDice {
  static int p1Init = 6;
  static int p2Init = 3;
  static int cutoff = 21;
  static BigInteger[][][][][] cache = new BigInteger[cutoff][cutoff][10][10][2];
  static BigInteger[][] sumDistribution = {
      {BigInteger.valueOf(3), BigInteger.valueOf(1)}, // 1 1 1
      {BigInteger.valueOf(4), BigInteger.valueOf(3)}, // 2 1 1
      {BigInteger.valueOf(5), BigInteger.valueOf(6)}, // 3 1 1 ; 2 2 1
      {BigInteger.valueOf(6), BigInteger.valueOf(7)}, // 3 2 1 ; 2 2 2
      {BigInteger.valueOf(7), BigInteger.valueOf(6)}, // 3 3 1 ; 3 2 2
      {BigInteger.valueOf(8), BigInteger.valueOf(3)}, // 3 3 2
      {BigInteger.valueOf(9), BigInteger.valueOf(1)}  // 3 3 3
  };
  public static void main1(String[] args) {
    BigInteger[][][][][] dp = new BigInteger[cutoff][cutoff][10][10][2];
    //                                        |        |     |   |
    //  Player 1 Initial Score ---------------+        |     |   |
    //  Player 2 Initial Score ------------------------+     |   |
    //  Player 1 Initial Position ---------------------------+   |
    //  Player 2 Initial Position -------------------------------+
    //  In how many universe does Player 1, 2 wins, if the game starts with given configuration
    for (int anchorScore = cutoff - 1; anchorScore >= 0; anchorScore--) {
      // fill for p1Score
      for (int p2Score = 0; p2Score <= anchorScore; p2Score++) {
        play1(dp, anchorScore, p2Score);
      }
      // fill for p2Score
      for (int p1Score = 0; p1Score < anchorScore; p1Score++) {
        play1(dp, p1Score, anchorScore);
      }
    }
    BigInteger p1Wins = dp[0][0][p1Init][p2Init][0];
    BigInteger p2Wins = dp[0][0][p1Init][p2Init][1];
    System.out.println(p1Wins + " " + p2Wins);
    if (p1Wins.compareTo(p2Wins) > 0) {
      System.out.println(p1Wins);
    } else {
      System.out.println(p2Wins);
    }
  }

  private static void play1(BigInteger[][][][][] dp, int p1Score, int p2Score) {
    for (int k = 0; k < 10; k++) {
      int p1Pos = k + 1;
      for (int l = 0; l < 10; l++) {
        int p2Pos = l + 1;
        BigInteger p1Wins = BigInteger.ZERO;
        BigInteger p2Wins = BigInteger.ZERO;
        for (BigInteger[] d: sumDistribution) {
          int _p1Pos = (p1Pos - 1 + d[0].intValue()) % 10 + 1;
          int _p1Score = _p1Pos + p1Score;
          if (_p1Score >= cutoff) {
            p1Wins = p1Wins.add(d[1]);
          } else {
            BigInteger[] nextConfiguration = dp[p2Score][_p1Score][p2Pos - 1][_p1Pos - 1];
            p1Wins = p1Wins.add(nextConfiguration[1].multiply(d[1]));
            p2Wins = p2Wins.add(nextConfiguration[0].multiply(d[1]));
          }
        }
        dp[p1Score][p2Score][p1Pos - 1][p2Pos - 1][0] = p1Wins;
        dp[p1Score][p2Score][p1Pos - 1][p2Pos - 1][1] = p2Wins;
      }
    }
  }

  public static void main(String[] args) {
    BigInteger[] result = play(0, 0, p1Init, p2Init);
    System.out.println(result[0] + " " + result[1]);
    if (result[0].compareTo(result[1]) > 0) {
      System.out.println(result[0]);
    } else {
      System.out.println(result[1]);
    }
    main1(args);
  }

  static BigInteger[] play(int p1Score, int p2Score, int p1Pos, int p2Pos) {
    BigInteger[] result = cache[p1Score][p2Score][p1Pos - 1][p2Pos - 1];
    if (result[0] != null) {
      return result;
    }
    BigInteger p1Wins = BigInteger.ZERO;
    BigInteger p2Wins = BigInteger.ZERO;
    for (BigInteger[] d: sumDistribution) {
      int _p1Pos = (p1Pos - 1 + d[0].intValue()) % 10 + 1;
      int _p1Score = p1Score + _p1Pos;
      if (_p1Score >= cutoff) {
        p1Wins = p1Wins.add(d[1]);
      } else {
        BigInteger[] nextConfig = play(p2Score, _p1Score, p2Pos, _p1Pos);
        p1Wins = p1Wins.add(nextConfig[1].multiply(d[1]));
        p2Wins = p2Wins.add(nextConfig[0].multiply(d[1]));
      }
    }
    result[0] = p1Wins;
    result[1] = p2Wins;
    return result;
  }
}