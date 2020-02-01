package pengguang.replayserver;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import java.util.HashMap;
import java.util.Random;

public class ChessState
{
    private static final String TAG = "chess";

    public static final int MOVE_ERROR = -2;
    public static final int NOT_FINISH = -1;
    
    public static final int TOP = 0;
    public static final int BOTTOM = 1;

    public static final int EMPTY = -1;

    public static final int JIANG = 0;
    public static final int SHI = 1;
    public static final int XIANG = 2;
    public static final int MA = 3;
    public static final int CHE = 4;
    public static final int PAO = 5;
    public static final int BING = 6;
    public static final int MAX_CATEGORY = 7;
    
    public static final int BLACK_JIANG = 4;
    public static final int RED_JIANG = 27;

    public static final int BLACK = 0;
    public static final int RED = 1;

    public static final int MAX_VALUE = 9999;
    public static final int MIN_VALUE = -9999;
    
    public static final int MAX_CHESS_COUNT = 32;
    public static final int MAX_ROW = 10;
    public static final int MAX_COL = 9;

    public static final int[] CAT = {
        CHE, MA, XIANG, SHI, JIANG, SHI, XIANG, MA, CHE, PAO, PAO, BING, BING, BING, BING, BING,
        BING, BING, BING, BING, BING, PAO, PAO, CHE, MA, XIANG, SHI, JIANG, SHI, XIANG, MA, CHE
    };

    public static final int[] V_ = new int[] {
        100, /* JIANG */
        5,   /* SHI */
        5,   /* XIANG */
        10,  /* MA */
        15,  /* CHE */
        12,  /* PAO */
        3,   /* BING */
    };

    public static final int[][] V = {
      { // jiang
         0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 
         0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 
         0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 
         0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 
         0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 
         0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 
         0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 
         0,  0,  0, 95, 96, 95,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 
         0,  0,  0, 98, 98, 98,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 
         0,  0,  0,100,100,100,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 
      }, { // shi
         0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 
         0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 
         0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 
         0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 
         0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 
         0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 
         0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 
         0,  0,  0,  5,  0,  5,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 
         0,  0,  0,  0,  6,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 
         0,  0,  0,  5,  0,  5,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 
      }, { // xiang
         0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
         0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
         0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
         0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
         0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
         0,  0,  5,  0,  0,  0,  5,  0,  0,  0,  0,  0,  0,  0,  0,  0,
         0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
         5,  0,  0,  0,  6,  0,  0,  0,  5,  0,  0,  0,  0,  0,  0,  0,
         0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
         0,  0,  5,  0,  0,  0,  5,  0,  0,  0,  0,  0,  0,  0,  0,  0,
      }, { // ma
        10, 10, 10, 11, 10, 11, 10, 10, 10,  0,  0,  0,  0,  0,  0,  0,
        10, 10, 15, 12, 10, 12, 15, 10, 10,  0,  0,  0,  0,  0,  0,  0,
        10, 10, 10, 15, 12, 15, 10, 10, 10,  0,  0,  0,  0,  0,  0,  0,
        11, 11, 11, 11, 11, 11, 11, 11, 11,  0,  0,  0,  0,  0,  0,  0,
        11, 11, 11, 11, 11, 11, 11, 11, 11,  0,  0,  0,  0,  0,  0,  0,
        11, 11, 11, 11, 11, 11, 11, 11, 11,  0,  0,  0,  0,  0,  0,  0,
        11, 11, 11, 11, 11, 11, 11, 11, 11,  0,  0,  0,  0,  0,  0,  0,
        11, 10, 11, 10, 10, 10, 11, 10, 11,  0,  0,  0,  0,  0,  0,  0,
        10, 10, 10, 10,  7, 10, 10, 10, 10,  0,  0,  0,  0,  0,  0,  0,
        10, 10, 10, 10, 10, 10, 10, 10, 10,  0,  0,  0,  0,  0,  0,  0,
      }, { // che
        16, 16, 16, 16, 16, 16, 16, 16, 16,  0,  0,  0,  0,  0,  0,  0,
        16, 16, 16, 16, 20, 16, 16, 16, 16,  0,  0,  0,  0,  0,  0,  0,
        16, 16, 16, 16, 16, 16, 16, 16, 16,  0,  0,  0,  0,  0,  0,  0,
        16, 16, 16, 16, 16, 16, 16, 16, 16,  0,  0,  0,  0,  0,  0,  0,
        16, 16, 16, 16, 16, 16, 16, 16, 16,  0,  0,  0,  0,  0,  0,  0,
        16, 16, 16, 16, 16, 16, 16, 16, 16,  0,  0,  0,  0,  0,  0,  0,
        15, 15, 15, 15, 15, 15, 15, 15, 15,  0,  0,  0,  0,  0,  0,  0,
        15, 15, 15, 15, 15, 15, 15, 15, 15,  0,  0,  0,  0,  0,  0,  0,
        15, 15, 15, 15, 15, 15, 15, 15, 15,  0,  0,  0,  0,  0,  0,  0,
        13, 15, 15, 15, 15, 15, 15, 15, 13,  0,  0,  0,  0,  0,  0,  0,
      }, { // pao
        14, 14, 14, 12, 12, 12, 14, 14, 14,  0,  0,  0,  0,  0,  0,  0,
        13, 13, 12, 12, 12, 12, 12, 13, 13,  0,  0,  0,  0,  0,  0,  0,
        13, 13, 12, 12, 12, 12, 12, 13, 13,  0,  0,  0,  0,  0,  0,  0,
        12, 12, 12, 12, 14, 12, 12, 12, 12,  0,  0,  0,  0,  0,  0,  0,
        12, 12, 12, 12, 14, 12, 12, 12, 12,  0,  0,  0,  0,  0,  0,  0,
        12, 12, 12, 12, 14, 12, 12, 12, 12,  0,  0,  0,  0,  0,  0,  0,
        12, 12, 12, 12, 13, 12, 12, 12, 12,  0,  0,  0,  0,  0,  0,  0,
        12, 12, 14, 12, 15, 12, 14, 12, 12,  0,  0,  0,  0,  0,  0,  0,
        12, 12, 12, 12, 12, 12, 12, 12, 12,  0,  0,  0,  0,  0,  0,  0,
        12, 12, 12, 12, 12, 12, 12, 12, 12,  0,  0,  0,  0,  0,  0,  0,
      }, { // bing
         5,  5,  5,  5,  5,  5,  5,  5,  5,  0,  0,  0,  0,  0,  0,  0,
         6,  6,  8,  9, 10,  9,  8,  6,  6,  0,  0,  0,  0,  0,  0,  0,
         6,  7,  8,  9,  9,  9,  8,  7,  6,  0,  0,  0,  0,  0,  0,  0,
         6,  6,  7,  7,  7,  7,  7,  6,  6,  0,  0,  0,  0,  0,  0,  0,
         5,  6,  6,  6,  6,  6,  6,  6,  5,  0,  0,  0,  0,  0,  0,  0,
         3,  0,  4,  0,  4,  0,  4,  0,  3,  0,  0,  0,  0,  0,  0,  0,
         3,  0,  3,  0,  4,  0,  3,  0,  3,  0,  0,  0,  0,  0,  0,  0,
         0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
         0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
         0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
      }
    };
    public static final int[] INIT_DATA = {
        0x0400, 0x0301, 0x0202, 0x0103, 0x0004, 0x0105, 0x0206, 0x0307, 0x0408,
                0x0521,                                         0x0527,
        0x0630,         0x0632,         0x0634,         0x0636,         0x0638,
        0x8660,         0x8662,         0x8664,         0x8666,         0x8668,
                0x8571,                                         0x8577,
        0x8490, 0x8391, 0x8292, 0x8193, 0x8094, 0x8195, 0x8296, 0x8397, 0x8498,
    };
    public static final int[] INIT_DATA_JIE = {
        0x7c00, 0x7b01, 0x7a02, 0x7903, 0x0004, 0x7905, 0x7a06, 0x7b07, 0x7c08,
                0x7d21,                                         0x7d27,
        0x7e30,         0x7e32,         0x7e34,         0x7e36,         0x7e38,
        0xfe60,         0xfe62,         0xfe64,         0xfe66,         0xfe68,
                0xfd71,                                         0xfd77,
        0xfc90, 0xfb91, 0xfa92, 0xf993, 0x8094, 0xf995, 0xfa96, 0xfb97, 0xfc98,
    };

    public static HashMap<Long, Long> mHash = new HashMap<Long, Long>();

    public int[][] mIndex = null;
    boolean mJiang = false;

    public ArrayList<ChessRoute> mRouteList = new ArrayList<ChessRoute>();

    /* best route */
    public ChessRoute mBestRoute = null;

    /* from route (parent route) */
    public ChessRoute mFromRoute = null;

    public ChessState mBestChild = null;

    /* the route take from its parent */
    ChessRoute mOrigRoute = null;

    /* value */
    public int mValue = MIN_VALUE;
    public int mPieceValue = 0;
    public int mParentValue = MAX_VALUE;

    /* turn, computer or user */
    public int mTurn = BOTTOM;
    public int mBottomColor = RED;
    public int mDepth = 0;

    public ArrayList<ChessRoute> mRoutes = null;

    public int[] mInitData = null;
    public int mInitTurn = RED; /* in color */

    public Zob zob = null;
    static Zob[][][] POOL = null;

    boolean mJie = false;

    static {
        Random r = new Random(System.currentTimeMillis());
        POOL = new Zob[15][10][9];
        int i, j, k;
        for (i=0; i<15; i++) {
            for (j=0; j<10; j++) {
                for (k=0; k<9; k++) {
                    POOL[i][j][k] = new Zob();
                    POOL[i][j][k].init(r);
                }
            }
        }
    }

    public ChessState()
    {
        mIndex = new int[10][9];
        int i, j;

        for (i=0; i<10; i++)
        {
            for (j=0; j<9; j++)
            {
                mIndex[i][j] = EMPTY;
            }
        }

        zob = new Zob();

        reset();
    }

    public ChessState(boolean jie)
    {
        mJie = jie;

        mIndex = new int[10][9];
        int i, j;

        for (i=0; i<10; i++)
        {
            for (j=0; j<9; j++)
            {
                mIndex[i][j] = EMPTY;
            }
        }

        zob = new Zob();

        reset();
    }
    public ChessState(ChessState state)
    {
        int i, j;

        zob = new Zob();

        mIndex = new int[10][9];
        for (i=0; i<10; i++) {
            for (j=0; j<9; j++) {
                mIndex[i][j] = state.mIndex[i][j];
                updateZob(i, j);
            }
        }

        mTurn = state.mTurn;
        zob.turn = mTurn;
        mDepth = state.mDepth;
        mRouteList = (ArrayList<ChessRoute>) state.mRouteList.clone();

        mBottomColor = state.mBottomColor;
        mJie = state.mJie;
    }

    void setJie(boolean jie) {
        mJie = jie;

        reset();
    }

    public void setTurn(int turn)
    {
        mTurn = turn;
    }

    public void setBottomColor(int color)
    {
        mBottomColor = color;
    }

    public void initBoard(String fen) {
        initBoard(fen, true);
    }

    public void initBoard(String fen, boolean save)
    {
        if (fen == null)
            return;

        int i, j, k, m;
        String fenRegex = "^([kabnrcpKABNRCP1-9]+/){9}[kabnrcpKABNRCP1-9]+/? +[wrb].*$";
        String[] fenTag = {
            "k", "a", "b", "n", "r", "c", "p",
        };

        if (!fen.matches(fenRegex)) {
            return;
        }

        String parts[] = fen.split(" ");
        String a[] = parts[0].split("/");
        if (a.length < 10) {
            return;
        }

        mTurn = BOTTOM;
        mBottomColor = RED;

        int count = 0;
        for (i=0; i<a.length; i++) {
            k = 0;
            for (j=0; j<a[i].length(); j++) {
                char c = a[i].charAt(j);
                if (c >= '1' && c <= '9') {
                    int repeat = c-'0';
                    while (repeat-- > 0) {
                        mIndex[i][k] = EMPTY;
                        k++;
                    }
                } else {
                    String tag = a[i].substring(j, j+1);
                    int color = (tag.equals(tag.toLowerCase())?0:1);
                    int cat = 0;
                    for (cat=0; cat<fenTag.length; cat++) {
                        if (tag.toLowerCase().equals(fenTag[cat])) {
                            break;
                        }
                    }
                    mIndex[i][k] = ((color<<7)|cat);
                    updateZob(i, k);
                    k++;

                    if (cat == JIANG && color == RED) {
                        if (i > 5) {
                            mBottomColor = RED;
                        } else {
                            mBottomColor = BLACK;
                        }
                    }
                }
            }
        }

        if (parts.length >= 2) {
            mTurn = parts[1].equals("b")?TOP:BOTTOM;
            if (mBottomColor != RED) {
                mTurn ^= 1;
            }
        }

        if (save) {
            mInitData = PgnLoader.fromFen(fen);
            mInitTurn = PgnLoader.getTurnFromFen(fen);
        }
    }

    public void initBoard(int[] data, int color) {
        initBoard(data, color, true);
    }

    public void initBoard(int[] data, int color, boolean save)
    {
        if (data == null)
            return;

        zob = new Zob();
        int i, j;
        for (i=0; i<10; i++) {
            Arrays.fill(mIndex[i], EMPTY);
        }

        for (i=0; i<data.length; i++) {
            int x = (data[i]>>4)&0xf;
            int y = (data[i]>>0)&0xf;
            mIndex[x][y] = (data[i]>>8)&0xff;
            updateZob(x, y);

            if (getColor(x, y) == RED) {
                mPieceValue -= getValue(x, y);
            } else {
                mPieceValue += getValue(x, y);
            }

            if (getCat(x, y)==JIANG) {
                if (x > 5) {
                    setBottomColor(getColor(x, y));
                } else {
                    setBottomColor(getColor(x, y)^1);
                }
            }

            int alt = (mIndex[x][y]>>3)&0x7;
            if (alt < MAX_CATEGORY && (mIndex[x][y]&0x40)!=0) {
                mJie = true;
            }

            if ((mIndex[x][y]&0x40)!=0) {
                mJie = true;
            }
        }

        if (mBottomColor != RED) {
            mPieceValue = 0-mPieceValue;
        }

        int turn = color;
        if (mBottomColor != RED) turn ^= 1;
        mTurn = turn;
        zob.turn = mTurn;

        if (save) {
            mInitData = new int[data.length];
            System.arraycopy(data, 0, mInitData, 0, data.length);
            mInitTurn = color;
        }
    }

    public int getColor(int turn)
    {
        if (turn == BOTTOM)
            return mBottomColor;
        else
            return mBottomColor^1;
    }

    public void swapSeat()
    {
        int r, c;
        for (r=0; r<=4; r++) {
            for (c=0; c<=8; c++) {
                int temp = mIndex[r][c];
                mIndex[r][c] = mIndex[9-r][8-c];
                mIndex[9-r][8-c] = temp;
            }
        }

        mBottomColor ^= 1;
        mTurn ^= 1;
    }

    public int[] prepareInitDataReverse()
    {
        int i, j;
        int[] temp = new int[32];
        int count = 0;

        for (i=0; i<MAX_ROW; i++) {
            for (j=0; j<MAX_COL; j++) {
                if (mIndex[i][j] == EMPTY) continue;
                temp[count] = (mIndex[i][j]<<8)|((9-i)<<4)|((8-j)<<0);
                count++;
            }
        }

        int[] init = new int[count];
        for (i=0; i<count; i++) {
            init[i] = temp[i];
        }

        return init;
    }

    public int[] prepareInitData()
    {
        int i, j;
        int[] temp = new int[32];
        int count = 0;

        for (i=0; i<MAX_ROW; i++) {
            for (j=0; j<MAX_COL; j++) {
                if (mIndex[i][j] == EMPTY) continue;
                temp[count] = (mIndex[i][j]<<8)|(i<<4)|(j<<0);
                count++;
            }
        }

        int[] init = new int[count];
        for (i=0; i<count; i++) {
            init[i] = temp[i];
        }

        return init;
    }

    public ChessState expand(ChessRoute route)
    {
        ChessState child = new ChessState(this);
        child.mDepth++;
        int won = child.go(route);
        child.mFromRoute = new ChessRoute(route);
        child.mBestRoute = null;
        child.mTurn = mTurn^1;

        if (won == getColor(TOP)) {
            child.mValue = MAX_VALUE;
        } else {
            child.mValue = MIN_VALUE;
        }

        if (mTurn == TOP) {
            child.mValue = MAX_VALUE+1;
            child.mParentValue = MIN_VALUE+1;
        } else {
            child.mValue = MIN_VALUE-1;
            child.mParentValue = MAX_VALUE-1;
        }

        return child;
    }

    public int go(ChessRoute route)
    {
        /*
        boolean rep = isRep(route);
        Log.d("chess", "rep="+rep);*/

        int x1 = route.x1();
        int y1 = route.y1();
        int x2 = route.x2();
        int y2 = route.y2();

        int index = mIndex[x1][y1];
        if (index == EMPTY) return MOVE_ERROR;

        ChessRoute step = new ChessRoute(route);
        step.eat(mIndex[x2][y2]);
        mRouteList.add(step);

        int v = getValue(x1, y1);
        if (getColor(x1, y1) == getColor(TOP)) {
            mPieceValue -= v;
        } else {
            mPieceValue += v;
        }

        updateZob(x1, y1);
        mIndex[x1][y1] = EMPTY;

        int eat = mIndex[x2][y2];
        if (eat != EMPTY) {
            updateZob(x2, y2);
            v = getValue(x2, y2);
            if (getColor(x2, y2) == getColor(TOP)) {
                mPieceValue -= v;
            } else {
                mPieceValue += v;
            }
        }

        mIndex[x2][y2] = index;

        if (!jied(x2, y2)) {
            jie(x2, y2);
            step.jie(true);
        }

        mTurn^=1;

        v = getValue(x2, y2);
        if (getColor(x2, y2) == getColor(TOP)) {
            mPieceValue += v;
        } else {
            mPieceValue -= v;
        }

        updateZob(x2, y2);
        zob.turn = mTurn;
        step.zob = new Zob(zob);

        return 0;
    }

    public int go1(ChessRoute route)
    {
        int before = new Rule().checkRule(new ChessState(this));

        int x1 = route.x1();
        int y1 = route.y1();
        int x2 = route.x2();
        int y2 = route.y2();

        int index = mIndex[x1][y1];
        if (index == EMPTY) return MOVE_ERROR;

        ChessRoute step = new ChessRoute(route);
        step.eat(mIndex[x2][y2]);
        mRouteList.add(step);

        int v = getValue(x1, y1);
        if (getColor(x1, y1) == getColor(TOP)) {
            mPieceValue -= v;
        } else {
            mPieceValue += v;
        }

        updateZob(x1, y1);
        mIndex[x1][y1] = EMPTY;

        int eat = mIndex[x2][y2];
        if (eat != EMPTY) {
            updateZob(x2, y2);
            v = getValue(x2, y2);
            if (getColor(x2, y2) == getColor(TOP)) {
                mPieceValue -= v;
            } else {
                mPieceValue += v;
            }
        }

        mIndex[x2][y2] = index;
        updateZob(x2, y2);

        if (!jied(x2, y2)) {
            jie(x2, y2);
            step.jie(true);
        }

        mTurn^=1;

        v = getValue(x2, y2);
        if (getColor(x2, y2) == getColor(TOP)) {
            mPieceValue += v;
        } else {
            mPieceValue -= v;
        }

        zob.turn = mTurn;
        step.zob = new Zob(zob);

        ChessState state = new ChessState(this);
        state.mTurn ^= 1;
        int after = new Rule().checkRule(state);
        int flag = 0;
        if ((after & 0x1) != 0) {
            flag = 0x1;
        } else if (((before & 0x2) == 0) && ((after & 0x2) != 0)) {
            flag = 0x2;
        }
        step.mRuleFlag = flag;

        return 0;
    }

    int getJiang(int turn) {
        int jiang = 0;
        if (turn == BOTTOM) {
            if (mBottomColor == RED) {
                jiang = RED_JIANG;
            } else {
                jiang = BLACK_JIANG;
            }
        } else {
            if (mBottomColor == RED) {
                jiang = BLACK_JIANG;
            } else {
                jiang = RED_JIANG;
            }
        }

        return jiang;
    }

    public boolean retreat() {
        int size = mRouteList.size();
        if (size > 0) {
            ChessRoute route = mRouteList.get(size-1);
            int x1 = route.x1();
            int y1 = route.y1();
            int x2 = route.x2();
            int y2 = route.y2();
            int index = mIndex[x2][y2];
            int v = getValue(x2, y2);
            if (getColor(x2, y2) == getColor(TOP)) {
                mPieceValue -= v;
            } else {
                mPieceValue += v;
            }

            updateZob(x2, y2);

            int eat = route.eat();
            mIndex[x2][y2] = eat;
            if (eat != EMPTY) {
                updateZob(x2, y2);
                v = getValue(x2, y2);
                if (getColor(x2, y2) == getColor(TOP)) {
                    mPieceValue += v;
                } else {
                    mPieceValue -= v;
                }
            }

            mIndex[x1][y1] = index;
            updateZob(x1, y1);

            if (route.jie()) {
                unJie(x1, y1);
            }

            v = getValue(x1, y1);
            if (getColor(x1, y1) == getColor(TOP)) {
                mPieceValue += v;
            } else {
                mPieceValue -= v;
            }


            mRouteList.remove(size-1);
            mTurn^=1;
            zob.turn = mTurn;

            if (mJiang) {
                mJiang = false;
            }

            return true;
        } else {
            return false;
        }
    }

    public void reset()
    {
        mTurn = BOTTOM;
        mBottomColor = RED;
        mRouteList.clear();
        zob = new Zob();

        if (mInitData != null) {
            initBoard(mInitData, mInitTurn);
        } else {
            if (mJie) {
                initBoard(INIT_DATA_JIE, RED);
            } else {
                initBoard(INIT_DATA, RED);
            }
        }
    }

    int JIANG_R[] = new int[] {1, -1, 0, 0};
    int JIANG_C[] = new int[] {0, 0, 1, -1};
    int SHI_R[] = new int[] {1, 1, -1, -1};
    int SHI_C[] = new int[] {1, -1, 1, -1};
    static int XIANG_R[] = new int[] {2, 2, -2, -2};
    static int XIANG_C[] = new int[] {2, -2, 2, -2};
    static int XIANG_P_R[] = new int[] {1, 1, -1, -1};
    static int XIANG_P_C[] = new int[] {1, -1, 1, -1};
    static int MA_R[] = new int[] {2, 1, -2, -1, 2, 1, -2, -1};
    static int MA_C[] = new int[] {1, 2, 1, 2, -1, -2, -1, -2};
    static int MA_P_R[] = new int[] {1, 0, -1, 0, 1, 0, -1, 0};
    static int MA_P_C[] = new int[] {0, 1, 0, 1, 0, -1, 0, -1};
    public boolean addRoute(int r1, int c1, int r2, int c2)
    {
        int eat = 0;
        int cat = (mIndex[r1][c1]&0x7);
        int color = (mIndex[r1][c1]>>7);
        int index = mIndex[r2][c2];
        if (index != EMPTY) {
            if (getColor(r1, c1) == getColor(r2, c2)) {
                return false;
            } else {
                eat = getValue(r2, c2);
                if (getCat(r2, c2) == JIANG) {
                    mJiang = true;
                }
            }
        }
        
        ChessRoute route = new ChessRoute(r1, c1, r2, c2);
        if (color == mBottomColor) {
            route.mValue = V[cat][r2<<4|c2]-V[cat][r1<<4|c1];
        } else {
            route.mValue = V[cat][(9-r2)<<4|c2]-V[cat][(9-r1)<<4|c1];
        }
        route.mValue += eat;

        int i = 0;
        int sz = mRoutes.size();
        while (i<sz) {
            if (route.mValue > mRoutes.get(i).mValue) break;
            i++;
        }
        mRoutes.add(i, route);

        return true;
    }

    int getColor(int r, int c) {
        return ((mIndex[r][c]>>7)&0x1);
    }
    int getCat(int r, int c) {
        return ((mIndex[r][c]>>0)&0x7);
    }
    int getCat1(int r, int c) {
        if (jied(r, c)) {
            return ((mIndex[r][c]>>0)&0x7);
        } else {
            return MAX_CATEGORY;
        }
    }
    int getHidden(int color, int cat) {
        int[][] HIDDEN = {
            {1, 2, 2, 2, 2, 2, 5},
            {1, 2, 2, 2, 2, 2, 5},
        };
        int hidden = HIDDEN[color][cat];
        int i;
        for (i=0; i<32; i++) {
            if (((mInitData[i]>>8)&0xb8) == (color<<7|cat<<3)) hidden--;
        }
        return hidden;
    }
    int getDead(int color, int cat) {
        if (mJie) return getDeadJie(color, cat);

        int[][] DEAD = {
            {1, 2, 2, 2, 2, 2, 5},
            {1, 2, 2, 2, 2, 2, 5},
        };
        int alive = 0;
        int i, j;
        for (i=0; i<MAX_ROW; i++) {
            for (j=0; j<MAX_COL; j++) {
                if (mIndex[i][j] != EMPTY) {
                    if (((mIndex[i][j]>>7)&0x1) == color && ((mIndex[i][j]>>0)&0x7) == cat) {
                        alive++;
                    }
                }
            }
        }

        return DEAD[color][cat]-alive;
    }
    int getDeadJie(int color, int cat) {
        int i;
        int dead = 0;
        for (i=0; i<mRouteList.size(); i++) {
            ChessRoute r = mRouteList.get(i);
            int eat = r.eat();
            if (eat != -1) {
                int COLOR = (eat>>7)&0x1;
                int CAT = 7;
                int hide = (eat>>6)&0x1;
                if (hide == 1) {
                    CAT = (eat>>3)&0x7;
                } else {
                    CAT = (eat>>0)&0x7;
                }

                if (color == COLOR && cat == CAT) dead++;
            }
        }

        return dead;
    }
    boolean empty(int r, int c) {
        return mIndex[r][c] == EMPTY;
    }
    int getValue(int r, int c) {
        int index = mIndex[r][c];
        if (index == EMPTY) return 0;

        int cat = getCat(r, c);
        if (!(cat >=0 && cat < MAX_CATEGORY)) return 0;

        int color = getColor(r, c);
        if (color != mBottomColor) {
            r = 9-r;
        }

        return V[cat][r<<4|c];
    }

    boolean jied(int r, int c) {
        return ((mIndex[r][c]>>6)&0x1) == 0;
    }

    void jie(int r, int c) {
        mIndex[r][c] &= ~(1<<6);
        int cur = (mIndex[r][c]&0x7);
        int alt = ((mIndex[r][c]>>3)&0x7);
        mIndex[r][c] = (mIndex[r][c]&0xc0)|(cur<<3)|(alt);
    }

    void unJie(int r, int c) {
        mIndex[r][c] |= (1<<6);
        int cur = (mIndex[r][c]&0x7);
        int alt = ((mIndex[r][c]>>3)&0x7);
        mIndex[r][c] = (mIndex[r][c]&0xc0)|(cur<<3)|(alt);
    }

    void clear() {
        int i;
        for (i=0; i<10; i++) {
            Arrays.fill(mIndex[i], EMPTY);
        }
    }

    void updateZob(int x, int y) {
        int index = mIndex[x][y];
        if (index != EMPTY) {
            int cat = getCat(x, y);
            int color = getColor(x, y);
            if (cat < MAX_CATEGORY) {
                zob.xor(POOL[cat*color+1][x][y]);
            }
        } else {
            zob.xor(POOL[0][x][y]);
        }
    }

    boolean goValid(ChessRoute r) {
        getRoutes();

        for (ChessRoute route: mRoutes) {
            if (route.eq(r)) return true;
        }

        return false;
    }

    public ArrayList<ChessRoute> getRoutes() {
        int i;
        int r, c;
        int row, col;

        //long start = System.currentTimeMillis();
        if (mRoutes == null) {
            mRoutes = new ArrayList<ChessRoute>();
        } else {
            mRoutes.clear();
        }

        int color = getColor(mTurn);
        for (row=0; row<MAX_ROW; row++) {
            for (col=0; col<MAX_COL; col++) {
                int minC = 0, maxC = 8;
                int minR = 0, maxR = 9;

                int index = mIndex[row][col];
                if (index == EMPTY) continue;
                if ((index>>7) != color) continue;

                int cat = (index&0x7);
                if (cat == ChessState.JIANG) {
                    minC = 3;
                    maxC = 5;
                    minR = 0;
                    maxR = 2;
                    if (color == getColor(ChessState.BOTTOM))
                    {
                        minR = 7;
                        maxR = 9;
                    }

                    for (i=0; i<JIANG_R.length; i++) {
                        r=row+JIANG_R[i];
                        c=col+JIANG_C[i];
                        if (!(r>=minR&&r<=maxR&&c>=minC&&c<=maxC)) continue;
                        addRoute(row, col, r, c);
                    }

                    c = col;
                    if (color == getColor(ChessState.TOP)) {
                        boolean found = false;
                        for (r = row+1; r<10; r++) {
                            if (!empty(r, c)) {
                                if (getCat(r, c) == JIANG) {
                                    found = true;
                                }
                                break;
                            }
                        }
                        if (found) addRoute(row, col, r, col);
                    } else {
                        boolean found = false;
                        for (r = row-1; r>=0; r--) {
                            if (!empty(r, c)) {
                                if (getCat(r, c) == JIANG) {
                                    found = true;
                                }
                                break;
                            }
                        }
                        if (found) addRoute(row, col, r, col);
                    }
                } else if (cat == ChessState.SHI) {
                    if (!mJie) {
                        minC = 3;
                        maxC = 5;
                        minR = 0;
                        maxR = 2;
                        if (color == getColor(ChessState.BOTTOM))
                        {
                            minR = 7;
                            maxR = 9;
                        }
                    }

                    for (i=0; i<SHI_R.length; i++) {
                        r=row+SHI_R[i];
                        c=col+SHI_C[i];
                        if (!(r>=minR&&r<=maxR&&c>=minC&&c<=maxC)) continue;
                        addRoute(row, col, r, c);
                    }
                } else if (cat == ChessState.XIANG) {
                    if (!mJie) {
                        minC = 0;
                        maxC = 8;
                        minR = 0;
                        maxR = 4;
                        if (color == getColor(ChessState.BOTTOM))
                        {
                            minR = 5;
                            maxR = 9;
                        }
                    }

                    for (i=0; i<XIANG_R.length; i++) {
                        r=row+XIANG_R[i];
                        c=col+XIANG_C[i];
                        int pin_r=row+XIANG_P_R[i];
                        int pin_c=col+XIANG_P_C[i];
                        if (!(r>=minR&&r<=maxR&&c>=minC&&c<=maxC)) continue;
                        if (!empty(pin_r, pin_c)) continue;
                        addRoute(row, col, r, c);
                    }
                } else if (cat == ChessState.MA) {
                    for (i=0; i<MA_R.length; i++) {
                        r=row+MA_R[i];
                        c=col+MA_C[i];
                        if (!(r>=minR&&r<=maxR&&c>=minC&&c<=maxC)) continue;
                        int pin_r=row+MA_P_R[i];
                        int pin_c=col+MA_P_C[i];
                        if (!empty(pin_r, pin_c)) continue;
                        addRoute(row, col, r, c);

                    }
                } else if (cat == ChessState.CHE) {
                    for (r = row+1; r<=maxR; r++) {
                        if (addRoute(row, col, r, col))  {
                            if (!empty(r, col)) break;
                        } else {
                            break;
                        }
                    }

                    for (r = row-1; r>=minR; r--) {
                        if (addRoute(row, col, r, col)) {
                            if (!empty(r, col)) break;
                        } else {
                            break;
                        }
                    }

                    for (c = col+1; c<=maxC; c++) {
                        if (addRoute(row, col, row, c)) { 
                            if (!empty(row, c)) break;
                        } else {
                            break;
                        }
                    }

                    for (c = col-1; c>=minC; c--) {
                        if (addRoute(row, col, row, c)) {
                            if (!empty(row, c)) break;
                        } else {
                            break;
                        }
                    }
                } else if (cat == ChessState.PAO) {
                    int skip = 0;
                    for (r = row+1; r<=maxR; r++) {
                        if (empty(r, col)) {
                            if (skip==0) addRoute(row, col, r, col);
                        } else {
                            skip++;
                            if (skip == 1) continue;
                            addRoute(row, col, r, col);
                            break;
                        }
                    }

                    skip = 0;
                    for (r = row-1; r>=minR; r--) {
                        if (empty(r, col)) {
                            if (skip==0) addRoute(row, col, r, col);
                        } else {
                            skip++;
                            if (skip == 1) continue;
                            addRoute(row, col, r, col);
                            break;
                        }
                    }

                    skip = 0;
                    for (c = col+1; c<=maxC; c++) {
                        if (empty(row, c)) {
                            if (skip==0) addRoute(row, col, row, c);
                        } else {
                            skip++;
                            if (skip == 1) continue;
                            addRoute(row, col, row, c);
                            break;
                        }
                    }

                    skip = 0;
                    for (c = col-1; c>=minC; c--) {
                        if (empty(row, c)) {
                            if (skip==0) addRoute(row, col, row, c);
                        } else {
                            skip++;
                            if (skip == 1) continue;
                            addRoute(row, col, row, c);
                            break;
                        }
                    }
                } else if (cat == ChessState.BING) {
                    if (color == getColor(ChessState.TOP))
                    {
                        r = row+1;
                        c = col;
                        if (r <= maxR) addRoute(row, col, r, c);

                        boolean inRemote = (row>=5);
                        if (inRemote)
                        {
                            r = row;
                            c = col+1;
                            if (c <= maxC) addRoute(row, col, r, c);

                            r = row;
                            c = col-1;
                            if (c >= minC) addRoute(row, col, r, c);
                        }
                    }
                    else
                    {
                        r = row-1;
                        c = col;
                        if (r >=minR) addRoute(row, col, r, c);

                        boolean inRemote = (row<=4);
                        if (inRemote)
                        {
                            r = row;
                            c = col+1;
                            if (c <= maxC) addRoute(row, col, r, c);

                            r = row;
                            c = col-1;
                            if (c >= minC) addRoute(row, col, r, c);
                        }
                    }
                }
            }
        }

        /*long elapsed = System.currentTimeMillis() - start;
        if (mDepth==0) Log.d("chess", "getRoutes() "+elapsed);*/
        return mRoutes;
    }

    public int getValue()
    {
        return mPieceValue;
    }

    long getCrc() {
        return 0;
    }

    void flip() {
        swapSeat();
    }

    void dump() {
        int i, j;
        String str = "---\n";
        for (i=0; i<10; i++) {
            for (j=0; j<9; j++) {
                str += String.format("%5d, ", mIndex[i][j]);
            }
            str += "\n";
        }
        Log.d("chess", str);
    }
}

