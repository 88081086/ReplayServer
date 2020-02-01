package pengguang.replayserver;

import java.util.Random;

public class Zob extends Object {
    public int turn;
    public int i1, i2;

    public Zob() {
        i1 = i2 = 0;
    }

    public Zob(Zob z) {
        turn = z.turn;
        i1 = z.i1;
        i2 = z.i2;
    }

    public void init(Random r) {
        i1 = r.nextInt();
        i2 = r.nextInt();
    }

    public void xor(Zob z) {
        i1 ^= z.i1;
        i2 ^= z.i2;
    }

    public boolean equals(Zob z) {
        return turn==z.turn && i1==z.i1 && i2==z.i2;
    }

    public String toString() {
        return String.format("%x,%x", i1, i2);
    }

    @Override
    public int hashCode() {
        return i1&0xff;
    }

    @Override
    public boolean equals(Object o) {
        return this.equals((Zob) o);
    }
}
