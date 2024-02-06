package fun.gatsby.sbimgutil.task.extracxml.po;

/**
 * @Author: Yilia
 * @Date: 2024/2/4 0004 17:17
 */

import lombok.Data;

@Data
public class Title {
    private String title;
    private String parallelTitle;
    private String otherTitle;
    private String titleOrigin;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getParallelTitle() {
        return parallelTitle;
    }

    public void setParallelTitle(String parallelTitle) {
        this.parallelTitle = parallelTitle;
    }

    public String getOtherTitle() {
        return otherTitle;
    }

    public void setOtherTitle(String otherTitle) {
        this.otherTitle = otherTitle;
    }

    public String getTitleOrigin() {
        return titleOrigin;
    }

    public void setTitleOrigin(String titleOrigin) {
        this.titleOrigin = titleOrigin;
    }

    @Override
    public String toString() {
        return "Title{" +
                "title='" + title + '\'' +
                ", parallelTitle='" + parallelTitle + '\'' +
                ", otherTitle='" + otherTitle + '\'' +
                ", titleOrigin='" + titleOrigin + '\'' +
                '}';
    }
}
