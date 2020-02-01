package pengguang.replayserver;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.*;
import java.util.Arrays;
import java.io.*;
import java.util.regex.*;

public class PgnLoader extends Loader
{
    static boolean DEBUG = false;

    static final int ICCS = 0;
    static final int CHINESE = 1;

    static final int PING = 0;
    static final int JIN = 1;
    static final int TUI = 2;

    static final String INIT_FEN = "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR w";
    static final String EMPTY_FEN = "9/9/9/9/9/9/9/9/9/9 w";

    int mFormat = CHINESE;
    String mEncoding = "GBK";

    public PgnLoader() {
        super();
    }

    public PgnLoader(String file) {
        super();
        mFilename = file;
    }

    public PgnLoader(InputStream in) {
        super();
        mInputStream = in;
    }

    @Override
    public boolean load(Listener listener) {
        super.load(listener);

        String[] format = {"UTF-8", "GBK"};

        mList = new ArrayList<HashMap<String, Object>>();
        ArrayList<HashMap<String, Object>> list = mList;

        int i;
        boolean matched = false;
        for (i=0; i<format.length && mFormat == CHINESE && !matched; i++) {
            try {
                BufferedReader br = null;
                HashMap<String, Object> map;
                String zipExtension = ".zip";
                int index = -1;

                if (mFilename != null) {
                    index = mFilename.toLowerCase().indexOf(zipExtension);
                }

                if (mFilename == null || index == -1) {
                    if (mFilename != null) {
                        br = new BufferedReader(new InputStreamReader(new FileInputStream(mFilename), format[i]));
                    } else {
                        br = new BufferedReader(new InputStreamReader(mInputStream, format[i]));
                    }
                } else {
                    index += zipExtension.length();
                    String zipName = mFilename.substring(0, index);
                    index += 1;
                    String entryName = mFilename.substring(zipName.length()+1);

                    ZipFile z = new ZipFile(zipName);
                    ZipEntry e = z.getEntry(entryName);
                    br = new BufferedReader(new InputStreamReader(z.getInputStream(e), format[i]));
                }

                ChessState state = new ChessState();
                String w = ""; /* whole */
                String line = null;
                String whole = "";
                while ((line = br.readLine()) != null)
                {
                    String regTag = "^\\[(.*)\"(.*)\"\\]$";
                    if (line.matches(regTag)) {
                        String tag = getTag(line, "FEN");
                        if (tag != null) {
                            state.initBoard(tag);
                            setInit(tag);
                        }

                        tag = getTag(line, "Format");
                        if (tag != null) {
                            if (tag.toLowerCase().equals("iccs")) {
                                mFormat = ICCS;
                                matched = true;
                            }
                        }

                        if ((tag = getTag(line, "Event")) != null) {
                            mEvent = tag;
                        }
                        if ((tag = getTag(line, "Date")) != null) {
                            mDate = tag;
                        }
                        if ((tag = getTag(line, "Site")) != null) {
                            mSite = tag;
                        }
                        if ((tag = getTag(line, "Red")) != null) {
                            mRed = tag;
                        }
                        if ((tag = getTag(line, "Black")) != null) {
                            mBlack = tag;
                        }
                        if ((tag = getTag(line, "Result")) != null) {
                            if (tag.equals("1-0")) {
                                mResult = 1;
                            } else if (tag.equals("0-1")) {
                                mResult = 0;
                            } else if (tag.equals("1/2-1/2")) {
                                mResult = 2;
                            } else {
                                mResult = -1;
                            }
                        }

                        continue;
                    }

                    if (mFormat == ICCS) {
                        String regStep = "^[0-9]+\\. *([A-Z][0-9]\\-[A-Z][0-9]) ?([A-Z][0-9]\\-[A-Z][0-9])?$";
                        if (!line.matches(regStep)) {
                            continue;
                        }

                        Matcher m = Pattern.compile(regStep).matcher(line);
                        if (!m.find()) {
                            continue;
                        }

                        String stepA = m.group(1);
                        String stepB = m.group(2);
                        ChessRoute route = null;

                        if (stepA != null && (route = getRoute(stepA)) != null) {
                            map = new HashMap<String, Object>();
                            map.put("route", route);
                            map.put("step", getText(route, state));
                            list.add(map);
                            addTree(map);
                        }

                        if (stepB != null && (route = getRoute(stepB)) != null) {
                            map = new HashMap<String, Object>();
                            map.put("route", route);
                            map.put("step", getText(route, state));
                            list.add(map);
                            addTree(map);
                        }            
                    } else {
                        String directionRegex = "[^{}]*[\u5e73|\u8fdb|\u9000][^{}]*";
                        if (line.matches(directionRegex)) {
                            matched = true;
                        }

                        whole += line;

                        if (DEBUG) Log.d("chess", line);
                    }
                }

                if (mFormat == CHINESE && matched) {
                    String regex = "[ \n]*\\{([^\\{\\}]+)\\}.*";
                    if (whole.matches("(?s)^"+regex)) {
                        Matcher matcher = Pattern.compile(regex, Pattern.DOTALL).matcher(whole);
                        matcher.find();
                        String part = matcher.group(1);
                        if (part != null) {
                            mComment = part;

                            HashMap<String, Object> m = mRoot.data;
                            m.put("comment", mComment);
                        }
                    }

                    regex = "[0-9]+\\.[ \n\t]*([^\\.\n \\{\\}]{4})([ \n\t]*\\{([^\\{\\}]+)\\})?([ \n\t]*([^\\.\n \\{\\}]{4})([ \n\t]*\\{([^\\{\\}]+)\\})?)?";
                    Matcher m = Pattern.compile(regex, Pattern.DOTALL).matcher(whole);
                    String part = null;
                    while (m.find()) {
                        part = m.group(1);
                        String comment = null;
                        ChessRoute route = null;
                        if (part != null && (route = getRoute(part, state)) != null) {
                            map = new HashMap<String, Object>();
                            map.put("route", route);
                            map.put("step", part);
                            comment = m.group(3);
                            if (comment != null) {
                                map.put("comment", comment);
                            }
                            list.add(map);
                            addTree(map);
                            if (DEBUG) Log.d("chess", String.format("%d - %s", mCurrent.count, (String) map.get("step")));
                        }

                        part = m.group(5);
                        if (part != null && (route = getRoute(part, state)) != null) {
                            map = new HashMap<String, Object>();
                            map.put("route", route);
                            map.put("step", part);
                            comment = m.group(7);
                            if (comment != null) {
                                map.put("comment", comment);
                            }
                            list.add(map);
                            addTree(map);
                            if (DEBUG) Log.d("chess", String.format("%d - %s", mCurrent.count, (String) map.get("step")));
                        }
                    }
                }
            } catch (Exception e) {
                Log.d("chess", "e: "+e);
                e.printStackTrace();
            }
        }

        return matched;
    }

    public String getTag(String line, String tag) {
        String regTag = String.format("^\\[%s +\"(.*)\"\\]$", tag);
        Matcher m = Pattern.compile(regTag).matcher(line);
        if (m.find()) {
            return m.group(1);
        } else {
            return null;
        }
        /*
        String l = line.toLowerCase();
        String t = tag.toLowerCase();
        if (l.indexOf("["+t+" ") == -1) return null;

        int start = line.indexOf("\"");
        if (start == -1) return null;
        int end = line.lastIndexOf("\"");
        if (end == start) return null;
        return line.substring(start+1, end);*/
    }

    public static ChessRoute getRoute(String step)
    {
        byte[] a = step.substring(step.indexOf('.')+1).getBytes();
        return new ChessRoute('9'-a[1], a[0]-'A', '9'-a[4], a[3]-'A');
    }

    public static ChessRoute getRoute(String step, ChessState state)
    {
        String[] name = {
            /* jiang, shi, xiang, ma, che, pao, bing */
            "将|將|帅|帥", "士|仕", "相|象", "马|馬|傌", "车|車|俥", "炮|砲", "兵|卒"
        };
        String[] col = new String[] {
            "1|１", "2|２", "3|３", "4|４", "5|５", "6|６", "7|７", "8|８", "9|９", 
            "九", "八", "七", "六", "五", "四", "三", "二", "一"
        };
        String[] delta = new String[] {"0", "1|１|一", "2|２|二", "3|３|三", "4|４|四", "5|５|五", "6|６|六", "7|７|七", "8|８|八", "9|９|九"};
        String[] direct = new String[] {"平", "进", "退"};

        int turn = state.mTurn;
        int color = state.getColor(turn);
        if (state.mBottomColor != ChessState.RED) {
            int i;
            for (i=0; i<col.length; i++) {
                if (i >= col.length-1-i) break;

                String temp = col[i];
                col[i] = col[col.length-1-i];
                col[col.length-1-i] = temp;
            }
        }
        if (DEBUG) Log.d("chess", String.format("getRoute %s, turn=%d", step, turn));

        int category;
        int src_row = -1, src_col = -1, dst_row = -1, dst_col = -1;
        int direction;
        int inc;
        int increment;
        boolean rare = false;

        String QIAN="\u524d";
        String ZHONG="\u4e2d";
        String HOU="\u540e";

        if (DEBUG) Log.d("chess", "hmmm, get route for "+step);

        int i;
        for (i=0; i<3; i++) {
            if (step.substring(2, 3).equals(direct[i])) break;
        }
        if (i != 3) {
            direction = i;
        } else {
            /* rare: qian bing er jin yi */
            for (i=0; i<3; i++) {
                if (step.substring(3, 4).equals(direct[i])) break;
            }
            if (i != 3) {
                direction = i;
                rare = true;
            } else {
                Log.d("chess", "cant match direction for "+step);
                return null;
            }
        }
        if (DEBUG) Log.d("chess", "direction: "+direction);

        for (i=turn*9; i<turn*9+9; i++)
        {
            if (step.substring(3, 4).matches(col[i])) break;
        }
        if (i == turn*9+9)
        {
            Log.d("chess", "cant match dest col "+step.substring(3, 4));
            return null;
        }
        dst_col = i%9;
        if (DEBUG) Log.d("chess", "dst_col: "+dst_col);

        for (i=0; i<delta.length; i++) {
            if (step.substring(3, 4).matches(delta[i])) break;
        }
        if (i == delta.length) {
            Log.d("chess", "cant match increment");
            return null;
        }
        increment = i%10;
        if (DEBUG) Log.d("chess", "increment: "+increment);

        int factor;
        if (turn == ChessState.TOP)
            factor = 1;
        else
            factor = -1;

        for (i=0; i<7; i++)
        {
            if (step.substring(0, 1).matches(name[i])) break;
        }

        if (i != 7)
        {
            category = i%7;
            if (DEBUG) Log.d("chess", "category: "+category);

            for (i=turn*9; i<turn*9+9; i++)
            {
                if (step.substring(1, 2).matches(col[i])) break;
            }
            src_col = i%9;
            if (DEBUG) Log.d("chess", "src_col: "+src_col);

            /* get source row */
            int r, c;
            boolean found = false;
            for (r=0; r<ChessState.MAX_ROW&&!found; r++) {
                for (c=0; c<ChessState.MAX_COL&!found; c++) {
                    if (state.mIndex[r][c] == ChessState.EMPTY) continue;
                    if (state.getColor(r, c) != color) continue;
                    if (state.getCat(r, c) == category && c == src_col) {
                        if (category == ChessState.SHI || category == ChessState.XIANG) {
                            inc = 1;
                            int min = 0;
                            int max = 2;
                            if (category == ChessState.XIANG) {
                                inc = 2;
                                max = 4;
                            }

                            if (state.getColor(r, c) == state.getColor(ChessState.BOTTOM)) {
                                int tmp = min;
                                min = 9-max;
                                max = 9-tmp;
                            }

                            int x = r;
                            if (direction==JIN && x+factor*inc>=min && x+factor*inc<=max) {
                                src_row = x;
                                found = true;
                                break;
                            }
                            if (direction==TUI && x-factor*inc>=min && x-factor*inc<=max) {
                                found = true;
                                src_row = x;
                                break;
                            }
                        } else {
                            found = true;
                            src_row = r;
                            break;
                        }
                    }
                }
            }
            if (!found) {
                Log.d("chess", "cant get src row");
                return null;
            }
            if (DEBUG) Log.d("chess", "src_row: "+src_row);
        } else {
            for (i=0; i<name.length; i++) {
                if (step.substring(1, 2).matches(name[i])) break;
            }

            if (i == turn*7+7) {
                Log.d("chess", "fallback category failed");
                return null;
            }

            category = i%7;
            if (DEBUG) Log.d("chess", "fallback category: "+category);

            if (rare) {
                for (i=turn*9; i<turn*9+9; i++) {
                    if (step.substring(1, 2).matches(col[i])) break;
                }
                src_col = i%9;
                if (DEBUG) Log.d("chess", "src_col: "+src_col);
            }

            int[] list = new int[5];
            int r, c;
            int rs=9, re=0, rinc=-1; /* start, end, inc */
            int cs=0, ce=8, cinc=1;
            int total=0, totalCol;

            if (turn == ChessState.BOTTOM) {
                rs=0;
                re=9;
                rinc = 1;
                cs=8;
                ce=0;
                cinc = -1;
            }

            for (c=cs; cinc>0?c<=ce:c>=0; c+=cinc)
            {
                totalCol = 0;
                for (r=rs; rinc>0?r<=re:r>=0; r+=rinc)
                {
                    int index = state.mIndex[r][c];
                    if (index != ChessState.EMPTY)
                    {
                        if (state.getColor(r, c) != state.getColor(turn)) continue;
                        if (state.getCat(r, c) == category)
                        {
                            list[total+totalCol] = (r<<4)+c;
                            totalCol++;
                        }
                    }
                }
                if (totalCol >= 2) {
                    total += totalCol;
                }
            }

            if (total < 2) {
                Log.d("chess", "no dup piece found in a col");
                return null;
            }

            int index = 0;
            String p = step.substring(0, 1);
            String[] seq = {
                "\u4e00|\u524d", "\u4e8c|\u4e2d", "\u4e09|\u540e", "\u56db", "\u4e94"
            };
            if (total >= 3) {
                for (i=0; i<seq.length; i++) {
                    if (p.matches(seq[i])) break;
                }

                if (i >= seq.length) {
                    Log.d("chess", "no position found");
                    return null;
                }
            } else {
                i = p.equals("\u524d")?0:1;
            }

            src_row = (list[i]>>4)&0xf;
            src_col = (list[i]>>0)&0xf;
        }

        switch (category) {
            case ChessState.JIANG:
            case ChessState.CHE:
            case ChessState.PAO:
            case ChessState.BING:
                if (direction == 0)
                {
                    dst_row = src_row;
                }
                else if (direction == 1)
                {
                    dst_row = src_row+increment*factor;
                    dst_col = src_col;
                }
                else if (direction == 2)
                {
                    dst_row = src_row-increment*factor;
                    dst_col = src_col;
                }
                else
                {
                    Log.d("chess", "unknown direction: "+direction);
                }
                break;

            case ChessState.SHI:
            case ChessState.XIANG:
            case ChessState.MA:
                if (category == ChessState.SHI) {
                    inc = 1;
                } else if (category == ChessState.XIANG) {
                    inc = 2;
                } else {
                    int abs = Math.abs(src_col-dst_col);
                    if (abs == 1) {
                        inc = 2;
                    } else {
                        inc = 1;
                    }
                }
                if (direction == 1) {
                    dst_row = src_row+inc*factor;
                } else if (direction == 2) {
                    dst_row = src_row-inc*factor;
                } else {
                    Log.d("chess", "invalid direction "+direction);
                }
                break;
            default:
                Log.d("chess", "unkown category: "+category);
        }

        if (DEBUG) Log.d("chess", String.format("step: [%s], route: (%d, %d) -> (%d, %d)", step, src_row, src_col, dst_row, dst_col));

        if (!(src_row>=0&&src_row<=9&&src_col>=0&&src_col<9))
            return null;
        int index = state.mIndex[src_row][src_col];
        ChessRoute route = new ChessRoute(src_row, src_col, dst_row, dst_col);
        if (index != ChessState.EMPTY && state.goValid(route)) {
            state.go(route);
            return route;
        } else {
            return null;
        }
    }

    public static String getText(ChessRoute route, ChessState state)
    {
        String[] direction = new String[] {"平", "进", "退"};
        final int PING = 0;
        final int JIN = 1;
        final int TUI = 2;

        int srcX = route.x1();
        int srcY = route.y1();
        int dstX = route.x2();
        int dstY = route.y2();

        int index = state.mIndex[srcX][srcY];
        if (index == ChessState.EMPTY) {
            return null;
        }

        int cat = state.getCat(srcX, srcY);
        int turn = state.mTurn;
        int color = turn;
        int factor = 1;

        if (turn == ChessState.BOTTOM) {
            factor = -1;
        }

        if (state.mBottomColor != ChessState.RED) {
            color ^= 1;
        }
        
        String txtStep = "";

        String[][] col = {{"１", "２", "３", "４", "５", "６", "７", "８", "９"}, {"一", "二", "三", "四", "五", "六", "七", "八", "九"}};

        txtStep += getP1P2(route, state);

        if (cat == ChessState.SHI ||
            cat == ChessState.XIANG ||
            cat == ChessState.MA)
        {
            if ((dstX-srcX)*factor > 0) {
                txtStep += direction[JIN];
            } else {
                txtStep += direction[TUI];
            }

            if (color == state.mBottomColor) {
                txtStep += col[color][8-dstY];
            } else {
                txtStep += col[color][dstY];
            }
        }
        else
        {
            String[][] num = {{"０", "１", "２", "３", "４", "５", "６", "７", "８", "９",}, {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"}};
            if ((dstX-srcX)*factor > 0) {
                txtStep += direction[JIN];
                txtStep += num[color][(dstX-srcX)*factor];
            } else if ((srcX-dstX)*factor > 0) {
                txtStep += direction[TUI];
                txtStep += num[color][(srcX-dstX)*factor];
            } else {
                txtStep += direction[PING];
                if (color == state.mBottomColor) {
                    txtStep += col[color][8-dstY];
                } else {
                    txtStep += col[color][dstY];
                }
            }
        }

        state.go(route);

        return txtStep;
    }

    static String getP1P2(ChessRoute route, ChessState state) {
        int[] list = new int[5];
        int[] listWithOthers = new int[5];
        int turn = state.mTurn;
        int color = turn;
        int i;
        int r, c;
        int rs=9, re=0, rinc=-1; /* start, end, inc */
        int cs=0, ce=8, cinc=1;
        int total=0, totalCol;
        int othersExceedTwo = 0;
        int totalWithOthers = 0;

        if (state.mBottomColor != ChessState.RED) {
            color ^= 1;
        }
        
        String[][] name = {{"将", "士", "象", "马", "车", "炮", "卒",}, {"帅", "仕", "相", "马", "车", "炮", "兵"}};
        String[][] col = {{"１", "２", "３", "４", "５", "６", "７", "８", "９"}, {"一", "二", "三", "四", "五", "六", "七", "八", "九"}};

        int index = state.mIndex[route.x1()][route.y1()];
        if (index==ChessState.EMPTY) {
            return null;
        }

        int category = state.getCat(route.x1(), route.y1());

        if (turn == ChessState.BOTTOM) {
            rs=0;
            re=9;
            rinc = 1;
            cs=8;
            ce=0;
            cinc = -1;
        }

        for (c=cs; cinc>0?c<=ce:c>=0; c+=cinc)
        {
            totalCol = 0;
            for (r=rs; rinc>0?r<=re:r>=0; r+=rinc)
            {
                index = state.mIndex[r][c];
                if (index != ChessState.EMPTY)
                {
                    if (state.getColor(r, c) != state.getColor(turn)) continue;
                    if (state.getCat(r, c) == category)
                    {
                        if (c == route.y1()) {
                            list[total++] = r<<4|c;
                        }

                        listWithOthers[totalWithOthers+totalCol] = r<<4|c;
                        totalCol++;
                    }
                }
            }
            if (totalCol >= 2) {
                totalWithOthers += totalCol;

                if (c != route.y1()) {
                    othersExceedTwo ++;
                }
            }
        }

        if (total == 1 || category == ChessState.SHI || category == ChessState.XIANG) {
            if (color == state.mBottomColor) {
                return name[color][category] + col[color][8-route.y1()];
            } else {
                return name[color][category] + col[color][route.y1()];
            }
        } else {
            /* bing */
            index = state.mIndex[route.x1()][route.y1()];
            if (othersExceedTwo >= 1) {
                for (i=0; i<totalWithOthers; i++) {
                    if (listWithOthers[i] == (route.x1()<<4|route.y1())) break;
                }
            } else {
                for (i=0; i<total; i++) {
                    if (list[i] == (route.x1()<<4|route.y1())) break;
                }
            }

            String p1 = null;
            if (othersExceedTwo >= 1) {
                total = totalWithOthers;
            }
            if (total == 2) {
                String[] qianhou = {"前", "后"};
                p1=qianhou[i];
            } else if (total == 3) {
                String[] qianzhonghou = {"前", "中", "后"};
                p1=qianzhonghou[i];
            } else {
                String[] num = {"一", "二", "三", "四", "五"};
                p1=num[i];
            }

            return p1+name[color][category];
        }
    }
    public static String toFen(int[] init, int color) {
        ChessState state = new ChessState();
        state.initBoard(init, color);
        return toFen(state);
    }

    public static String toFen(ChessState state) {
        if (state.mJie) return null;

        int i, j;
        String fen = "";
        String[][] fenTag = {
            {
                "k", "a", "b", "n", "r", "c", "p",
            },{
                "K", "A", "B", "N", "R", "C", "P",
            },
        };
        int blank = 0;
        int maxr = ChessState.MAX_ROW-1;
        int maxc = ChessState.MAX_COL-1;
        for (i=0; i<=maxr; i++) {
            for (j=0; j<=maxc; j++) {
                int index = state.mIndex[i][j];
                if (index == ChessState.EMPTY) {
                    blank++;
                } else {
                    int color = state.getColor(i, j);
                    int cat = state.getCat(i,j);
                    if (blank != 0) {
                        fen += (""+blank);
                        blank = 0;
                    }

                    fen += fenTag[color][cat];
                }
            }

            if (blank != 0) {
                fen += (""+blank);
                blank = 0;
            }

            if (i != ChessState.MAX_ROW-1) {
                fen += "/";
            }
        }

        fen += String.format(" %s", state.getColor(state.mTurn)==ChessState.RED?"w":"b");

        return fen;
    }

    public static int[] fromFen(String fen) {
        int i, j, k, m;
        String fenRegex = "^([kabnrcpKABNRCP1-9]+/){9}[kabnrcpKABNRCP1-9]+/? +[wrb].*$";
        String[] fenTag = {
            "k", "a", "b", "n", "r", "c", "p",
        };

        if (!fen.matches(fenRegex)) {
            return null;
        }

        String parts[] = fen.split(" ");
        String a[] = parts[0].split("/");
        if (a.length < 10) {
            return null;
        }

        int[] temp = new int[32];
        int count = 0;
        for (i=0; i<a.length; i++) {
            k = 0;
            for (j=0; j<a[i].length(); j++) {
                char c = a[i].charAt(j);
                if (c >= '1' && c <= '9') {
                    int repeat = c-'0';
                    while (repeat-- > 0) {
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
                    temp[count++] = (color<<15)|cat<<8|(i<<4)|k;
                    k++;
                }
            }
        }

        int[] init = new int[count];
        for (i=0; i<count; i++) {
            init[i] = temp[i];
        }

        return init;
    }

    public static int getTurnFromFen(String fen) {
        int i, j, k, m;
        String fenRegex = "^([kabnrcpKABNRCP1-9]+/){9}[kabnrcpKABNRCP1-9]+/? +[wrb].*$";
        String[] fenTag = {
            "k", "a", "b", "n", "r", "c", "p",
        };

        if (!fen.matches(fenRegex)) {
            return 1;
        }

        String parts[] = fen.split(" ");
        if (parts.length < 2) {
            return ChessState.RED;
        }

        return parts[1].equals("b")?ChessState.BLACK:ChessState.RED;
    }

    public static void save(String fullname, int whoWon, ArrayList<ChessRoute> list, String fen, boolean swap) throws Exception
    {
        char[] row = {'9', '8', '7', '6', '5', '4', '3', '2', '1', '0'};
        char[] col = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I'};

        FileWriter writer = null;
        writer = new FileWriter(fullname, false); /* non-append */

        writer.write("[Event \"A match against computer\"]\n");
        writer.write("[Site \"Nowhere\"]\n");
        SimpleDateFormat sf = new SimpleDateFormat("yyyy.MM.dd");
        String d = sf.format(new Date());
        String s = String.format("[Date \"%s\"]\n", d);
        writer.write(s);
        s = String.format("[Round \"%d\"]\n", list.size()/2);
        writer.write(s);
        writer.write("[Black \"Computer\"]\n");
        writer.write("[Red \"User\"]\n");

        String result = null;
        if (whoWon == ChessState.BLACK) {
            result = "Black Won";
        } else if (whoWon == ChessState.RED) {
            result = "Red Won";
        } else {
            result = "Draw";
        }

        s = String.format("[Result \"%s\"]\n", result);
        writer.write(s);
        s = String.format("[Format \"ICCS\"]\n");
        writer.write(s);
        s = String.format("[FEN \"%s\"]\n", fen);
        writer.write(s);
        
        int i=0;
        int j=0;
        for (i=0; i<list.size(); i+=2)
        {
            s = String.format("%d.", j+1);
            if (i<list.size())
            {
                ChessRoute r = list.get(i);
                int x1,y1,x2,y2;
                x1 = r.x1();
                y1 = r.y1();
                x2 = r.x2();
                y2 = r.y2();
                if (swap) {
                    x1 = row.length-1-x1;
                    y1 = col.length-1-y1;
                    x2 = row.length-1-x2;
                    y2 = col.length-1-y2;
                }
                String t = String.format("%c%c-%c%c ", col[y1], row[x1], col[y2], row[x2]);
                s+=t;
            }

            if (i+1<list.size())
            {
                ChessRoute r = list.get(i+1);
                int x1,y1,x2,y2;
                x1 = r.x1();
                y1 = r.y1();
                x2 = r.x2();
                y2 = r.y2();
                if (swap) {
                    x1 = row.length-1-x1;
                    y1 = col.length-1-y1;
                    x2 = row.length-1-x2;
                    y2 = col.length-1-y2;
                }
                String t = String.format("%c%c-%c%c\n", col[y1], row[x1], col[y2], row[x2]);
                s+=t;
            }

            writer.write(s);
            j++;
        }

        writer.flush();
        writer.close();
    }

    public static void saveChinese(String fullname, int whoWon, ArrayList<ChessRoute> list, String fen, boolean swap) throws Exception
    {
        FileWriter writer = null;
        writer = new FileWriter(fullname, false); /* non-append */

        SimpleDateFormat sf = new SimpleDateFormat("yyyy.MM.dd");
        String d = sf.format(new Date());
        String s = String.format("[Date \"%s\"]\n", d);
        writer.write(s);
        s = String.format("[Round \"%d\"]\n", list.size()/2);
        writer.write(s);

        s = String.format("[FEN \"%s\"]\n", fen);
        writer.write(s);

        ChessState state = new ChessState();
        state.initBoard(fen);

        int i=0;
        int j=0;
        for (i=0; i<list.size(); i+=2)
        {
            s = String.format("%d. ", j+1);
            if (i<list.size()) {
                s += getText(list.get(i), state);
            }

            if (i+1<list.size()) {
                s += " ";
                s += getText(list.get(i+1), state);
                s += "\n";
            }

            writer.write(s);
            j++;
        }

        writer.flush();
        writer.close();
    }

    void saveTag(Writer writer, String tag, String val) throws Exception {
        if (val == null || val.length() == 0) return;
        writer.write(String.format("[%s \"%s\"]\n", tag, val));
    }

    public boolean save(Loader loader, String fullname) throws Exception {
        Writer writer = new OutputStreamWriter(new FileOutputStream(fullname), mEncoding); /* non-append */

        String result = "*";
        if (loader.mResult == 1) {
            result = "1-0";
        } else if (loader.mResult == 0) {
            result = "0-1";
        } else if (loader.mResult == 2) {
            result = "1/2-1/2";
        }

        saveTag(writer, "Format", "CHINESE");
        saveTag(writer, "Event", loader.mEvent);
        saveTag(writer, "Site", loader.mSite);
        saveTag(writer, "Date", loader.mDate);
        saveTag(writer, "Red", loader.mRed);
        saveTag(writer, "Black", loader.mBlack);
        saveTag(writer, "Result", result);

        if (loader.mInitData != null) {
            saveTag(writer, "FEN", toFen(loader.mInitData, loader.mTurn));
        }

        if (loader.mComment != null) {
            writer.write(String.format("{%s}\n", loader.mComment));
        }
        
        String s = null;
        TreeNode<HashMap<String, Object>> node = loader.mRoot;
        while (!node.isLeaf()) {
            node = node.children.get(0);
            HashMap<String, Object> map = node.data;
            int level = node.getLevel();
            if (level%2 != 0) {
                s = String.format("%d. %s", (level+1)/2, (String) map.get("step"));
            } else {
                s = String.format("%s", (String) map.get("step"));
            }

            String comment = (String) map.get("comment");
            if (comment != null) {
                s += String.format("\n{%s}\n", comment);
            } else {
                if (level%2 != 0) {
                    s += " ";
                } else {
                    s += "\n";
                }
            }

            writer.write(s);
        }

        writer.flush();
        writer.close();
        return true;
    }
}
