import java.util.Random;
import java.util.Timer;

public class ComputerController {
  private Block block;
  private Random rand;
  // private int controlCount = 0;
  // テストのために20回にセット
  public int controlCount = 100;
  public int currentCount = 0;

  public ComputerController(Block block) {
    this.block = block;
    rand = new Random();
  }

  // 1つのブロックに対して操作する回数をセット
  public void setControlCount(int count) {
    this.controlCount = count;
  }

  // 1つのブロックに対しての操作が終わった時に呼んであげる
  public void resetControl() {
    this.controlCount = 0;
    this.currentCount = 0;
  }

  public int controlBlock() {
    if (this.currentCount != this.controlCount+1) {
      this.currentCount++;
      // todo:ランダムで返しているものを、
      // 学習させたもので返すようにする
      // 0、1、2の左、右、回転の3つだけに絞る。
      return rand.nextInt(7);
    } else {
      // 操作が終わったらブロックを一気に落とす。
      // 4で下に落とす
      return 7;
    }

  }
}
