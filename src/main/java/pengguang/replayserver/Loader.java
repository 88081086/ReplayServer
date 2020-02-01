package pengguang.replayserver;

import java.io.*;
import java.util.HashMap;
import java.util.ArrayList;

public class Loader
{
    public String mFilename = null;
    public int mErrorCode = 0;

    public String mDate = "";
    public String mSite = "";
    public String mEvent = "";
    public String mRed = "";
    public String mBlack = "";
    public byte mResult = -1; /* -1: unknown, 0: black, 1: red, 2: draw */

    public int[] mInitData = null; 
    int mTurn = ChessState.RED; 
    int mChess = 0;
    public String mComment = null; /* replay global comment */

    InputStream mInputStream = null;
    Listener mListener = null;

    public ArrayList<HashMap<String, Object>> mList;

    public TreeNode<HashMap<String, Object>> mRoot = null;
    public TreeNode<HashMap<String, Object>> mCurrent = null;

    public Loader() {
        HashMap<String, Object> m = new HashMap<String, Object>();
        addTree(m);
    }

    public boolean load(Listener listener) {
        mListener = listener;
        return false;
    }

    public static Loader loadReplay(File file) {
        Loader loader = null;
        String name = file.getName();

        if (name.toLowerCase().endsWith("pgn")) {
            loader = new PgnLoader();
        } else if (name.toLowerCase().endsWith("xqr")) {
            loader = new XqrLoader();
        } else if (name.toLowerCase().endsWith("xqf")) {
            loader = new XqfLoader();
        } else {
            return null;
        }

        try {
            loader.mInputStream = new FileInputStream(file);
            loader.load((Listener) null);
        } catch (Exception e) {
            return null;
        }

        return loader;
    }

    public boolean save(Loader loader, String fullname) throws Exception {
        return false;
    }

    public static String saveReplay(Loader ldr, String fullname) throws Exception {
        return saveReplay(ldr, fullname, "GBK");
    }

    public static String saveReplay(Loader ldr, String fullname, String encoding) throws Exception {
        Loader loader = null;

        String name = fullname;
        if (ldr.mChess == 1 && !fullname.toLowerCase().endsWith(".xqr")) {
            name = fullname.replaceAll("\\.[^\\.]+$", ".xqr");
        } else if (fullname.toLowerCase().endsWith(".pgn")) {
            boolean hasBranch = false;
            TreeNode<HashMap<String, Object>> node = ldr.mRoot;
            while (node != null) {
                int num = node.children.size();
                if (num >= 1) {
                    if (num > 1) {
                        hasBranch = true;
                        break;
                    } else {
                        node = node.children.get(0);
                    }
                } else {
                    break;
                }
            }

            if (hasBranch) {
                name = fullname.replaceAll("\\.[^\\.]+$", ".xqr");
            }
        }

        if (name.toLowerCase().endsWith(".pgn")) {
            loader = new PgnLoader();
            ((PgnLoader) loader).mEncoding = encoding;
        } else if (name.toLowerCase().endsWith(".xqf")) {
            loader = new XqfLoader();
        } else {
            loader = new XqrLoader();
        }
        loader.save(ldr, name);
        return name;
    }

    void setInit(String init) {
        mInitData = PgnLoader.fromFen(init);
        mTurn = PgnLoader.getTurnFromFen(init);
    }

    void setInit(int[] init) {
        mInitData = init;
    }

    void addTree(HashMap<String, Object> m) {
        if (mRoot == null) {
            mRoot = mCurrent = new TreeNode(m);
        } else {
            mCurrent = mCurrent.addChild(m);
        }

        if (m.get("id") == null) {
            m.put("id", ""+(mRoot.count-1));
        }

        int num = mCurrent.getLevel()-1;
        m.put("num", String.format("%d%s", num/2+1, num%2==0?"A":"B"));
        //m.put("color", new Integer(num%2==0?0xffc0c0c0:0xffd0d0d0));
    }

    void updateProgress(String msg) {
        if (mListener != null) {
            mListener.updateProgress(msg);
        }
    }

    interface Listener {
        void updateProgress(String text);
    }
}

