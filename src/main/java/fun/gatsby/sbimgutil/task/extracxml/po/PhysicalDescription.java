package fun.gatsby.sbimgutil.task.extracxml.po;

/**
 * @Author: Yilia
 * @Date: 2024/2/4 0004 17:20
 */

import lombok.Data;

@Data
public class PhysicalDescription {
    private String binding;
    private String quantity;
    private String dimension;
    private String chart;
    private String accompanyingMaterial;

    public String getBinding() {
        return binding;
    }

    public void setBinding(String binding) {
        this.binding = binding;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    public String getChart() {
        return chart;
    }

    public void setChart(String chart) {
        this.chart = chart;
    }

    public String getAccompanyingMaterial() {
        return accompanyingMaterial;
    }

    public void setAccompanyingMaterial(String accompanyingMaterial) {
        this.accompanyingMaterial = accompanyingMaterial;
    }

    @Override
    public String toString() {
        return "PhysicalDescription{" +
                "binding='" + binding + '\'' +
                ", quantity='" + quantity + '\'' +
                ", dimension='" + dimension + '\'' +
                ", chart='" + chart + '\'' +
                ", accompanyingMaterial='" + accompanyingMaterial + '\'' +
                '}';
    }
}
