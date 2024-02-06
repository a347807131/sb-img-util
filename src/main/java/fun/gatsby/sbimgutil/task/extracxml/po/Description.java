package fun.gatsby.sbimgutil.task.extracxml.po;

/**
 * @Author: Yilia
 * @Date: 2024/2/4 0004 17:20
 */

import lombok.Data;

@Data
public class Description {
    private String description;
    private String creatorDescription;
    private String inventoryShortageVolume;
    private String missingCharacters;
    private String seriesDescription;
    private String boundDescription;
    private String frameSize;
    private String paragraphFormat;
    private String aabstract;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatorDescription() {
        return creatorDescription;
    }

    public void setCreatorDescription(String creatorDescription) {
        this.creatorDescription = creatorDescription;
    }

    public String getInventoryShortageVolume() {
        return inventoryShortageVolume;
    }

    public void setInventoryShortageVolume(String inventoryShortageVolume) {
        this.inventoryShortageVolume = inventoryShortageVolume;
    }

    public String getMissingCharacters() {
        return missingCharacters;
    }

    public void setMissingCharacters(String missingCharacters) {
        this.missingCharacters = missingCharacters;
    }

    public String getSeriesDescription() {
        return seriesDescription;
    }

    public void setSeriesDescription(String seriesDescription) {
        this.seriesDescription = seriesDescription;
    }

    public String getBoundDescription() {
        return boundDescription;
    }

    public void setBoundDescription(String boundDescription) {
        this.boundDescription = boundDescription;
    }

    public String getFrameSize() {
        return frameSize;
    }

    public void setFrameSize(String frameSize) {
        this.frameSize = frameSize;
    }

    public String getParagraphFormat() {
        return paragraphFormat;
    }

    public void setParagraphFormat(String paragraphFormat) {
        this.paragraphFormat = paragraphFormat;
    }

    public String getAabstract() {
        return aabstract;
    }

    public void setAabstract(String aabstract) {
        this.aabstract = aabstract;
    }

    @Override
    public String toString() {
        return "Description{" +
                "creatorDescription='" + creatorDescription + '\'' +
                ", inventoryShortageVolume='" + inventoryShortageVolume + '\'' +
                ", missingCharacters='" + missingCharacters + '\'' +
                ", seriesDescription='" + seriesDescription + '\'' +
                ", boundDescription='" + boundDescription + '\'' +
                ", frameSize='" + frameSize + '\'' +
                ", paragraphFormat='" + paragraphFormat + '\'' +
                ", aabstract='" + aabstract + '\'' +
                '}';
    }
}
