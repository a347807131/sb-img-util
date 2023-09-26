package fun.gatsby.sbimgutil.utils;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.TreeSet;

/**
 * @desc 定义书签
 */
@NoArgsConstructor
@Data
public class PdfBookmark implements Comparable<PdfBookmark> {
    Integer page;
    String title;
    Integer level;
    PdfBookmark parent;
    Integer order;

    Set<PdfBookmark> childrens = new TreeSet<>();

    public PdfBookmark(int level, String title, Integer page) {
        this.level = level;
        this.title = title;
        this.page = page;
    }

    @Override
    public int compareTo(PdfBookmark o) {
        if (this.page.equals(o.page)) {
            return this.order.compareTo(o.order);
        } else
            return this.page.compareTo(o.page);
    }

    @Override
    public String toString() {
        return "Bookmark{" +
                ", page=" + page +
                ", title='" + title + '\'' +
                ", level=" + level +
//                ", parent=" + parent +
//                ", children=" + children +
                '}';
    }

    public void addChild(PdfBookmark bookmark) {
        childrens.add(bookmark);
    }
}
