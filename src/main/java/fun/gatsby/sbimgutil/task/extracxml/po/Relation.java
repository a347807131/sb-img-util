package fun.gatsby.sbimgutil.task.extracxml.po;

/**
 * @Author: Yilia
 * @Date: 2024/2/4 0004 17:44
 */

import lombok.Data;

@Data
public class Relation {
    private String series;
    private String seriesLink;
    private String sub_series;
    private String sub_seriesLink;
    private String boundWith;
    private String boundWithLink;

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public String getSeriesLink() {
        return seriesLink;
    }

    public void setSeriesLink(String seriesLink) {
        this.seriesLink = seriesLink;
    }

    public String getSub_series() {
        return sub_series;
    }

    public void setSub_series(String sub_series) {
        this.sub_series = sub_series;
    }

    public String getSub_seriesLink() {
        return sub_seriesLink;
    }

    public void setSub_seriesLink(String sub_seriesLink) {
        this.sub_seriesLink = sub_seriesLink;
    }

    public String getBoundWith() {
        return boundWith;
    }

    public void setBoundWith(String boundWith) {
        this.boundWith = boundWith;
    }

    public String getBoundWithLink() {
        return boundWithLink;
    }

    public void setBoundWithLink(String boundWithLink) {
        this.boundWithLink = boundWithLink;
    }

    @Override
    public String toString() {
        return "Relation{" +
                "series='" + series + '\'' +
                ", seriesLink='" + seriesLink + '\'' +
                ", sub_series='" + sub_series + '\'' +
                ", sub_seriesLink='" + sub_seriesLink + '\'' +
                ", boundWith='" + boundWith + '\'' +
                ", boundWithLink='" + boundWithLink + '\'' +
                '}';
    }
}
