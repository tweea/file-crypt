/*
 * 版权所有 2017 Tweea。
 * 保留所有权利。
 */
package cn.tweea.filecrypt;

import java.util.ArrayList;
import java.util.List;

public class FileMeta {
    private String plainFileName;

    private String cipherFileName;

    private List<String> locations;

    public FileMeta() {
        this.locations = new ArrayList<>();
    }

    public String getPlainFileName() {
        return plainFileName;
    }

    public void setPlainFileName(String plainFileName) {
        this.plainFileName = plainFileName;
    }

    public String getCipherFileName() {
        return cipherFileName;
    }

    public void setCipherFileName(String cipherFileName) {
        this.cipherFileName = cipherFileName;
    }

    public List<String> getLocations() {
        return locations;
    }

    public void setLocations(List<String> locations) {
        this.locations = locations;
    }
}
