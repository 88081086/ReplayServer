package pengguang.replayserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.*;
import java.util.Random;
import java.util.Date;
import java.util.Arrays;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;

public class XqfLoader extends Loader
{
    static boolean DEBUG = false;
    static byte[] keyBytes = "[(C) Copyright Mr. Dong Shiwei.]".getBytes();

    int nArg0;
    int nXqfVer;

    byte mKeyXY;
    byte mKeyXYf;
    byte mKeyXYt;
    int mKeyRMKSize;
    int mKeyIndex = 0;
    int[] nKey = new int[32];
    byte[] nPiecePos = new byte[32];

    public XqfLoader() {
        super();
    }

    public XqfLoader(String file) {
        super();
        mFilename = file;
    }

    public XqfLoader(InputStream stream) {
        super();
        mInputStream = stream;
    }

    @Override
    public boolean load(Listener listener) {
        super.load(listener);

        boolean status = false;

        ChessState state = new ChessState();
        mList = new ArrayList<HashMap<String, Object>>();
        ArrayList<HashMap<String, Object>> list = mList;

        try {
            InputStream in = null;

            if (mFilename != null) {
                String zipExtension = ".zip";
                int index = mFilename.toLowerCase().indexOf(zipExtension);
                if (index == -1) {
                    in = new FileInputStream(mFilename);
                } else {
                    index += zipExtension.length();
                    String zipName = mFilename.substring(0, index);
                    index += 1;
                    String entryName = mFilename.substring(zipName.length()+1);

                    ZipFile z = new ZipFile(zipName);
                    ZipEntry e = z.getEntry(entryName);
                    in = z.getInputStream(e);
                }
            } else {
                in = mInputStream;
            }

            byte[] hdr = new byte[0x400];
            int totalRead = in.read(hdr);

            int i = 0;
            if (DEBUG) {
                String s = String.format("%04x - ", i);
                for (i=0; i<0x400; i++)
                {
                    s += String.format("%02x ", hdr[i]);
                    if ((i+1)%16 == 0)
                    {
                        Log.d("chess", s);
                        s = String.format("%04x - ", i+1);
                    }
                }
            }

            if (hdr[Signature+0] != 'X' || hdr[Signature+1] != 'Q')
            {
                Log.d("chess", "Invalid XQF format");
                return false;
            }

            nXqfVer = hdr[Version];
            if (DEBUG) Log.d("chess", String.format("xqf ver: %d", nXqfVer));

            if (nXqfVer < 11)
            {
                mKeyXY = mKeyXYf = mKeyXYt = 0;
                mKeyRMKSize = 0;
                for (i = 0; i < 32; i++)
                    nKey[i] = 0;
            }
            else
            {
                mKeyXY = (byte) (Square54Plus221((byte) hdr[KeyXY]) * (byte) hdr[KeyXY]);
                mKeyXYf = (byte) (Square54Plus221((byte) hdr[KeyXYf]) * mKeyXY);
                mKeyXYt = (byte) (Square54Plus221((byte) hdr[KeyXYt]) * mKeyXYf);
                mKeyRMKSize = (((hdr[KeysSum]<<8)&0xffff) | (hdr[KeyXY]&0xff)) % 32000 + 767;
                nArg0 = hdr[KeyMask];
                byte[] seed = new byte[4];
                for (i=0; i<4; i++)
                {
                    seed[i] = (byte) (hdr[KeyOrA+i] | (hdr[KeysSum+i] & nArg0));
                }
                for (i=0; i<32; i++)
                {
                    nKey[i] = (byte) (seed[i%4] & keyBytes[i]);
                }
                if (DEBUG) {
                    Log.d("chess", String.format("mKeyXY: %d", mKeyXY&0xff));
                    Log.d("chess", String.format("mKeyXYf: %d", mKeyXYf&0xff));
                    Log.d("chess", String.format("mKeyXYt: %d", mKeyXYt&0xff));
                    Log.d("chess", String.format("mKeyRMKSize: %d", mKeyRMKSize));
                    Log.d("chess", "seed: ");
                    String s = "";
                    for (i=0; i<4; i++) s += String.format("%2x ", seed[i]&0xff);
                    Log.d("chess", s);
                    Log.d("chess", "nKey: ");
                    s = "";
                    for (i=0; i<32; i++) s += String.format("%2x ", nKey[i]&0xff);
                    Log.d("chess", s);
                }
            }
            mKeyIndex = 0;

            if (DEBUG) Log.d("chess", String.format("setup0: %d", hdr[CodeA]));
            boolean checkCodeA = false;
            if (hdr[CodeA] < 2 && checkCodeA) {
                Log.d("chess", "no initial set up");
            } else {
                if (nXqfVer < 12) {
                    for (i=0; i<32; i++)
                        nPiecePos[i] = (byte) (hdr[QiziXY+i] - mKeyXY);
                } else {
                    for (i=0; i<32; i++)
                        nPiecePos[((mKeyXY&0xff) + 1 + i) % 32] = (byte) (hdr[QiziXY+i] - mKeyXY);
                }

                if (DEBUG) {
                    String s = "";
                    for (i=0; i<32; i++)
                        s += String.format("%02x ", nPiecePos[i]);
                    Log.d("chess", s);
                }

                mInitData = getInitData(nPiecePos);

                if (mInitData != null) {
                    state.initBoard(mInitData, 1);
                }
            }

            String s = null;
            if (DEBUG) {
                Log.d("chess", String.format("%20s - %s", "Title", new String(hdr, TitleA+1, hdr[TitleA]&0xff, "GB2312")));
                Log.d("chess", String.format("%20s - %s", "Event", new String(hdr, MatchName+1, hdr[MatchName]&0xff, "GB2312")));
                Log.d("chess", String.format("%20s - %s", "Date", new String(hdr, MatchTime+1, hdr[MatchTime]&0xff, "GB2312")));
                Log.d("chess", String.format("%20s - %s", "Site", new String(hdr, MatchAddr+1, hdr[MatchAddr]&0xff, "GB2312")));
                Log.d("chess", String.format("%20s - %s", "Red", new String(hdr, RedPlayer+1, hdr[RedPlayer]&0xff, "GB2312")));
                Log.d("chess", String.format("%20s - %s", "Black", new String(hdr, BlkPlayer+1, hdr[BlkPlayer]&0xff, "GB2312")));
                Log.d("chess", String.format("%20s - %s", "RuleTime", new String(hdr, TimeRule+1, hdr[TimeRule]&0xff, "GB2312")));
                Log.d("chess", String.format("%20s - %s", "RedTime", new String(hdr, RedTime+1, hdr[RedTime]&0xff, "GB2312")));
                Log.d("chess", String.format("%20s - %s", "BlackTime", new String(hdr, BlkTime+1, hdr[BlkTime]&0xff, "GB2312")));
                Log.d("chess", String.format("%20s - %s", "Annotator", new String(hdr, RMKWriter+1, hdr[RMKWriter]&0xff, "GB2312")));
                Log.d("chess", String.format("%20s - %s", "Author", new String(hdr, Author+1, hdr[Author]&0xff, "GB2312")));
            }

            mEvent = new String(hdr, MatchName+1, hdr[MatchName]&0xff, "GB2312");
            mDate = new String(hdr, MatchTime+1, hdr[MatchTime]&0xff, "GB2312");
            mSite = new String(hdr, MatchAddr+1, hdr[MatchAddr]&0xff, "GB2312");
            mRed = new String(hdr, RedPlayer+1, hdr[RedPlayer]&0xff, "GB2312");
            mBlack = new String(hdr, BlkPlayer+1, hdr[BlkPlayer]&0xff, "GB2312");
            mResult = (byte) (hdr[PlayResult]&0xff);
            if (mResult == 1) {
                mResult = 1;
            } else if (mResult == 2) {
                mResult = 0;
            } else if (mResult == 3) {
                mResult = 2;
            } else {
                mResult = -1;
            }

            mTurn = (byte) (hdr[WhoPlay]&0xff); /* 0: red, 1: black */
            if (DEBUG) Log.d("chess", "read WhoPlay="+mTurn);
            mTurn ^= 1;

            int turn = mTurn;
            if (state.mBottomColor != ChessState.RED) {
                turn ^= 1;
            }
            state.setTurn(turn);

            byte src, dst;
            int count = 0; /* 0 to skip 1st record */
            boolean bHasNext = true;
            HashMap <String, Object> map;
            int avail = in.available();
            while (bHasNext) {
                updateProgress(""+(avail-in.available())*100/avail+"%");

                byte[] mv = new byte[4];
                if (in.read(mv) < 4) break;
                src = (byte) (decode(mv[0]) - mKeyXYf - 24);
                dst = (byte) (decode(mv[1]) - mKeyXYt - 32);

                byte tag = decode(mv[2]);

                if (nXqfVer <= 10) {
                    byte b = 0;
                    if ((tag & 0xf0) != 0) b |= 0x80;
                    if ((tag & 0x0f) != 0) b |= 0x40;
                    tag = b;
                }

                decode(mv[3]); /* skip reserved */
                int commentLen = 0;
                byte[] comment = null;
                if (nXqfVer<=10 || (tag & 0x20) != 0)
                {
                    byte raw[] = new byte[4];
                    byte len[] = new byte[4];
                    if (in.read(raw) < 4) break;
                    len[0] = decode(raw[0]);
                    len[1] = decode(raw[1]);
                    len[2] = decode(raw[2]);
                    len[3] = decode(raw[3]);
                    commentLen = ((len[0]&0xff)<<0) | ((len[1]&0xff)<<8) | ((len[2]&0xff)<<16) | ((len[3]&0xff)<<24);
                    commentLen -= mKeyRMKSize;

                    if (commentLen > 1024*1024) {
                        break;
                    }

                    comment = new byte[commentLen];
                    if (in.read(comment) < commentLen) break;
                    for (i=0; i<commentLen; i++) 
                    {
                        comment[i] = decode(comment[i]);
                    }
                }

                if (DEBUG) {
                    Log.d("chess", String.format("src: %d, dst: %d, route: %s, tag: %2x, commentLen: %d[%s]", 
                        src&0xff, dst&0xff, getRoute(src, dst), tag, commentLen, 
                        commentLen>0 ? new String(comment, 0, commentLen, "GB2312").replaceAll("\\r\\n", "") : ""));
                }

                if (count != 0) {
                    map = new HashMap<String, Object>();

                    ChessRoute r = getRoute(src, dst); 
                    if (r != null) {
                        /* get game turn */
                        if (count == 1) {
                            mTurn = state.getColor(r.x1(), r.y1());

                            turn = mTurn;
                            if (state.mBottomColor != ChessState.RED) {
                                turn ^= 1;
                            }
                            state.setTurn(turn);
                        }

                        map.put("route", getRoute(src, dst));
                        map.put("tag", tag);
                        if (commentLen > 0) {
                            map.put("comment", new String(comment, 0, commentLen, "GB2312"));
                        }
                        list.add(map);
                        
                        tag = ((Byte) mCurrent.data.get("tag"));
                        if ((tag & 0x80) != 0) {
                            String text = PgnLoader.getText(getRoute(src, dst), state);
                            map.put("step", text);
                            mCurrent = mCurrent.addChild(map);
                        } else {
                            while (((tag = ((Byte) mCurrent.data.get("tag"))) & 0x40) == 0) {
                                mCurrent = mCurrent.parent;
                                state.retreat();
                            }
                            mCurrent = mCurrent.parent;
                            state.retreat();

                            String text =  PgnLoader.getText(getRoute(src, dst), state);
                            map.put("step", text);
                            mCurrent = mCurrent.addChild(map);
                        }
                        int num = mCurrent.getLevel()-1;
                        map.put("num", String.format("%d%s", num/2+1, num%2==0?"A":"B"));
                        map.put("id", ""+count);
                    } else {
                        bHasNext = false;
                    }
                } else {
                    HashMap<String, Object> m = mRoot.data;
                    m.put("tag", tag);
                    m.put("id", ""+count);

                    if (commentLen > 0) {
                        mComment = new String(comment, 0, commentLen, "GB2312");
                        m.put("comment", mComment);
                    }
                }
                count++;
            }

            status = true;
        } catch (Exception e) {
            Log.d("chess", "e: "+e);
            e.printStackTrace();
        }

        return status;
    }

    public ChessRoute getRoute(byte src, byte dst)
    {
        int sx = 9-(src&0xff)%10, sy = (src&0xff)/10;
        int dx = 9-(dst&0xff)%10, dy = (dst&0xff)/10;
        if (sx >= ChessState.MAX_ROW ||
            dx >= ChessState.MAX_ROW ||
            sy >= ChessState.MAX_COL ||
            dy >= ChessState.MAX_COL) {
            return null;
        }

        return new ChessRoute(sx, sy, dx, dy);
    }

    public static void main(String[] args)
    {
        XqfLoader loader = new XqfLoader(args[0]);
        loader.load(null);
    }

    int Square54Plus221(int x)
    {
        return x*x*54 + 221;
    }

    byte encode(byte a) {
        byte r = (byte) (a+nKey[mKeyIndex]);
        mKeyIndex = (mKeyIndex+1) % 32;
        return r;
    }

    byte decode(byte raw)
    {
        byte ret = (byte) (raw - nKey[mKeyIndex]);
        mKeyIndex = (mKeyIndex+1) % 32;
        return ret;
    }

    int[] getInitData(byte[] pos)
    {
        int i, j;
        int count = 0;
        int[] temp = new int[32];
        int[] CAT = {
            4, 3, 2, 1, 0, 1, 2, 3, 4, 5, 5, 6, 6, 6, 6, 6,
            4, 3, 2, 1, 0, 1, 2, 3, 4, 5, 5, 6, 6, 6, 6, 6,
        };
        for (i=0; i<32; i++) {
            int p = transformPoint(pos[i]);
            if (p != -1) {
                int color = 1;
                if (i >= 16) color = 0;
                temp[count++] = (color<<15)|(CAT[i]<<8)|(p/10<<4)|(p%10);
            }
        }
        /*
        for (i=16, j=0; i<32; i++, j++) {
            int p = transformPoint(pos[i]);
            if (p != -1) {
                temp[count++] = (CAT[j]<<8)|(p/10<<4)|(p%10);
            }
        }
        for (i=15, j=0; i>=0; i--, j++) {
            int p = transformPoint(pos[i]);
            if (p != -1) {
                temp[count++] = (1<<15)|(CAT[j]<<8)|(p/10<<4)|(p%10);
            }
        }*/

        int[] init = new int[count];
        for (i=0; i<count; i++) {
            init[i] = temp[i];
        }
        return init;
    }

    int transformPoint(byte pos)
    {
        int newPos = -1;
        int i = pos&0xff;
        if (i >= 0 && i < 90)
        {
            int x = 9-(i&0xff)%10;
            int y = (i%0xff)/10;
            newPos = x*10+y;
        }

        return newPos;
    }

    byte[] getBytesWithLimit(String s, int maxLen) throws Exception {
        if (s == null) return null;

        int len = s.length();
        byte[] b = s.substring(0, len).getBytes("GB2312");
        while (b.length > maxLen) {
            b = s.substring(0, --len).getBytes("GB2312");
        }

        return b;
    }

    public boolean save(Loader loader, String fullname) throws Exception {
        FileOutputStream fos = new FileOutputStream(fullname);

        Random r = new Random();
        byte[] hdr = new byte[1024];
        hdr[Signature+0] = 'X';
        hdr[Signature+1] = 'Q';
        hdr[Version] = 18;
        byte result = loader.mResult;
        if (result == 0) {
            result = 2;
        } else if (result == 1) {
            result = 1;
        } else if (result == 2) {
            result = 3;
        } else {
            result = 0;
        }
        hdr[PlayResult] = result;
        hdr[KeyMask] = (byte)((r.nextInt()%256)|0xaa);
        hdr[KeyOrA] = (byte)(r.nextInt()%256);
        hdr[KeyOrB] = (byte)(r.nextInt()%256);
        hdr[KeyOrC] = (byte)(r.nextInt()%256);
        hdr[KeyOrD] = (byte)(hdr[KeyOrA]+hdr[KeyOrB]+hdr[KeyOrC]);
        hdr[KeyOrD] = (byte)(256-hdr[KeyOrD]);

        byte b = 0;
        hdr[KeyXY] = (byte)((r.nextInt()%254)+1);
        b = (byte)(b+hdr[KeyXY]);
        hdr[KeyXYf] = (byte)((r.nextInt()%254)+1);
        b = (byte)(b+hdr[KeyXYf]);
        hdr[KeyXYt] = (byte)((r.nextInt()%254)+1);
        b = (byte)(b+hdr[KeyXYt]);
        hdr[KeysSum] = (byte)(256-b);

        byte bKey = 0;
        bKey = hdr[KeyXY];
        mKeyXY = (byte)((((((bKey*bKey)*3+9)*3+8)*2+1)*3+8) * bKey);
        bKey = hdr[KeyXYf];
        mKeyXYf = (byte)((((((bKey*bKey)*3+9)*3+8)*2+1)*3+8) * mKeyXY);
        bKey = hdr[KeyXYt];
        mKeyXYt = (byte)((((((bKey*bKey)*3+9)*3+8)*2+1)*3+8) * mKeyXYf);
        /*
        short wKey = (short)( (hdr[KeysSum]) * 256 + hdr[KeyXY]);
        mKeyRMKSize = ((wKey%32000) + 767);*/
        mKeyRMKSize = (((hdr[KeysSum]<<8)&0xffff) | (hdr[KeyXY]&0xff)) % 32000 + 767;
        if (DEBUG) Log.d("chess", String.format("offset=%d/%d/%d/%d", mKeyXY&0xff, mKeyXYf&0xff, mKeyXYt&0xff, mKeyRMKSize));

        ChessState state = new ChessState();
        if (loader.mInitData != null) {
            state.initBoard(loader.mInitData, loader.mTurn);
        }

        int turn = loader.mTurn;
        if (state.mBottomColor != ChessState.RED) {
            turn ^= 1;
        }
        state.setTurn(turn);

        byte who = (byte) loader.mTurn;
        who ^= 1;
        hdr[WhoPlay] = who;
        if (DEBUG) Log.d("chess", "write WhoPlay="+hdr[WhoPlay]);

        int i;
        byte[] xy = new byte[32];
        int[] cat = {
            4, 3, 2, 1, 0, 1, 2, 3, 4, 5, 5, 6, 6, 6, 6, 6,
            4, 3, 2, 1, 0, 1, 2, 3, 4, 5, 5, 6, 6, 6, 6, 6,
        };
        int[] used = new int[32];
        Arrays.fill(used, 0, 31, 0);
        int x, y;
        for (x = 0; x<10; x++) {
            for (y = 0; y<9; y++) {
                if (state.mIndex[x][y] == ChessState.EMPTY) continue;
                int color = state.getColor(x, y);
                color ^= 1;
                for (i=color*16; i<(color+1)*16; i++) {
                    if (cat[i] == state.getCat(x, y) && used[i] == 0) {
                        break;
                    }
                }
                /* 0-31
                 * 红: 车马相士帅士相马车炮炮兵兵兵兵兵
                 * 黑: 车马象士将士象马车炮炮卒卒卒卒卒
                 */

                /* 十位数为X(0-8)个位数为Y(0-9) */
                xy[i] = (byte)((y)*10+(9-x));
                xy[i] += mKeyXY;
                used[i] = 1;
            }
        }

        for (i=0; i<32; i++) {
            if (used[i] == 0) {
                xy[i] = 90;
                xy[i] += mKeyXY;
            }
        }

        for (i=0; i<32; i++) {
            hdr[QiziXY+i] = xy[((mKeyXY&0xff)+i+1)%32];
        }

        String s = loader.mEvent;
        byte[] bytes = null;
        if (s != null && s.length() > 0) {
            bytes = getBytesWithLimit(s, 64-1);
            hdr[MatchName] = (byte) bytes.length;
            System.arraycopy(bytes, 0, hdr, MatchName+1, bytes.length);
        }

        s = loader.mDate;
        if (s != null && s.length() > 0) {
            bytes = getBytesWithLimit(s, 16-1);
            hdr[MatchTime] = (byte) bytes.length;
            System.arraycopy(bytes, 0, hdr, MatchTime+1, bytes.length);
        }

        s = loader.mSite;
        if (s != null && s.length() > 0) {
            bytes = getBytesWithLimit(s, 16-1);
            hdr[MatchAddr] = (byte) bytes.length;
            System.arraycopy(bytes, 0, hdr, MatchAddr+1, bytes.length);
        }

        s = loader.mRed;
        if (s != null && s.length() > 0) {
            bytes = getBytesWithLimit(s, 16-1);
            hdr[RedPlayer] = (byte) bytes.length;
            System.arraycopy(bytes, 0, hdr, RedPlayer+1, bytes.length);
        }

        s = loader.mBlack;
        if (s != null && s.length() > 0) {
            bytes = getBytesWithLimit(s, 16-1);
            hdr[BlkPlayer] = (byte) bytes.length;
            System.arraycopy(bytes, 0, hdr, BlkPlayer+1, bytes.length);
        }

        fos.write(hdr);

        byte[] seed = new byte[4];
        seed[0] = (byte)((hdr[KeysSum] & hdr[KeyMask]) | hdr[KeyOrA]);
        seed[1] = (byte)((hdr[KeyXY]   & hdr[KeyMask]) | hdr[KeyOrB]);
        seed[2] = (byte)((hdr[KeyXYf]  & hdr[KeyMask]) | hdr[KeyOrC]);
        seed[3] = (byte)((hdr[KeyXYt]  & hdr[KeyMask]) | hdr[KeyOrD]);

        for (i=0; i<32; i++) {
            nKey[i] = (byte)(keyBytes[i] & seed[i%4]);
        }

        if (DEBUG) {
            Log.d("chess", "seed: ");
            s = "";
            for (i=0; i<4; i++) s += String.format("%2x ", seed[i]&0xff);
            Log.d("chess", s);
            Log.d("chess", "nKey: ");
            s = "";
            for (i=0; i<32; i++) s += String.format("%2x ", nKey[i]&0xff);
            Log.d("chess", s);
        }

        TreeNode<HashMap<String, Object>> node = loader.mRoot;
        saveNode(fos, node);

        return true;
    }

    void saveNode(FileOutputStream fos, TreeNode<HashMap<String, Object>> node) throws Exception {
        byte[] mv = new byte[8];

        int x1 = 0;
        int y1 = 0;
        int x2 = 0;
        int y2 = 0;
        if (node.getLevel() != 0) {
            ChessRoute rt = (ChessRoute) node.data.get("route");
            x1 = rt.x1();
            y1 = rt.y1();
            x2 = rt.x2();
            y2 = rt.y2();
        }
        byte from = (byte)((9-x1)+y1*10);
        mv[FROM] = encode((byte)(from+24+mKeyXYf));
        byte to = (byte)((9-x2)+y2*10);
        mv[TO] = encode((byte)(to+32+mKeyXYt));

        //mv[TAG] = (byte) (r.nextInt() & 0x1f);
        mv[TAG] = 0;
        
        if (!node.isLeaf()) {
            mv[TAG] |= 0x80;
        }

        TreeNode<HashMap<String, Object>> sib = node.getSibling();
        if (sib != null) {
            mv[TAG] |= 0x40;
        }

        String comment = (String) node.data.get("comment");
        if (comment != null) {
            mv[TAG] |= 0x20;
        }

        mv[TAG] = encode(mv[TAG]);
        mv[RSVD] = encode(mv[RSVD]);

        int commentLength = 0;
        if (comment != null) {
            byte[] commentBytes = comment.getBytes("GB2312");
            int len = commentBytes.length;
            commentLength = len;
            commentLength += mKeyRMKSize;
            mv[LEN+0] = encode( (byte) ((commentLength>>0)&0xff));
            mv[LEN+1] = encode( (byte) ((commentLength>>8)&0xff));
            mv[LEN+2] = encode( (byte) ((commentLength>>16)&0xff));
            mv[LEN+3] = encode( (byte) ((commentLength>>24)&0xff));
            fos.write(mv);

            byte[] commentBytesNew = new byte[len];
            int i;
            for (i=0; i<len; i++) {
                commentBytesNew[i] = encode(commentBytes[i]);
            }
            fos.write(commentBytesNew);
        } else {
            fos.write(mv, 0, 4);
        }

        if (!node.isLeaf()) {
            saveNode(fos, node.children.get(0));
        }

        if (sib != null) {
            saveNode(fos, sib);
        }
    }

    int FROM = 0;
    int TO = 1;
    int TAG = 2;
    int RSVD = 3;
    int LEN = 4; /* 4 bytes */

    int Signature = 0;
    int Version = 2;
    int KeyMask = 3;
    int ProductId = 4;
    int KeyOrA = 8;
    int KeyOrB = 9;
    int KeyOrC = 10;
    int KeyOrD = 11;
    int KeysSum = 12;
    int KeyXY = 13;
    int KeyXYf = 14;
    int KeyXYt = 15;

    // = 16 bytes
    int QiziXY = 16;
    // = 48 bytes
    int PlayStepNo = 48;
    int WhoPlay = 50;
    int PlayResult = 51;
    int PlayNodes = 52;
    int PTreePos = 56;
    int Reserved1 = 60;
    // = 64 bytes
    int CodeA = 64;
    int CodeB = 66;
    int CodeC = 68;
    int CodeD = 70;
    int CodeE = 72;
    int CodeF = 74;
    int CodeH = 76;
    int CodeG = 78;
    // = 80  bytes
    int TitleA = 80;
    int TitleB = 144;
    // = 208 bytes
    int MatchName = 208;
    int MatchTime = 208+64;
    int MatchAddr = 208+64+16;
    int RedPlayer = 208+64+16+16;
    int BlkPlayer = 208+64+16+16+16;
    // = 336 bytes
    int TimeRule = 336;
    int RedTime = 336+64;
    int BlkTime = 336+64+16;
    int Reservedh = 336+64+16+16;
    // = 464 bytes
    int RMKWriter = 464;
    int Author = 464+16;
    // = 496 bytes
    int Reserved2 = 496;
    // = 512 bytes
    int Reserved3 = 512;
}

