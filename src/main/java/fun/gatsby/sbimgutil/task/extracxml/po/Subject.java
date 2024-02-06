package fun.gatsby.sbimgutil.task.extracxml.po;

/**
 * @Author: Yilia
 * @Date: 2024/2/4 0004 17:54
 */

import lombok.Data;

@Data
public class Subject {
    private String subject;
    private String CCT;
    private String FDC;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getCCT() {
        return CCT;
    }

    public void setCCT(String CCT) {
        this.CCT = CCT;
    }

    public String getFDC() {
        return FDC;
    }

    public void setFDC(String FDC) {
        this.FDC = FDC;
    }

    @Override
    public String toString() {
        return "Subject{" +
                "subject='" + subject + '\'' +
                ", CCT='" + CCT + '\'' +
                ", FDC='" + FDC + '\'' +
                '}';
    }
}
