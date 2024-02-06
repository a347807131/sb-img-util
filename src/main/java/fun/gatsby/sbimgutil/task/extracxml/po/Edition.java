package fun.gatsby.sbimgutil.task.extracxml.po;

/**
 * @Author: Yilia
 * @Date: 2024/2/4 0004 17:18
 */

import lombok.Data;

@Data
public class Edition {
    private String edition;
    private String editionType;
    private String editionSupplement;

    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    public String getEditionType() {
        return editionType;
    }

    public void setEditionType(String editionType) {
        this.editionType = editionType;
    }

    public String getEditionSupplement() {
        return editionSupplement;
    }

    public void setEditionSupplement(String editionSupplement) {
        this.editionSupplement = editionSupplement;
    }

    @Override
    public String toString() {
        return "Edition{" +
                "edition='" + edition + '\'' +
                ", editionType='" + editionType + '\'' +
                ", editionSupplement='" + editionSupplement + '\'' +
                '}';
    }
}
