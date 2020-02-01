/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package pengguang.replayserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.io.File;

public class App {
    public static void main(String[] args) {
        Loader loader = Loader.loadReplay(new File(args[0]));
        if (loader != null) {
            ArrayList list =  loader.mList;
            int i;
            String s = "";
            if (loader.mInitData != null) {
                for (i=0; i<loader.mInitData.length; i++) s += String.format("%4x", loader.mInitData[i]);
            }
            Log.d(s);
            s = "";
            for (i=0; i<list.size(); i++) {
                ChessRoute r = (ChessRoute) ((HashMap<String, Object>)list.get(i)).get("route");
                s += r.y1();
                s += r.x1();
                s += r.y2();
                s += r.x2();
            }
            Log.d(s);
        }
    }
}