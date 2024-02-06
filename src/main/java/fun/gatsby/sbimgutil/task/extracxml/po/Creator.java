package fun.gatsby.sbimgutil.task.extracxml.po;

/**
 * @Author: Yilia
 * @Date: 2024/2/4 0004 17:17
 */

import lombok.Data;

@Data
public class Creator {
    private String creator;
    private String statementOfResponsiblePerson;
    private String role;

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getStatementOfResponsiblePerson() {
        return statementOfResponsiblePerson;
    }

    public void setStatementOfResponsiblePerson(String statementOfResponsiblePerson) {
        this.statementOfResponsiblePerson = statementOfResponsiblePerson;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "Creator{" +
                "creator='" + creator + '\'' +
                ", statementOfResponsiblePerson='" + statementOfResponsiblePerson + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
