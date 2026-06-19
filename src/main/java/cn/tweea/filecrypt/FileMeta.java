/*
 * 版权所有 2017 Tweea。
 * 保留所有权利。
 */
package cn.tweea.filecrypt;

public class FileMeta {
    private String plainFileName;

    private String cipherFileName;

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
}
