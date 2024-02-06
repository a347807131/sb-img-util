package fun.gatsby.sbimgutil.task.extracxml.po;

/**
 * @Author: Yilia
 * @Date: 2024/2/4 0004 17:44
 */

import lombok.Data;

@Data
public class Location {
    private String collectionUnit;
    private String callNumber;

    public String getCollectionUnit() {
        return collectionUnit;
    }

    public void setCollectionUnit(String collectionUnit) {
        this.collectionUnit = collectionUnit;
    }

    public String getCallNumber() {
        return callNumber;
    }

    public void setCallNumber(String callNumber) {
        this.callNumber = callNumber;
    }

    @Override
    public String toString() {
        return "Location{" +
                "collectionUnit='" + collectionUnit + '\'' +
                ", callNumber='" + callNumber + '\'' +
                '}';
    }
}
