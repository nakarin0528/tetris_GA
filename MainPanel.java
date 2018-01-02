import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

/*
* Created on 2006/07/16
*/

public class MainPanel extends JPanel implements KeyListener, Runnable {
  // パネルサイズ
  public static final int WIDTH = 192;
  public static final int HEIGHT = 416;

  // スコア
  // 下キーを押した時に加点されると、難しくなるかもしれないので、今回は排除
  //    private static final int BLOCK_DOWN = 1; // 下キーを押したとき+1
  private static final int ONE_LINE = 100; // 1行消したとき
  private static final int TWO_LINE = 400; // 2行消したとき
  private static final int THREE_LINE = 1000; // 3行消したとき
  private static final int TETRIS = 2000; // 4行消した（テトリス）とき

  // フィールド
  private Field field;
  // ブロック
  private Block block;
  // 次のブロック
  private Block nextBlock;

  // ブロックのイメージ
  private Image blockImage;

  // ゲームループ用スレッド
  private Thread gameLoop;

  private Random rand;

  // スコアパネルへの参照
  private ScorePanel scorePanel;
  // 次のブロックパネルへの参照
  private NextBlockPanel nextBlockPanel;

  private ComputerController computerController;

  public MainPanel(ScorePanel scorePanel, NextBlockPanel nextBlockPanel) {
    // パネルの推奨サイズを設定、pack()するときに必要
    setPreferredSize(new Dimension(WIDTH, HEIGHT));
    // パネルがキー入力を受け付けるようにする
    setFocusable(true);

    this.scorePanel = scorePanel;
    this.nextBlockPanel = nextBlockPanel;
    // computerのcontrollerにブロックを操作するものを渡す
    this.computerController = new ComputerController(block);

    // ブロックのイメージをロード
    loadImage("image/block.gif");

    rand = new Random();
    rand.setSeed(System.currentTimeMillis());

    field = new Field();
    block = createBlock(field);
    nextBlock = createBlock(field);
    nextBlockPanel.set(nextBlock, blockImage);
    // todo: ここに最初のコンピュータの操作を指示？？

    addKeyListener(this);

    // ゲームループ開始
    gameLoop = new Thread(this);
    gameLoop.start();
  }

  /**
  * ゲームループ
  */
  public void run() {
    while (true) {
      // ブロックを下方向へ移動する
      boolean isFixed = block.move(Block.DOWN);
      if (isFixed) { // ブロックが固定されたら
        // コンピュータの操作をリセット
        this.computerController.resetControl();
        // 次のブロックをセット
        block = nextBlock;
        // さらに次のブロックを作成してパネルに表示
        nextBlock = createBlock(field);

        // todo: ここで、次のブロックのコンピュータの操作を処理？

        nextBlockPanel.set(nextBlock, blockImage);
        // 状態を出力
        this.field.printField();
      }

      // ブロックがそろった行を消す
      // deleteLineは消した行数
      int deleteLine = field.deleteLine();

      // 消した行数に応じてスコアをプラスする
      if (deleteLine == 1) {
        scorePanel.upScore(ONE_LINE);
      } else if (deleteLine == 2) {
        scorePanel.upScore(TWO_LINE);
      } else if (deleteLine == 3) {
        scorePanel.upScore(THREE_LINE);
      } else if (deleteLine == 4) {
        scorePanel.upScore(TETRIS);
      }

      // ゲームオーバーか
      if (field.isStacked()) {
        System.out.println("Game Over");
        // todo: スコアをGAに渡す処理を書く

        // スコアをリセット
        scorePanel.setScore(0);
        // フィールドをリセット
        field = new Field();
        block = createBlock(field);
        nextBlock = createBlock(field);
        nextBlockPanel.set(nextBlock, blockImage);
      }

      this.computer();

      repaint();

      try {
        // ここでスピード調整
        // 200がノーマルスピード
        Thread.sleep(30);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  public void paintComponent(Graphics g) {
    super.paintComponent(g);

    // フィールドを描画
    field.draw(g, blockImage);
    // ブロックを描画
    block.draw(g, blockImage);
  }

  public void computer() {
    // コンピュータが操作
    // todo: 0〜4だけに修正する。
    switch (this.computerController.controlBlock()) {
      case 0:
      case 1:
      case 2:
        block.move(Block.LEFT);
      break;
      case 3:
      case 4:
      case 5:
        block.move(Block.RIGHT);
        break;
      case 6:
        block.turn();
        break;
      case 7:
        block.move(Block.DOWN);
        break;
    }
  }

  public void keyTyped(KeyEvent e) {
  }

  public void keyPressed(KeyEvent e) {
    int key = e.getKeyCode();

    if (key == KeyEvent.VK_LEFT) { // ブロックを左へ移動
      block.move(Block.LEFT);
    } else if (key == KeyEvent.VK_RIGHT) { // ブロックを右へ移動
      block.move(Block.RIGHT);
    } else if (key == KeyEvent.VK_DOWN) { // ブロックを下へ移動
      block.move(Block.DOWN);
      //            scorePanel.upScore(BLOCK_DOWN);
    } else if (key == KeyEvent.VK_SPACE || key == KeyEvent.VK_UP) { // ブロックを回転
      block.turn();
    }

    repaint();
  }

  public void keyReleased(KeyEvent e) {
  }

  /**
  * ランダムに次のブロックを作成
  *
  * @param field フィールドへの参照
  * @return ランダムに生成されたブロック
  */
  private Block createBlock(Field field) {
    // ZとSの形を多めにする
    int blockNo = rand.nextInt(11); // ブロックは0-6の7種類
    switch (blockNo) {
      case 0:
        return new BarBlock(field);
      case 1:
      case 2:
      case 3:
        return new ZShapeBlock(field);
      case 4:
        return new SquareBlock(field);
      case 5:
        return new LShapeBlock(field);
      case 6:
      case 7:
      case 8:
        return new ReverseZShapeBlock(field);
      case 9:
        return new TShapeBlock(field);
      case 10:
        return new ReverseLShapeBlock(field);
    }

    return null;
  }

  /**
  * ブロックのイメージをロード
  *
  * @param filename
  */
  private void loadImage(String filename) {
    // ブロックのイメージを読み込む
    ImageIcon icon = new ImageIcon(getClass().getResource(filename));
    blockImage = icon.getImage();
  }
}
