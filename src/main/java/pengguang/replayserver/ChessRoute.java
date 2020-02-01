package pengguang.replayserver;

public class ChessRoute extends Object
{
    static final int JIANG = 0;
    static final int ZHUO = 1;

    public int mValue = 0;
    public int rt = 0;
    public int mRuleFlag = 0;
    public int jie = 0;
    public Zob zob = null;

    public ChessRoute()
    {
    }

    public ChessRoute(int x1, int y1, int x2, int y2)
    {
        rt = (x1<<12)|(y1<<8)|(x2<<4)|(y2<<0);
    }

    public ChessRoute(ChessRoute route)
    {
        rt = route.rt;
    }

    public ChessRoute(String s)
    {
        String[] a = s.split(",");
        int x1 = Integer.parseInt(a[0]);
        int y1 = Integer.parseInt(a[1]);
        int x2 = Integer.parseInt(a[2]);
        int y2 = Integer.parseInt(a[3]);
        rt = (x1<<12)|(y1<<8)|(x2<<4)|(y2<<0);
    }

    public int x1() {
        return (rt&0xf000)>>12;
    }

    public int y1() {
        return (rt&0x0f00)>>8;
    }

    public int x2() {
        return (rt&0x00f0)>>4;
    }

    public int y2() {
        return (rt&0x000f);
    }

    public int eat() {
        return rt>>16;
    }

    public void eat(int i) {
        rt &= 0xffff;
        rt |= (i<<16);
    }

    public boolean jiang() {
        return (mRuleFlag & (1<<JIANG)) != 0;
    }

    public boolean zhuo() {
        return (mRuleFlag & (1<<ZHUO)) != 0;
    }

    public void jiang(boolean en) {
        if (en) {
            mRuleFlag |= 1<<JIANG;
        } else {
            mRuleFlag &= ~(1<<JIANG);
        }
    }

    public void zhuo(boolean en) {
        if (en) {
            mRuleFlag |= 1<<ZHUO;
        } else {
            mRuleFlag &= ~(1<<ZHUO);
        }
    }

    public String toString()
    {
        return String.format("(%d,%d)->(%d,%d)", x1(), y1(), x2(), y2());
    }

    public boolean eq(ChessRoute r) {
        if (x1() != r.x1()) return false;
        if (y1() != r.y1()) return false;
        if (x2() != r.x2()) return false;
        if (y2() != r.y2()) return false;
        return true;
    }

    public boolean jie() {
        return ((jie>>16)&0x1) == 1;
    }
    public void jie(boolean en) {
        if (en) {
            jie |= (1<<16);
        }
    }
}
