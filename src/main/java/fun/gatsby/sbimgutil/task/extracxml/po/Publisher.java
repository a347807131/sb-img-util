package fun.gatsby.sbimgutil.task.extracxml.po;

/**
 * @Author: Yilia
 * @Date: 2024/2/4 0004 17:19
 */

import lombok.Data;

@Data
public class Publisher {
    private String publisher;
    private String placeOfPublication;
    private String publishingMethod;
    private String printer;
    private String placeOfPrinting;
    private String printingMethod;

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getPlaceOfPublication() {
        return placeOfPublication;
    }

    public void setPlaceOfPublication(String placeOfPublication) {
        this.placeOfPublication = placeOfPublication;
    }

    public String getPublishingMethod() {
        return publishingMethod;
    }

    public void setPublishingMethod(String publishingMethod) {
        this.publishingMethod = publishingMethod;
    }

    public String getPrinter() {
        return printer;
    }

    public void setPrinter(String printer) {
        this.printer = printer;
    }

    public String getPlaceOfPrinting() {
        return placeOfPrinting;
    }

    public void setPlaceOfPrinting(String placeOfPrinting) {
        this.placeOfPrinting = placeOfPrinting;
    }

    public String getPrintingMethod() {
        return printingMethod;
    }

    public void setPrintingMethod(String printingMethod) {
        this.printingMethod = printingMethod;
    }

    @Override
    public String toString() {
        return "Publisher{" +
                "publisher='" + publisher + '\'' +
                ", placeOfPublication='" + placeOfPublication + '\'' +
                ", publishingMethod='" + publishingMethod + '\'' +
                ", printer='" + printer + '\'' +
                ", placeOfPrinting='" + placeOfPrinting + '\'' +
                ", printingMethod='" + printingMethod + '\'' +
                '}';
    }
}
