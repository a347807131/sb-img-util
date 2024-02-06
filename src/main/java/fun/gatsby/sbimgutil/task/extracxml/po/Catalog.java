package fun.gatsby.sbimgutil.task.extracxml.po;

/**
 * @Author: Yilia
 * @Date: 2024/2/5 0005 9:09
 */

import lombok.Data;

@Data
public class Catalog {
    private String bookID;
    private Integer internalSequenceNumber;
    private Integer levelNumber;
    private String volumeTitleAndArticleTitle;
    private String articleAuthor;
    private String volumeName;
    private String page;

    public String getBookID() {
        return bookID;
    }

    public void setBookID(String bookID) {
        this.bookID = bookID;
    }

    public Integer getInternalSequenceNumber() {
        return internalSequenceNumber;
    }

    public void setInternalSequenceNumber(Integer internalSequenceNumber) {
        this.internalSequenceNumber = internalSequenceNumber;
    }

    public Integer getLevelNumber() {
        return levelNumber;
    }

    public void setLevelNumber(Integer levelNumber) {
        this.levelNumber = levelNumber;
    }

    public String getVolumeTitleAndArticleTitle() {
        return volumeTitleAndArticleTitle;
    }

    public void setVolumeTitleAndArticleTitle(String volumeTitleAndArticleTitle) {
        this.volumeTitleAndArticleTitle = volumeTitleAndArticleTitle;
    }

    public String getArticleAuthor() {
        return articleAuthor;
    }

    public void setArticleAuthor(String articleAuthor) {
        this.articleAuthor = articleAuthor;
    }

    public String getVolumeName() {
        return volumeName;
    }

    public void setVolumeName(String volumeName) {
        this.volumeName = volumeName;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    @Override
    public String toString() {
        return "Catalog{" +
                "bookID='" + bookID + '\'' +
                ", internalSequenceNumber=" + internalSequenceNumber +
                ", levelNumber=" + levelNumber +
                ", volumeTitleAndArticleTitle='" + volumeTitleAndArticleTitle + '\'' +
                ", articleAuthor='" + articleAuthor + '\'' +
                ", volumeName='" + volumeName + '\'' +
                ", page='" + page + '\'' +
                '}';
    }
}
