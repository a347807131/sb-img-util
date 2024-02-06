package fun.gatsby.sbimgutil.task.extracxml.po;

/**
 * @Author: Yilia
 * @Date: 2024/2/4 0004 17:42
 */

import lombok.Data;

@Data
public class AncientBookPreservation {
    private String culturalRelicsLevel;
    private String damageLevel;

    public String getCulturalRelicsLevel() {
        return culturalRelicsLevel;
    }

    public void setCulturalRelicsLevel(String culturalRelicsLevel) {
        this.culturalRelicsLevel = culturalRelicsLevel;
    }

    public String getDamageLevel() {
        return damageLevel;
    }

    public void setDamageLevel(String damageLevel) {
        this.damageLevel = damageLevel;
    }

    @Override
    public String toString() {
        return "AncientBookPreservation{" +
                "culturalRelicsLevel='" + culturalRelicsLevel + '\'' +
                ", damageLevel='" + damageLevel + '\'' +
                '}';
    }
}
