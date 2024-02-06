package fun.gatsby.sbimgutil.task.extracxml.po;

/**
 * @Author: Yilia
 * @Date: 2024/2/4 0004 17:19
 */

import lombok.Data;

@Data
public class Date {
    private String GregorianCalendar;
    private String ChineseCalendar;
    private String issued;
    private String printed;

    public String getGregorianCalendar() {
        return GregorianCalendar;
    }

    public void setGregorianCalendar(String gregorianCalendar) {
        GregorianCalendar = gregorianCalendar;
    }

    public String getChineseCalendar() {
        return ChineseCalendar;
    }

    public void setChineseCalendar(String chineseCalendar) {
        ChineseCalendar = chineseCalendar;
    }

    public String getIssued() {
        return issued;
    }

    public void setIssued(String issued) {
        this.issued = issued;
    }

    public String getPrinted() {
        return printed;
    }

    public void setPrinted(String printed) {
        this.printed = printed;
    }

    @Override
    public String toString() {
        return "Date{" +
                "GregorianCalendar='" + GregorianCalendar + '\'' +
                ", ChineseCalendar='" + ChineseCalendar + '\'' +
                ", issued='" + issued + '\'' +
                ", printed='" + printed + '\'' +
                '}';
    }
}
