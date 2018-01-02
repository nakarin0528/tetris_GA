


public class GA {


  public static final int MAX_GEN = 100;          //最大世代交替
  public static final int POP_SIZE = 10;          //集団のサイズ
  public static final int LEN_CHROM = 6;          //遺伝子の長さ
  public static final double GEN_GAP = 0.2;          //世代交替の割合
  public static final double P_MUTAION = 0.1;          //突然変異の確率
  public static final int RANDOM_MAX = 32767;
  public static final int BEFORE = 0;
  public static final int AFTER = 1;

  private int[][] chrom = new int[POP_SIZE][LEN_CHROM];   //染色体
  private int[] fitness = new int[POP_SIZE];            //適合度
  private int max, min, sumfitness;         //適合度のmax,min,sum
  private int n_min;



  /*------------------------------------------
    擬似乱数
   ------------------------------------------*/
  public long next = 1;

  public int Rand() {
    // todo: ↓のコードだとおかしくなる
    // next = next * 1103515245 + 12345;
    // return (int)(next/65536)%32768;
    next = (int)(Math.random()*10000);
    return (int)next;
  }

  public void Srand(int seed) {
    next = seed;
  }


  /*------------------------------------------
    データ表示関数
   ------------------------------------------*/
  public void PrintEachChromFitness(int i) {
    int j;
    System.out.printf("[%d] ",i);
    for (j=0; j<LEN_CHROM; j++) {
      System.out.printf("%d", chrom[i][j]);
    }
    System.out.printf(": %d\n", fitness[i]);
  }

  public void PrintChromFitness() {
    int i;
    for (i=0; i<POP_SIZE; i++) {
      PrintEachChromFitness(i);
    }
  }

  public void PrintStatistics(int gen) {
    System.out.printf("[gen=%2d] max=%d min=%d sumfitness=%d ave=%f\n",
                  gen, max, min, sumfitness, (double)sumfitness/(double)POP_SIZE);
  }

  public void PrintCrossover(int flag, int parent1, int parent2, int child1, int child2, int n_cross) {
    switch (flag) {
      case BEFORE:
        System.out.printf("parent1   |"); PrintEachChromFitness(parent1);
        System.out.printf("parent2   |"); PrintEachChromFitness(parent2);
        System.out.printf("delete1   |"); PrintEachChromFitness(child1);
        System.out.printf("delete2   |"); PrintEachChromFitness(child2);
        System.out.printf("n_cross=%d\n", n_cross);
        break;
      case AFTER:
        System.out.printf("child1   |"); PrintEachChromFitness(child1);
        System.out.printf("child2   |"); PrintEachChromFitness(child2);
        System.out.printf("----------------------------\n");
        break;
    }
  }

  public void PrintMutation(int flag, int child, int n_mutate) {
    switch (flag) {
      case BEFORE:
        System.out.printf("child(OLD)|"); PrintEachChromFitness(child);
        System.out.printf("n_mutate=%d\n", n_mutate);
        break;
      case AFTER:
        System.out.printf("child(NEW)|"); PrintEachChromFitness(child);
        System.out.printf("----------------------------\n");
        break;
    }
  }


  /*------------------------------------------
     突然変異
   ------------------------------------------*/
  public void Mutation(int child) {
    int n_mutate;
    double rand;

    rand = (double)Rand() / ((double)(RANDOM_MAX+1));   // 0<=num<1とする
    if (rand<P_MUTAION) {
      // 突然変異位置
      n_mutate = Rand()%LEN_CHROM;    // n_mutate=0,・・・,5
      // 突然変異
      PrintMutation(BEFORE, child, n_mutate);
      switch (chrom[child][n_mutate]) {
        case 0:
          chrom[child][n_mutate] = 1;
          break;
        case 1:
          chrom[child][n_mutate] = 0;
          break;
      }
      fitness[child] = ObjFunc(child);
      PrintMutation(AFTER, child, n_mutate);
    }
  }

  /*------------------------------------------
    fitnessの合計値の計算
   ------------------------------------------*/
  public void Statistics() {
    int i;

    max = 0;
    min = POP_SIZE;
    sumfitness = 0;

    for (i=0; i<POP_SIZE; i++) {
      if (fitness[i] > max) {
        max = fitness[i];
      }
      if (fitness[i] < min) {
        min = fitness[i];
        n_min = i;
      }
      sumfitness += fitness[i];
    }
  }


  /*------------------------------------------
     交叉
   ------------------------------------------*/
  public void Crossover(int parent1, int parent2, int child1, int child2) {
    int min2;
    int n_cross;
    int i,j;

    // 一番小さい値を子供としてセット
    child1 = n_min;
    // 2番目に小さい値を見つける
    min2 = POP_SIZE;
    for (i=0; i<POP_SIZE; i++) {
      if (i != child1) {
        if (min<=fitness[i] && fitness[i]<min2) {
          min2 = fitness[i];
          child2 = i;
        }
      }
    }

    // 交叉位置
    n_cross = Rand() % (LEN_CHROM-1) + 1;   // n_cross=1,・・・,5
    // 交叉
    PrintCrossover(BEFORE, parent1, parent2, child1, child2, n_cross);
    for (j=0; j<n_cross; j++) {
      chrom[child1][j] = chrom[parent1][j];
      chrom[child2][j] = chrom[parent2][j];
    }
    for (j=n_cross; j<LEN_CHROM; j++) {
      chrom[child1][j] = chrom[parent2][j];
      chrom[child2][j] = chrom[parent1][j];
    }
    fitness[child1] = ObjFunc(child1);
    fitness[child2] = ObjFunc(child2);
    PrintCrossover(AFTER, parent1, parent2, child1, child2, n_cross);
  }






  /*------------------------------------------
    1世代の処理
   ------------------------------------------*/
  public void Generation(int gen) {
    int parent1, parent2;
    int child1 = 0;
    int child2 = 0;
    int n_gen;
    int i;

    //集団の表示
    Statistics();
    PrintStatistics(gen);

    //世代交替
    n_gen = (int)((double)POP_SIZE * GEN_GAP/2.0);
    for (i=0; i<n_gen; i++) {
      Statistics();
      parent1 = Select();
      parent2 = Select();
      Crossover(parent1, parent2, child1, child2);
      Mutation(child1);
      Mutation(child2);
    }
  }


  /*------------------------------------------
    目的関数（1が多いほどGOOD！）
   ------------------------------------------*/
  public int ObjFunc(int i) {
    int j;
    int count = 0;

    for (j=0; j<LEN_CHROM; j++) {
      if (chrom[i][j] == 1) {
        count++;
      }
    }

    return count;
  }


  /*------------------------------------------
    選択
   ------------------------------------------*/
  public int Select() {
      int i;
      int sum;
      double rand;

      sum = 0;
      rand = (double)Rand() / ((double)(RANDOM_MAX+1));   // 0<=num<1とする

      for (i=0; i<POP_SIZE; i++) {
        sum += fitness[i];
        if ((double)sum/(double)sumfitness > rand) break;
      }
      return i;
  }


  /*------------------------------------------
    初期データ設定
   ------------------------------------------*/
  public void Initialize() {
    int i,j;

    for (i=0; i<POP_SIZE; i++) {
      for (j=0; j<LEN_CHROM; j++) {
        chrom[i][j] = Rand()%2;
      }
      fitness[i] = ObjFunc(i);
    }
    System.out.printf("First Population\n");
    PrintChromFitness();
    System.out.printf("----------------------------\n");
  }


  /*------------------------------------------
     メイン関数
   ------------------------------------------*/
  public static void main(String[] args) {
    int gen;

    GA ga = new GA();
    ga.Initialize();
    for (gen=1; gen<=MAX_GEN; gen++) {
      ga.Generation(gen);
    }
  }

}
