
package com.hisilicon.launcher.coverflow;

import java.util.ArrayList;
import java.util.List;

/**
 * control the image position
 *
 * @author li_bin
 */
public class PositionLogic {

    private static List<Postion> mPostions;
    protected static float viewWidth[] = {
            0, 0, 0, 0, 0
    };

    public static List<Postion> getPlist() {
        return mPostions;
    }

    public PositionLogic() {
        Postion p1 = new Postion(viewWidth[0], 1.0f, 0);
        Postion p2 = new Postion(viewWidth[1], 1.0f, 1);
        Postion p3 = new Postion(viewWidth[2], 1.0f, 4);
        Postion p4 = new Postion(viewWidth[3], 1.0f, 3);
        Postion p5 = new Postion(viewWidth[4], 1.0f, 2);

        mPostions = new ArrayList<Postion>();
        mPostions.add(p1);
        mPostions.add(p2);
        mPostions.add(p3);
        mPostions.add(p4);
        mPostions.add(p5);
    }

    public PositionLogic(float[] viewWidth2) {
        Postion p1 = new Postion(viewWidth2[0], 1.45f, 0);
        Postion p2 = new Postion(viewWidth2[1], 1.6f, 1);
        Postion p3 = new Postion(viewWidth2[2], 1.9f, 4);
        Postion p4 = new Postion(viewWidth2[3], 1.6f, 3);
        Postion p5 = new Postion(viewWidth2[4], 1.45f, 2);

        mPostions = new ArrayList<Postion>();
        mPostions.add(p1);
        mPostions.add(p2);
        mPostions.add(p3);
        mPostions.add(p4);
        mPostions.add(p5);
    }

    public Postion getNextPosion(Postion p) {
        if (p == null) {
            return null;
        }

        for (int i = 0; i < mPostions.size(); i++) {
            if (mPostions.get(i).equals(p)) {
                if (i == mPostions.size() - 1) {
                    return mPostions.get(0);
                } else {
                    return mPostions.get(i + 1);
                }
            }
        }

        return null;
    }

}
