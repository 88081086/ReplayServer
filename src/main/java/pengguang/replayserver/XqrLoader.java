package pengguang.replayserver;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.*;
import java.util.Arrays;
import java.io.*;

public class XqrLoader extends Loader
{
    static boolean DEBUG = false;

    static final int XQR_VERSION = 0;

    static final int TYPE_MAGIC = 0;
    static final int TYPE_VERSION = 1;
    static final int TYPE_EVENT = 2;
    static final int TYPE_SITE = 3;
    static final int TYPE_DATE = 4;
    static final int TYPE_RED = 5;
    static final int TYPE_BLACK = 6;
    static final int TYPE_RESULT = 7;
    static final int TYPE_INIT = 8;
    static final int TYPE_MOVE = 9;
    static final int TYPE_CRC = 10;
    static final int TYPE_TURN = 11;
    static final int TYPE_CHESS = 12;

    static final int HAS_CHILD = 0x1;
    static final int HAS_SIB = 0x2;
    static final int HAS_COMMENT = 0x4;

    static int FROM = 0;
    static int TO = 1;
    static int FLAG = 2;
    static int RSVD = 3;
    static int LEN = 4; /* 4 bytes */

    FileOutputStream mStream = null;

    public XqrLoader() {
        super();
    }

    public XqrLoader(String file)
    {
        mFilename = file;
    }

    public XqrLoader(InputStream in)
    {
        mInputStream = in;
    }

    @Override
    public boolean load(Listener listener) {
        super.load(listener);

        boolean status = true;

        try {
            if (mFilename != null) {
                mInputStream = new FileInputStream(mFilename);
            }

            mCRC = new CRC32();
            int total = mLen;
            if (mInputStream != null) total = mInputStream.available();

            mPos = 0;
            mRead = 0;
            if (mInputStream != null) {
                mLen = 0;
            } else {
                int i=0; 
                String tmp = "";
                for (i=0; i<mLen; ) {
                    tmp += String.format("%02x ", mBuf[i]);
                    if (i++%16==0) {
                        Log.d("chess", tmp);
                        tmp = "";
                    }
                }
            }

            /* check magic = 0x0, 0x2, 0x20, 0x17 */
            byte[] magic = new byte[4];
            getBytes(magic);
            if (!(magic[0] == 0x0 && magic[1] == 0x2 && magic[2] == 0x20 && magic[3] == 0x17)) {
                Log.d("chess", "invalid xqr format"+String.format(" %x/%x/%x/%x", magic[0], magic[1], magic[2], magic[3]));
                status = false;
            }

            while (status && mPos < mLen) {
                byte[] tl = new byte[2];
                if (!getBytes(tl)) {
                    status = false;
                    break;
                }
                int type = tl[0];
                int len = tl[1];
                byte[] buf = new byte[len];
                String val = null;
                if (len > 0) {
                    if (!getBytes(buf)) {
                        status = false;
                        break;
                    }
                    val = new String(buf);
                }

                if (type == TYPE_VERSION) {
                    byte ver = buf[0];
                    if (ver > XQR_VERSION) {
                        Log.d("chess", "xqr file version is too high: "+ver);
                        status = false;
                    }
                }

                if (type == TYPE_DATE) mDate = val;
                if (type == TYPE_SITE) mSite = val;
                if (type == TYPE_EVENT) mEvent= val;
                if (type == TYPE_RED) mRed = val;
                if (type == TYPE_BLACK) mBlack = val;
                if (type == TYPE_RESULT) mResult = buf[0];
                if (type == TYPE_TURN) mTurn = buf[0];
                if (type == TYPE_CHESS) mChess = buf[0];
                if (type == TYPE_INIT) {
                    mInitData = new int[len/2];
                    int i;
                    for (i=0; i<len/2; i++) {
                        mInitData[i] = ((buf[i*2+1]&0xff)<<8)|(buf[i*2+0]&0xff);
                    }
                }
                if (type == TYPE_MOVE) break;
            }

            ChessState state = new ChessState();
            mList = new ArrayList<HashMap<String, Object>>();
            ArrayList<HashMap<String, Object>> list = mList;
            if (mInitData != null) state.initBoard(mInitData, mTurn);

            boolean bJie = (mChess==1);
            int count = 0;
            boolean done = false;
            HashMap<String, Object> map = null;
            while (status && !done) {
                updateProgress(""+mRead*100/total+"%");

                byte[] mv = new byte[4];
                getBytes(mv);

                byte flag = mv[FLAG];
                String comment = null;
                if ((flag & HAS_COMMENT) != 0)
                {
                    byte len[] = new byte[4];
                    getBytes(len);
                    int commentLen = 0;
                    commentLen += ((int) len[0] & 0xff) << 0;
                    commentLen += ((int) len[1] & 0xff) << 8;
                    commentLen += ((int) len[2] & 0xff) << 16;
                    commentLen += ((int) len[3] & 0xff) << 24;

                    byte[] commentBytes = new byte[commentLen];
                    getBytes(commentBytes);
                    comment = new String(commentBytes, 0, commentLen);
                }

                if (DEBUG) {
                    Log.d("chess", String.format("from: %d, to: %d, flag: %2x, comment: %s", mv[FROM], mv[TO], mv[FLAG], comment==null?"null":comment)); 
                }

                if (count == 0) {
                    map = mRoot.data;
                    map.put("flag", flag);
                    map.put("id", ""+0);
                    map.put("comment", comment);
                    mComment = comment;

                    if ((flag & HAS_CHILD) == 0) {
                        done = true;
                    }
                } else {
                    map = new HashMap<String, Object>();
                    ChessRoute r = new ChessRoute((mv[FROM]>>4)&0xf, mv[FROM]&0xf, (mv[TO]>>4)&0xf, mv[TO]&0xf); 
                    map.put("route", r);
                    map.put("flag", mv[FLAG]);
                    if (comment != null) {
                        map.put("comment", comment);
                    }

                    String text = "";
                    if (!bJie) {
                        text = PgnLoader.getText(r, state);
                    }
                    map.put("step", text);
                    mCurrent = mCurrent.addChild(map);

                    int num = mCurrent.getLevel()-1;
                    map.put("num", String.format("%d%s", num/2+1, num%2==0?"A":"B"));
                    map.put("id", ""+count);

                    if ((flag & HAS_CHILD) == 0) {
                        do {
                            flag = (Byte) mCurrent.data.get("flag");
                            mCurrent = mCurrent.parent;
                            if (!bJie) state.retreat();
                        } while ((flag & HAS_SIB) == 0 && mCurrent != mRoot);

                        if ((flag & HAS_SIB) == 0) {
                            done = true;
                        }
                    }
                }

                count++;
            }

            if (status) {
                long exp = mCRC.getValue();

                byte[] tlv = new byte[6];
                if (getBytes(tlv) && tlv[0] == TYPE_CRC && tlv[1] == 4) {
                    long crc = 0;
                    crc += ((long) tlv[2] & 0xffL) << 0;
                    crc += ((long) tlv[3] & 0xffL) << 8;
                    crc += ((long) tlv[4] & 0xffL) << 16;
                    crc += ((long) tlv[5] & 0xffL) << 24;
                    if (crc != exp) {
                        Log.d("chess", "crc error");
                        status = false;
                    }
                } else {
                    Log.d("chess", "no crc");
                    status = false;
                }

                if (status) {
                    TreeNode<HashMap<String, Object>> node = mRoot.children.get(0);
                    for (; node != null; node = node.children.get(0)) {
                        list.add(node.data);
                        byte flag = (Byte) node.data.get("flag");
                        if ((flag & HAS_CHILD) == 0) {
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.d("chess", ""+e);
            e.printStackTrace();
            status = false;
        }

        return status;
    }
    
    public ChessRoute getRoute(String step, ChessState state)
    {
        return null;
    }

    public byte[] mBuf = new byte[64*1024];
    public int mPos = 0;
    public int mLen = 0;
    int mRead = 0;
    CRC32 mCRC = null;
    public boolean save(Loader loader) throws Exception {
        mCRC = new CRC32();

        mBuf[mPos++] = TYPE_MAGIC;
        mBuf[mPos++] = 2;
        mBuf[mPos++] = 0x20;
        mBuf[mPos++] = 0x17;

        mBuf[mPos++] = TYPE_VERSION;
        mBuf[mPos++] = 1;
        mBuf[mPos++] = XQR_VERSION;

        saveType(TYPE_EVENT, loader.mEvent);
        saveType(TYPE_DATE, loader.mDate);
        saveType(TYPE_SITE, loader.mSite);
        saveType(TYPE_RED, loader.mRed);
        saveType(TYPE_BLACK, loader.mBlack);

        mBuf[mPos++] = TYPE_RESULT;
        mBuf[mPos++] = 1;
        mBuf[mPos++] = loader.mResult;

        mBuf[mPos++] = TYPE_TURN;
        mBuf[mPos++] = 1;
        mBuf[mPos++] = (byte) loader.mTurn;

        mBuf[mPos++] = TYPE_CHESS;
        mBuf[mPos++] = 1;
        mBuf[mPos++] = (byte) loader.mChess;

        int[] init = loader.mInitData;
        if (init != null && !Arrays.equals(init, ChessState.INIT_DATA)) {
            mBuf[mPos++] = TYPE_INIT;
            mBuf[mPos++] = (byte) (init.length*2);
            int i;
            for (i=0; i<init.length; i++) {
                mBuf[mPos++] = (byte) (init[i]>>0&0xff);
                mBuf[mPos++] = (byte) (init[i]>>8&0xff);
            }
        }

        mBuf[mPos++] = TYPE_MOVE;
        mBuf[mPos++] = 0;

        saveNode(loader.mRoot);

        mCRC.update(mBuf, 0, mPos);

        long crc = mCRC.getValue();
        mBuf[mPos++] = TYPE_CRC;
        mBuf[mPos++] = 4;
        mBuf[mPos++] = (byte)((crc>>0)&0xff);
        mBuf[mPos++] = (byte)((crc>>8)&0xff);
        mBuf[mPos++] = (byte)((crc>>16)&0xff);
        mBuf[mPos++] = (byte)((crc>>24)&0xff);

        return true;
    }

    public boolean save(Loader loader, String fullname) throws Exception {
        mStream = new FileOutputStream(fullname);
        mPos = 0;

        save(loader);

        mStream.write(mBuf, 0, mPos);
        mPos = 0;
        mStream.close();
        return true;
    }

    void saveType(int tag, String s) throws Exception {
        if (s == null || s.length() == 0) return;
        int length = s.getBytes().length;
        mBuf[mPos++] = (byte) tag;
        mBuf[mPos++] = (byte) length;
        addBytes(s.getBytes(), length);
    }

    boolean getBytes(byte[] b) throws IOException {
        boolean ok = true;

        if (mPos + b.length > mLen) {
            System.arraycopy(mBuf, mPos, mBuf, 0, mLen-mPos);
            mLen = mLen-mPos;
            mPos = 0;

            if (mInputStream != null) {
                mLen += mInputStream.read(mBuf, mLen, mBuf.length-mLen);
                if (mPos + b.length > mLen) {
                    return !ok;
                }
            }
        }

        System.arraycopy(mBuf, mPos, b, 0, b.length);
        mPos += b.length;
        mRead += b.length;

        mCRC.update(b);

        return ok;
    }

    void addBytes(byte[] b) throws IOException {
        addBytes(b, b.length);
    }

    void addBytes(byte[] b, int length) throws IOException {
        if (b != null && length > 0) {
            if (mPos + length >= mBuf.length) {
                flush();
            }

            System.arraycopy(b, 0, mBuf, mPos, length);
            mPos += length;
        }
    }

    void flush() throws IOException {
        mCRC.update(mBuf, 0, mPos);
        mStream.write(mBuf, 0, mPos);
        mPos = 0;
    }

    void saveNode(TreeNode<HashMap<String, Object>> node) throws Exception {
        if (node == null) return;

        byte[] mv = new byte[8];

        int from = 0;
        int to = 0;
        if (node.getLevel() != 0) {
            ChessRoute rt = (ChessRoute) node.data.get("route");
            from = (rt.x1()<<4)|rt.y1();
            to = (rt.x2()<<4)|rt.y2();
        }
        mv[FROM] = (byte) from;
        mv[TO] = (byte) to;
        mv[RSVD] = 0;
        mv[FLAG] = 0;
        
        if (!node.isLeaf()) {
            mv[FLAG] |= HAS_CHILD;
        }

        TreeNode<HashMap<String, Object>> sib = node.getSibling();
        if (sib != null) {
            mv[FLAG] |= HAS_SIB;
        }

        String comment = (String) node.data.get("comment");
        if (comment != null && comment.length() > 0) {
            mv[FLAG] |= HAS_COMMENT;
        }

        if (comment != null && comment.length() > 0) {
            byte[] commentBytes = comment.getBytes();
            int commentLength = commentBytes.length;
            mv[LEN+0] = (byte) ((commentLength>>0)&0xff);
            mv[LEN+1] = (byte) ((commentLength>>8)&0xff);
            mv[LEN+2] = (byte) ((commentLength>>16)&0xff);
            mv[LEN+3] = (byte) ((commentLength>>24)&0xff);
            addBytes(mv);
            addBytes(commentBytes);
        } else {
            addBytes(mv, 4);
        }

        if (!node.isLeaf()) {
            saveNode(node.children.get(0));
        }

        if (sib != null) {
            saveNode(sib);
        }
    }
}
