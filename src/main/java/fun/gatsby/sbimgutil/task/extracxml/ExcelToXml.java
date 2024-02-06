package fun.gatsby.sbimgutil.task.extracxml;

import fun.gatsby.sbimgutil.task.extracxml.po.VolumeBook;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import fun.gatsby.sbimgutil.task.extracxml.po.*;


/**
 * 最终简化版本
 * @Author: Yilia
 * @Date: 2024/2/4 0004 16:50
 */

public class ExcelToXml {
    public static void main(String[] args) {
        String filePath = "C:\\Users\\Gatsby\\OneDrive - bupt.edu.cn\\桌面\\文献整理登记表.xls";
        // 单本书 单本或主从结构的一部书，内容存在一个xls文件的5个sheet中
        try {
            changeSingleBook(filePath, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void changeSingleBook(String filePath, Boolean isCong) throws IOException {
        // 读取Excel文件写入xml文件
        writeXmlInfo(readExcelInfo(filePath), isCong);
    }

    public static VolumeBook readExcelInfo(String filePath) throws IOException {
        VolumeBook volumeBook = new VolumeBook();
        FileInputStream fileInputStream = new FileInputStream(filePath);
        Workbook workbook = new HSSFWorkbook(fileInputStream);
        // 文献整理登记表
        volumeBook.setMetadata(readSheet(workbook, 1, Metadata::new));
        // 结构数据表
        volumeBook.setStructure(readSheet(workbook, 2, Structure::new));
        // 卷目篇名数据表
        volumeBook.setCatalog(readSheet(workbook, 3, Catalog::new));
        return volumeBook;
    }

    /**
     * 读取sheet表
     * @param workbook
     * @param sheetNumber
     * @param objectSupplier
     * @param <T>
     * @return
     */
    public static <T> List<T> readSheet(Workbook workbook, int sheetNumber, Supplier<T> objectSupplier) {
        List<T> resultList = new ArrayList<>();
        Sheet sheet = workbook.getSheetAt(sheetNumber);
        Row headerRow = sheet.getRow(0);

        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row dataRow = sheet.getRow(rowIndex);
            if (dataRow != null) {
                T object = objectSupplier.get();
                boolean isEmpty = true;

                for (int columnIndex = 0; columnIndex < headerRow.getLastCellNum(); columnIndex++) {
                    Cell headerCell = headerRow.getCell(columnIndex);
                    Cell dataCell = dataRow.getCell(columnIndex);

                    String headerValue = (headerCell != null && headerCell.getCellType() == CellType.STRING) ? headerCell.getStringCellValue() : "";
                    String cellValue = getCellValueAsString(dataCell);
                    if (!StringUtils.isEmpty(cellValue)) {
                        // 根据不同的对象类型，调用对应的方法添加数据
                        if (object instanceof Metadata) {
                            addMetadata((Metadata) object, headerValue, cellValue);
                        } else if (object instanceof Structure) {
                            addStructure((Structure) object, headerValue, cellValue);
                        } else if (object instanceof Catalog) {
                            addCatalog((Catalog) object, headerValue, cellValue);
                        }
                        isEmpty = false;
                    }
                }

                if (!isEmpty) {
                    resultList.add(object);
                }
            }
        }

        return resultList;
    }

    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return Double.toString(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case BLANK:
                return "";
            default:
                return "";
        }
    }

    public static void addMetadata(Metadata metadata, String headerValue, String cellValue) {
        switch (headerValue) {
            case "加工记录标识号":
                metadata.getIdentifier().setBookID(cellValue);
                break;
            case "国家珍贵古籍名录号":
                metadata.getIdentifier().setDirectoryNumber(cellValue);
                break;
            case "古籍普查登记编号":
                metadata.getIdentifier().setCensusNumber(cellValue);
                break;
            case "书目记录标识号":
                metadata.getIdentifier().setRecordID(cellValue);
                break;
            case "题名":
                metadata.getTitle().setTitle(cellValue);
                break;
            case "并列题名":
                metadata.getTitle().setParallelTitle(cellValue);
                break;
            case "其他题名":
                metadata.getTitle().setOtherTitle(cellValue);
                break;
            case "题名出处":
                metadata.getTitle().setTitleOrigin(cellValue);
                break;
            case "主要责任者":
                metadata.getCreator().setCreator(cellValue);
                break;
            case "责任者说明":
                metadata.getCreator().setStatementOfResponsiblePerson(cellValue);
                break;
            case "著作方式":
                metadata.getCreator().setRole(cellValue);
                break;
            case "其他责任者":
                metadata.getContributor().setContributor(cellValue);
                break;
            case "版本":
                metadata.getEdition().setEdition(cellValue);
                break;
            case "版本类型":
                metadata.getEdition().setEditionType(cellValue);
                break;
            case "版本补配":
                metadata.getEdition().setEditionSupplement(cellValue);
                break;
            case "出版者":
                metadata.getPublisher().setPublisher(cellValue);
                break;
            case "出版地":
                metadata.getPublisher().setPlaceOfPublication(cellValue);
                break;
            case "出版方式":
                metadata.getPublisher().setPublishingMethod(cellValue);
                break;
            case "印刷者":
                metadata.getPublisher().setPrinter(cellValue);
                break;
            case "印刷地":
                metadata.getPublisher().setPlaceOfPrinting(cellValue);
                break;
            case "印刷方式":
                metadata.getPublisher().setPrintingMethod(cellValue);
                break;
            case "公元纪年":
                metadata.getDate().setGregorianCalendar(cellValue);
                break;
            case "年号纪年":
                metadata.getDate().setChineseCalendar(cellValue);
                break;
            case "出版日期":
                metadata.getDate().setIssued(cellValue);
                break;
            case "印刷日期":
                metadata.getDate().setPrinted(cellValue);
                break;
            case "装帧形式":
                metadata.getPhysicalDescription().setBinding(cellValue);
                break;
            case "数量":
                metadata.getPhysicalDescription().setQuantity(cellValue);
                break;
            case "开本尺寸":
                metadata.getPhysicalDescription().setDimension(cellValue);
                break;
            case "图表":
                metadata.getPhysicalDescription().setChart(cellValue);
                break;
            case "附件":
                metadata.getPhysicalDescription().setAccompanyingMaterial(cellValue);
                break;
            case "附注":
                metadata.getDescription().setDescription(cellValue);
                break;
            case "责任者附注":
                metadata.getDescription().setCreatorDescription(cellValue);
                break;
            case "残存附注":
                metadata.getDescription().setInventoryShortageVolume(cellValue);
                break;
            case "缺字附注":
                metadata.getDescription().setMissingCharacters(cellValue);
                break;
            case "丛书附注":
                metadata.getDescription().setSeriesDescription(cellValue);
                break;
            case "合订附注":
                metadata.getDescription().setBoundDescription(cellValue);
                break;
            case "版框尺寸":
                metadata.getDescription().setFrameSize(cellValue);
                break;
            case "版式":
                metadata.getDescription().setParagraphFormat(cellValue);
                break;
            case "提要":
                metadata.getDescription().setAabstract(cellValue);
                break;
            case "批校题跋者":
                metadata.getProvenance().setInscriptionWriter(cellValue);
                break;
            case "批校题跋者说明":
                metadata.getProvenance().setWriterStat(cellValue);
                break;
            case "批校题跋方式":
                metadata.getProvenance().setInscriptionRole(cellValue);
                break;
            case "文物级别":
                metadata.getAncientBookPreservation().setCulturalRelicsLevel(cellValue);
                break;
            case "破损级别":
                metadata.getAncientBookPreservation().setDamageLevel(cellValue);
                break;
            case "收藏单位":
                metadata.getLocation().setCollectionUnit(cellValue);
                break;
            case "索书号":
                metadata.getLocation().setCallNumber(cellValue);
                break;
            case "丛书":
                metadata.getRelation().setSeries(cellValue);
                break;
            case "丛书链接":
                metadata.getRelation().setSeriesLink(cellValue);
                break;
            case "子目":
                metadata.getRelation().setSub_series(cellValue);
                break;
            case "子目链接":
                metadata.getRelation().setSub_seriesLink(cellValue);
                break;
            case "合订书名":
                metadata.getRelation().setBoundWith(cellValue);
                break;
            case "合订链接":
                metadata.getRelation().setBoundWithLink(cellValue);
                break;
            case "主题":
                metadata.getSubject().setSubject(cellValue);
                break;
            case "语种":
                metadata.setLanguage(cellValue);
                break;
            case "权限":
                metadata.setRights(cellValue);
                break;
            case "文献类型":
                metadata.setType(cellValue);
                break;
        }
    }

    public static void addStructure(Structure structure, String headerValue, String cellValue) {
        switch (headerValue) {
            case "加工记录标识号":
                structure.setBookID(cellValue);
                break;
            case "内部序号":
                structure.setInternalSequenceNumber((int) Double.parseDouble(cellValue));
                break;
            case "册名称":
                structure.setVolumeTitle(cellValue);
                break;
            case "册号":
                structure.setVolumeName(cellValue);
                break;
            case "册内文件数":
                structure.setFileNumber((int) Double.parseDouble(cellValue));
                break;
        }
    }

    public static void addCatalog(Catalog catalog, String headerValue, String cellValue) {
        switch (headerValue) {
            case "加工记录标识号":
                catalog.setBookID(cellValue);
                break;
            case "内部序号":
                catalog.setInternalSequenceNumber((int) Double.parseDouble(cellValue));
                break;
            case "层级号":
                catalog.setLevelNumber((int) Double.parseDouble(cellValue));
                break;
            case "卷名篇名":
                catalog.setVolumeTitleAndArticleTitle(cellValue);
                break;
            case "作者":
                catalog.setArticleAuthor(cellValue);
                break;
            case "册号":
                catalog.setVolumeName(cellValue);
                break;
            case "叶码":
                catalog.setPage(cellValue);
                break;
        }
    }

    public static void writeXmlInfo(VolumeBook volumeBook, Boolean isCong) {
        try {
            String bookId = "";
            // 创建根元素 <book>
            Document document = DocumentHelper.createDocument();
            Element bookElement = document.addElement("book");

            // 创建 <metadata> 元素
            Element metadataElement = bookElement.addElement("metadata");
            List<Metadata> metadataList = volumeBook.getMetadata();
            for (int i = 0; i < metadataList.size(); i++) {
                Element seriesInfoElement;
                if (isCong) {
                    // 主从套书
                    if (i == 0) {
                        // 创建 <seriesInfo> 元素
                        seriesInfoElement = metadataElement.addElement("seriesInfo");
                    }else {
                        // 创建 <sub-seriesInfo> 元素
                        seriesInfoElement = metadataElement.addElement("sub-seriesInfo");
                    }
                }else {
                    seriesInfoElement = metadataElement;
                }

                Metadata metadata = metadataList.get(i);
                Identifier identifier = metadata.getIdentifier();
                if (null != identifier) {
                    // 创建 <identifier> 元素及其子元素
                    Element identifierElement = seriesInfoElement.addElement("identifier");
                    if (!StringUtils.isEmpty(identifier.getBookID())) {
                        bookId = identifier.getBookID();
                        identifierElement.addElement("bookID").setText(identifier.getBookID());
                    }
                    addElementIfNotEmpty(identifierElement, "directoryNumber", identifier.getDirectoryNumber());
                    addElementIfNotEmpty(identifierElement, "censusNumber", identifier.getCensusNumber());
                    addElementIfNotEmpty(identifierElement, "recordID", identifier.getRecordID());
                }

                Title title = metadata.getTitle();
                Creator creator = metadata.getCreator();
                if (title != null) {
                    // 创建 <titlesAndAuthors> 元素及其子元素
                    Element titlesAndAuthorsElement = seriesInfoElement.addElement("titlesAndAuthors");
                    Element titleAndAuthorElement = titlesAndAuthorsElement.addElement("titleAndAuthor");

                    addElementIfNotEmpty(titleAndAuthorElement, "title", title.getTitle());

                    String creatorName = creator.getCreator();
                    if (!StringUtils.isEmpty(creatorName)) {
                        Element creator1 = titleAndAuthorElement.addElement("creator");

                        if (creatorName.contains("(") && creatorName.contains(")")) {
                            int openingBracketIndex = creatorName.indexOf("(");
                            int closingBracketIndex = creatorName.indexOf(")");

                            if (openingBracketIndex != -1 && closingBracketIndex != -1 && closingBracketIndex > openingBracketIndex) {
                                // 截取括号内的内容
                                String contentInsideBrackets = creatorName.substring(openingBracketIndex + 1, closingBracketIndex);
                                creator1.addAttribute("statementOfResponsiblePerson", contentInsideBrackets);

                                // 截取完括号后剩下的字符串
                                String remainingString = creatorName.substring(0, openingBracketIndex) + creatorName.substring(closingBracketIndex + 1);
                                creator1.addAttribute("role", remainingString.substring(remainingString.length() - 1));
                                creator1.setText(remainingString.substring(0, remainingString.length() - 1));
                            }
                        }else {
                            creator1.addAttribute("role", creatorName.substring(creatorName.length() - 1));
                            creator1.setText(creatorName.substring(0, creatorName.length() - 1));
                        }
                    }
                    if (!StringUtils.isEmpty(title.getOtherTitle())) {
                        Element otherTitleAndAuthor = titlesAndAuthorsElement.addElement("otherTitleAndAuthor");
                        otherTitleAndAuthor.addElement("otherTitle").setText(title.getOtherTitle());

                        if (null != metadata.getContributor()) {
                            addElementIfNotEmpty(titleAndAuthorElement, "creator", creatorName);
                        }
                    }
                }

                // 创建 <editions> 元素及其子元素
                Edition edition = metadata.getEdition();
                if (null != edition) {
                    Element editionsElement = seriesInfoElement.addElement("editions");
                    addElementIfNotEmpty(editionsElement, "edition", edition.getEdition());
                    addElementIfNotEmpty(editionsElement, "editionType", edition.getEditionType());
                    addElementIfNotEmpty(editionsElement, "editionSupplement", edition.getEditionSupplement());
                }

                // 创建 <publishers> 元素及其子元素
                Publisher publisher = metadata.getPublisher();
                if (null != publisher) {
                    Element publishersElement = seriesInfoElement.addElement("publishers");
                    addElementIfNotEmpty(publishersElement, "publisher", publisher.getPublisher());
                    addElementIfNotEmpty(publishersElement, "placeOfPublication", publisher.getPlaceOfPublication());
                    addElementIfNotEmpty(publishersElement, "publishingMethod", publisher.getPublishingMethod());
                    addElementIfNotEmpty(publishersElement, "printer", publisher.getPrinter());
                    addElementIfNotEmpty(publishersElement, "placeOfPrinting", publisher.getPlaceOfPrinting());
                    addElementIfNotEmpty(publishersElement, "printingMethod", publisher.getPrintingMethod());

                    Date date = metadata.getDate();
                    if (null != date) {
                        Element creator1 = publishersElement.addElement("issued");
                        addAttributeIfNotEmpty(creator1, "GregorianCalendar", date.getGregorianCalendar());
                        addAttributeIfNotEmpty(creator1, "ChineseCalendar", date.getChineseCalendar());
                    }
                }

                // 创建 <physicalDescriptions> 元素及其子元素
                PhysicalDescription physicalDescription = metadata.getPhysicalDescription();
                if (null != physicalDescription) {
                    Element physicalDescriptionsElement = seriesInfoElement.addElement("physicalDescriptions");
                    addElementIfNotEmpty(physicalDescriptionsElement, "binding", physicalDescription.getBinding());
                    addElementIfNotEmpty(physicalDescriptionsElement, "quantity", physicalDescription.getQuantity());
                    addElementIfNotEmpty(physicalDescriptionsElement, "dimension", physicalDescription.getDimension());
                    addElementIfNotEmpty(physicalDescriptionsElement, "chart", physicalDescription.getChart());
                    addElementIfNotEmpty(physicalDescriptionsElement, "accompanyingMaterial", physicalDescription.getAccompanyingMaterial());
                }

                // 创建 <description> 元素及其子元素
                Description description = metadata.getDescription();
                if (null != description) {
                    Element descriptionsElement = seriesInfoElement.addElement("descriptions");
                    addElementIfNotEmpty(descriptionsElement, "description", description.getDescription());
                    addElementIfNotEmpty(descriptionsElement, "creatorDescription", description.getCreatorDescription());
                    addElementIfNotEmpty(descriptionsElement, "inventoryShortageVolume", description.getInventoryShortageVolume());
                    addElementIfNotEmpty(descriptionsElement, "missingCharacters", description.getMissingCharacters());
                    addElementIfNotEmpty(descriptionsElement, "seriesDescription", description.getSeriesDescription());
                    addElementIfNotEmpty(descriptionsElement, "boundDescription", description.getBoundDescription());
                    addElementIfNotEmpty(descriptionsElement, "frameSize", description.getFrameSize());
                    addElementIfNotEmpty(descriptionsElement, "paragraphFormat", description.getParagraphFormat());
                    addElementIfNotEmpty(descriptionsElement, "abstract", description.getAabstract());
                }

                // 创建 <provenances> 元素及其子元素
                Provenance provenance = metadata.getProvenance();
                if (null != provenance) {
                    Element provenancesElement = seriesInfoElement.addElement("provenances");
                    addElementIfNotEmpty(provenancesElement, "inscriptionWriter", provenance.getInscriptionWriter());
                    addElementIfNotEmpty(provenancesElement, "writerStat", provenance.getWriterStat());
                    addElementIfNotEmpty(provenancesElement, "inscriptionRole", provenance.getInscriptionRole());
                }

                // 创建 <ancientBookPreservations> 元素及其子元素
                AncientBookPreservation ancientBookPreservation = metadata.getAncientBookPreservation();
                if (null != ancientBookPreservation) {
                    Element ancientBookPreservationsElement = seriesInfoElement.addElement("ancientBookPreservations");
                    addElementIfNotEmpty(ancientBookPreservationsElement, "culturalRelicsLevel", ancientBookPreservation.getCulturalRelicsLevel());
                    addElementIfNotEmpty(ancientBookPreservationsElement, "damageLevel", ancientBookPreservation.getDamageLevel());
                }

                // 创建 <locations> 元素及其子元素
                Location location = metadata.getLocation();
                if (null != location) {
                    Element locationsElement = seriesInfoElement.addElement("locations");
                    addElementIfNotEmpty(locationsElement, "collectionUnit", location.getCollectionUnit());
                    addElementIfNotEmpty(locationsElement, "callNumber", location.getCallNumber());
                }

                // 创建 <relations> 元素及其子元素
                Relation relation = metadata.getRelation();
                if (null != location) {
                    Element relationsElement = seriesInfoElement.addElement("relations");
                    addElementIfNotEmpty(relationsElement, "series", relation.getSeries());
                    addElementIfNotEmpty(relationsElement, "seriesLink", relation.getSeriesLink());
                    addElementIfNotEmpty(relationsElement, "sub-series", relation.getSub_series());
                    addElementIfNotEmpty(relationsElement, "sub-seriesLink", relation.getSub_seriesLink());
                    addElementIfNotEmpty(relationsElement, "boundWith", relation.getBoundWith());
                    addElementIfNotEmpty(relationsElement, "boundWithLink", relation.getBoundWithLink());
                }

                // 创建 <subjects> 元素及其子元素 暂时按照FDC存储
                Subject subject = metadata.getSubject();
                if (null != subject) {
                    Element subjectsElement = seriesInfoElement.addElement("subjects");
                    addElementIfNotEmpty(subjectsElement, "FDC", subject.getSubject());
                }
                // 创建 <language> 元素
                addElementIfNotEmpty(seriesInfoElement, "language", metadata.getLanguage());
                // 创建 <rights> 元素
                addElementIfNotEmpty(seriesInfoElement, "rights", metadata.getRights());
                // 创建 <type> 元素
                addElementIfNotEmpty(seriesInfoElement, "type", metadata.getType());
            }

            // 创建 <structure> 元素
            Element structureElement = bookElement.addElement("structure");
            List<Structure> structureList = volumeBook.getStructure();
            String structureBookId = structureList.get(0).getBookID();
            Element separateStructureElement = structureElement.addElement("separateStructure");
            separateStructureElement.addAttribute("bookID", structureList.get(0).getBookID());
            for (Structure structure : structureList) {
                if (!structure.getBookID().equals(structureBookId)) {
                    separateStructureElement = structureElement.addElement("separateStructure");
                    structureBookId = structure.getBookID();
                    separateStructureElement.addAttribute("bookID", structureBookId);
                }

                Element creator1 = separateStructureElement.addElement("volume");
                addAttributeIfNotEmpty(creator1, "internalSequenceNumber", String.valueOf(structure.getInternalSequenceNumber()));
                addAttributeIfNotEmpty(creator1, "volumeTitle", structure.getVolumeTitle());
                addAttributeIfNotEmpty(creator1, "volumeName", structure.getVolumeName());
                addAttributeIfNotEmpty(creator1, "fileNumber", String.valueOf(structure.getFileNumber()));
            }

            // 创建 <catalog> 元素
            Element catalogElement = bookElement.addElement("catalog");
            List<Catalog> catalogList = volumeBook.getCatalog();
            String catalogBookId = catalogList.get(0).getBookID();
            Element catalogueElement = catalogElement.addElement("catalogue");
            catalogueElement.addAttribute("bookID", catalogList.get(0).getBookID());
            for (Catalog catalog : catalogList) {
                if (!catalog.getBookID().equals(catalogBookId)) {
                    catalogueElement = catalogElement.addElement("catalogue");
                    catalogBookId = catalog.getBookID();
                    catalogueElement.addAttribute("bookID", catalogBookId);
                }

                Element creator1 = catalogueElement.addElement("catalogItem");
                addAttributeIfNotEmpty(creator1, "internalSequenceNumber", String.valueOf(catalog.getInternalSequenceNumber()));
                addAttributeIfNotEmpty(creator1, "levelNumber", String.valueOf(catalog.getLevelNumber()));
                addAttributeIfNotEmpty(creator1, "volumeTitleAndArticleTitle", catalog.getVolumeTitleAndArticleTitle());
                addAttributeIfNotEmpty(creator1, "articleAuthor", catalog.getArticleAuthor());
                addAttributeIfNotEmpty(creator1, "volumeName", String.valueOf(catalog.getVolumeName()));
                addAttributeIfNotEmpty(creator1, "page", String.valueOf(catalog.getPage()));
            }

            // 创建一个XMLWriter来将XML写入文件
            OutputFormat format = OutputFormat.createPrettyPrint();
            XMLWriter writer = new XMLWriter(new FileWriter("" + bookId + ".xml"), format);

            // 将XML写入文件
            writer.write(document);
            writer.close();
            System.out.println("XML文件已生成。");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addElementIfNotEmpty(Element parentElement, String elementName, String value) {
        if (!StringUtils.isEmpty(value)) {
            parentElement.addElement(elementName).setText(value);
        }
    }

    private static void addAttributeIfNotEmpty(Element parentElement, String attributeName, String value) {
        if (!StringUtils.isEmpty(value)) {
            parentElement.addAttribute(attributeName, value);
        }
    }


}
