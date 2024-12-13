package cn.navibeidou.beidou.Util;

public class ListItem {

    private String title;
    private String subTitle;
    private int style;

    public ListItem() {
    }

    public ListItem(String title, String subTitle, int style) {
        this.title = title;
        this.subTitle = subTitle;
        this.style = style;
    }

    public String getTitle() {
        return title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public int getStyle() {
        return style;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public void setStyle(int icon) {
        this.style = icon;
    }
}
