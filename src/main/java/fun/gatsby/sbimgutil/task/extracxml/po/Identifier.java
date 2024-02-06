package fun.gatsby.sbimgutil.task.extracxml.po;

/**
 * @Author: Yilia
 * @Date: 2024/2/4 0004 17:16
 */

import lombok.Data;

@Data
public class Identifier {
    private String bookID;
    private String directoryNumber;
    private String censusNumber;
    private String recordID;

    public String getBookID() {
        return bookID;
    }

    public void setBookID(String bookID) {
        this.bookID = bookID;
    }

    public String getDirectoryNumber() {
        return directoryNumber;
    }

    public void setDirectoryNumber(String directoryNumber) {
        this.directoryNumber = directoryNumber;
    }

    public String getCensusNumber() {
        return censusNumber;
    }

    public void setCensusNumber(String censusNumber) {
        this.censusNumber = censusNumber;
    }

    public String getRecordID() {
        return recordID;
    }

    public void setRecordID(String recordID) {
        this.recordID = recordID;
    }

    @Override
    public String toString() {
        return "Identifier{" +
                "bookID='" + bookID + '\'' +
                ", directoryNumber='" + directoryNumber + '\'' +
                ", censusNumber='" + censusNumber + '\'' +
                ", recordID='" + recordID + '\'' +
                '}';
    }
}
