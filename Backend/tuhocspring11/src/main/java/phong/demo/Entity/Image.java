package phong.demo.Entity;

import jakarta.persistence.*;

@Entity
@Table(name =  "image")
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private  String imageName;

    private String imageURl;

    public Image() {
    }

    public Image(String imageName, String imageURl) {
        this.imageName = imageName;
        this.imageURl = imageURl;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        imageName = imageName;
    }

    public String getImageURl() {
        return imageURl;
    }

    public void setImageURl(String imageURl) {
        imageURl = imageURl;
    }
}
