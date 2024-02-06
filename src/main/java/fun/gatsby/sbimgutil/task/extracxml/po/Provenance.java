package fun.gatsby.sbimgutil.task.extracxml.po;

/**
 * @Author: Yilia
 * @Date: 2024/2/4 0004 17:41
 */

import lombok.Data;

@Data
public class Provenance {
    private String inscriptionWriter;
    private String writerStat;
    private String inscriptionRole;

    public String getInscriptionWriter() {
        return inscriptionWriter;
    }

    public void setInscriptionWriter(String inscriptionWriter) {
        this.inscriptionWriter = inscriptionWriter;
    }

    public String getWriterStat() {
        return writerStat;
    }

    public void setWriterStat(String writerStat) {
        this.writerStat = writerStat;
    }

    public String getInscriptionRole() {
        return inscriptionRole;
    }

    public void setInscriptionRole(String inscriptionRole) {
        this.inscriptionRole = inscriptionRole;
    }

    @Override
    public String toString() {
        return "Provenance{" +
                "inscriptionWriter='" + inscriptionWriter + '\'' +
                ", writerStat='" + writerStat + '\'' +
                ", inscriptionRole='" + inscriptionRole + '\'' +
                '}';
    }
}
