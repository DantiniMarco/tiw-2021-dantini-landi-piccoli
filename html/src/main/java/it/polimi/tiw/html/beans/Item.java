package it.polimi.tiw.html.beans;

public class Item {
    private int idItem;
    private String name;
    private String image;
    private String description;

    /***
     * @author Alfredo Landi
     * Default constructor for this bean
     */
    public Item(){
        super();
    }

    /**
     * @author Alfredo Landi
     * Constructor of item without id attribute in the case that database will compute item id
     * @param name of item
     * @param image of item
     * @param description of item
     */
    public Item(String name, String image, String description){
        this.name=name;
        this.image=image;
        this.description=description;
    }

    public int getIdItem() {
        return idItem;
    }

    public void setIdItem(int idItem) {
        this.idItem = idItem;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
