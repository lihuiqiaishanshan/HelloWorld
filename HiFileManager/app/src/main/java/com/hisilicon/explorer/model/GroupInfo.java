package com.hisilicon.explorer.model;

import java.util.List;

/**
 */

public class GroupInfo {
    private String type;
    private List<RootInfo> roots;

    public GroupInfo() {
    }

    public GroupInfo(String type, List<RootInfo> roots) {
        this.type = type;
        this.roots = roots;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<RootInfo> getRoots() {
        return roots;
    }

    public void setRoots(List<RootInfo> roots) {
        this.roots = roots;
    }
}
