package fun.gatsby.sbimgutil.task.extracxml.po;

/**
 * @Author: Yilia
 * @Date: 2024/2/4 0004 17:55
 */

import lombok.Data;

@Data
public class Structure {
    private String bookID;
    private Integer internalSequenceNumber;
    private String volumeTitle;
    private String volumeName;
    private Integer fileNumber;

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

    public String getVolumeTitle() {
        return volumeTitle;
    }

    public void setVolumeTitle(String volumeTitle) {
        this.volumeTitle = volumeTitle;
    }

    public String getVolumeName() {
        return volumeName;
    }

    public void setVolumeName(String volumeName) {
        this.volumeName = volumeName;
    }

    public Integer getFileNumber() {
        return fileNumber;
    }

    public void setFileNumber(Integer fileNumber) {
        this.fileNumber = fileNumber;
    }

    @Override
    public String toString() {
        return "Structure{" +
                "bookID='" + bookID + '\'' +
                ", internalSequenceNumber=" + internalSequenceNumber +
                ", volumeTitle='" + volumeTitle + '\'' +
                ", volumeName='" + volumeName + '\'' +
                ", fileNumber=" + fileNumber +
                '}';
    }
}
