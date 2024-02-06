package fun.gatsby.sbimgutil.task.extracxml.po;

/**
 * @Author: Yilia
 * @Date: 2024/2/4 0004 17:16
 */

import lombok.Data;

@Data
public class Metadata {
    private Identifier identifier;
    private Title title;
    private Creator creator;
    private Contributor contributor;
    private Edition edition;
    private Publisher publisher;
    private Date date;
    private PhysicalDescription physicalDescription;
    private Description description;
    private Provenance provenance;
    private AncientBookPreservation ancientBookPreservation;
    private Location location;
    private Relation relation;
    private Subject subject;
    private String language;
    private String rights;
    private String type;

    public Metadata() {
        this.identifier = new Identifier();
        this.title = new Title();
        this.creator = new Creator();
        this.contributor = new Contributor();
        this.edition = new Edition();
        this.publisher = new Publisher();
        this.date = new Date();
        this.physicalDescription = new PhysicalDescription();
        this.description = new Description();
        this.provenance = new Provenance();
        this.ancientBookPreservation = new AncientBookPreservation();
        this.location = new Location();
        this.relation = new Relation();
        this.subject = new Subject();
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Identifier identifier) {
        this.identifier = identifier;
    }

    public Title getTitle() {
        return title;
    }

    public void setTitle(Title title) {
        this.title = title;
    }

    public Creator getCreator() {
        return creator;
    }

    public void setCreator(Creator creator) {
        this.creator = creator;
    }

    public Contributor getContributor() {
        return contributor;
    }

    public void setContributor(Contributor contributor) {
        this.contributor = contributor;
    }

    public Edition getEdition() {
        return edition;
    }

    public void setEdition(Edition edition) {
        this.edition = edition;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public PhysicalDescription getPhysicalDescription() {
        return physicalDescription;
    }

    public void setPhysicalDescription(PhysicalDescription physicalDescription) {
        this.physicalDescription = physicalDescription;
    }

    public Provenance getProvenance() {
        return provenance;
    }

    public void setProvenance(Provenance provenance) {
        this.provenance = provenance;
    }

    public AncientBookPreservation getAncientBookPreservation() {
        return ancientBookPreservation;
    }

    public void setAncientBookPreservation(AncientBookPreservation ancientBookPreservation) {
        this.ancientBookPreservation = ancientBookPreservation;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Relation getRelation() {
        return relation;
    }

    public void setRelation(Relation relation) {
        this.relation = relation;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getRights() {
        return rights;
    }

    public void setRights(String rights) {
        this.rights = rights;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Description getDescription() {
        return description;
    }

    public void setDescription(Description description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Metadata{" +
                "identifier=" + identifier +
                ", title=" + title +
                ", creator=" + creator +
                ", contributor=" + contributor +
                ", edition=" + edition +
                ", publisher=" + publisher +
                ", date=" + date +
                ", physicalDescription=" + physicalDescription +
                ", description=" + description +
                ", provenance=" + provenance +
                ", ancientBookPreservation=" + ancientBookPreservation +
                ", location=" + location +
                ", relation=" + relation +
                ", subject=" + subject +
                ", language='" + language + '\'' +
                ", rights='" + rights + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
