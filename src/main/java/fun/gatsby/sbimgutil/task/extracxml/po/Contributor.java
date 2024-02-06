package fun.gatsby.sbimgutil.task.extracxml.po;

/**
 * @Author: Yilia
 * @Date: 2024/2/4 0004 17:18
 */

import lombok.Data;

@Data
public class Contributor {
    private String contributor;
    private String statementOfResponsiblePerson;
    private String role;

    public String getContributor() {
        return contributor;
    }

    public void setContributor(String contributor) {
        this.contributor = contributor;
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
        return "Contributor{" +
                "contributor='" + contributor + '\'' +
                ", statementOfResponsiblePerson='" + statementOfResponsiblePerson + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
