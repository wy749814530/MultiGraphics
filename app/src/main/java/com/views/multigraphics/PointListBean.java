package com.views.multigraphics;

import java.io.Serializable;
import java.util.List;

/**
 * @WYU-WIN
 * @date 2020/8/4 0004.
 * description：
 */
public class PointListBean implements Serializable {

    /**
     * Region : [[{"x":258,"y":159},{"x":898,"y":159},{"x":898,"y":519},{"x":258,"y":519}],[{"x":1019,"y":157},{"x":1659,"y":157},{"x":1659,"y":517},{"x":1019,"y":517}],[{"x":212,"y":668},{"x":852,"y":668},{"x":852,"y":1028},{"x":212,"y":1028}],[{"x":998,"y":603},{"x":1638,"y":603},{"x":1638,"y":963},{"x":998,"y":963}]]
     * Enable : true
     * RegionType : 1
     */

    private boolean Enable;
    private int RegionType;
    private List<List<RegionBean>> Region;

    public boolean isEnable() {
        return Enable;
    }

    public void setEnable(boolean Enable) {
        this.Enable = Enable;
    }

    public int getRegionType() {
        return RegionType;
    }

    public void setRegionType(int RegionType) {
        this.RegionType = RegionType;
    }

    public List<List<RegionBean>> getRegion() {
        return Region;
    }

    public void setRegion(List<List<RegionBean>> Region) {
        this.Region = Region;
    }

    public static class RegionBean implements Serializable {
        /**
         * x : 258
         * y : 159
         */

        private int x;
        private int y;

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }
    }
}
