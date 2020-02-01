package pengguang.replayserver;

import java.util.List;
import java.util.ArrayList;

public class Rule {
    static final int REP = 0;
    static final int R60 = 1;
    static final int JIANG = 2;
    static final int ZHUO = 4;

    int mFlag = 0;

    int checkRule(ChessState state) {
        ArrayList<ChessRoute> list = (ArrayList<ChessRoute>) state.getRoutes().clone();
        if (state.mJiang) return 0x1;

        for (ChessRoute route: list) {
            if (state.mIndex[route.x2()][route.y2()] != ChessState.EMPTY &&
                state.getColor(route.x1(), route.y1()) != state.getColor(route.x2(), route.y2())) {
                state.go(route);

                boolean gen = false;
                int index = state.mIndex[route.x1()][route.y1()];
                ArrayList<ChessRoute> list1 = state.getRoutes();
                for (ChessRoute route1: list1) {
                    if (route1.x2() == route.x2() && route1.y2() == route.y2()) {
                        gen = true;
                        break;
                    }
                }

                state.retreat();

                if (!gen) {
                    return 0x2;
                }
            }
        }

        return 0;
    }

    int checkRule(ChessState state, ChessRoute route) {
        int before = checkRule(state);
        state.go(route);
        state.mTurn ^= 1;
        int after = checkRule(state);

        if ((before & 0x1) != 0) return 0x1;
        if (((before & 0x2) == 0) && ((after & 0x2) != 0)) return 0x2;
        return 0;
    }

    boolean isRep(ChessState state, ChessRoute route) {
        int ret = check(state, route);
        return (ret&0x4) != 0;
    }

    boolean isRepJiang(ChessState state, ChessRoute route) {
        int ret = check(state, route);
        return (ret&(1<<state.mTurn)) != 0;
    }

    int check(ChessState state, ChessRoute route) {
        mFlag = 0;

        int loop = -1;
        state.go1(route); 
        List<ChessRoute> list = state.mRouteList;

        int i;
        int turn = state.mTurn^1;
        int round = 0;
        int jiang = 0x3;
        int zhuo = 0x3;
        Zob z = null;
        boolean DBG_REP = false;
        if (DBG_REP) Log.d("chess", "checkRep");
        for (i=list.size()-1; i>=0; i--, round++, turn^=1) {
            ChessRoute r = (ChessRoute) list.get(i);

            if (r.eat() != ChessState.EMPTY) break;

            if (round == 120) {
                setFlag(R60, true);
                break;
            }

            if (!r.jiang()) {
                jiang &= ~(1<<state.getColor(turn));
            }

            if (!r.zhuo()) {
                zhuo &= ~(1<<state.getColor(turn));
            }

            if (z == null) {
                z = r.zob;
            }

            boolean dup = r.zob.equals(z);
            if (DBG_REP) Log.d("chess", String.format("%s - %s%s%s", ""+r, turn==1?"[r]":"[b]", r.jiang()?"[jiang]":"", dup&&loop>0?"[dup]":""));

            if (dup) {
                loop++;

                if (loop == 3) {
                    setFlag(REP, true);
                    break;
                }
            }

        }

        state.retreat();
        mFlag |= (jiang<<JIANG);
        mFlag |= (zhuo<<ZHUO);
        if (DBG_REP) Log.d("chess", "checkRep ret="+String.format("0x%x", mFlag));
        return mFlag;
    }

    void setFlag(int shift, boolean en) {
        if (en) {
            mFlag |= (1<<shift);
        } else {
            mFlag &= ~(1<<shift);
        }
    }

    boolean getFlag(int shift) {
        return (mFlag & (1<<shift)) != 0;
    }
}
