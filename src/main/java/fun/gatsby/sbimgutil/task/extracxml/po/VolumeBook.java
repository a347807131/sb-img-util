package fun.gatsby.sbimgutil.task.extracxml.po;

import java.util.List;

/**
 * @Author: Yilia
 * @Date: 2024/2/5 0005 9:08
 */

import lombok.Data;

@Data
public class VolumeBook {
    private List<Metadata> metadata;
    private List<Structure> structure;
    private List<Catalog> catalog;

    public List<Metadata> getMetadata() {
        return metadata;
    }

    public void setMetadata(List<Metadata> metadata) {
        this.metadata = metadata;
    }

    public List<Structure> getStructure() {
        return structure;
    }

    public void setStructure(List<Structure> structure) {
        this.structure = structure;
    }

    public List<Catalog> getCatalog() {
        return catalog;
    }

    public void setCatalog(List<Catalog> catalog) {
        this.catalog = catalog;
    }

    @Override
    public String toString() {
        return "BookCong{" +
                "metadata=" + metadata +
                ", structure=" + structure +
                ", catalog=" + catalog +
                '}';
    }
}
