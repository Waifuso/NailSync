package phong.demo.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import phong.demo.DTO.JSOn_objectString;
import phong.demo.DTO.JSon_Object;
import phong.demo.Entity.Image;
import phong.demo.Repository.ImageRepository;
import phong.demo.Service.S3Service;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.util.*;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {
    @Autowired
    private  S3Service s3Service;

    @Autowired
    private ImageRepository imageRepository;



    @PostMapping(
            path = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<String> uploadImage(
            @RequestParam("file") MultipartFile file,@RequestParam String imageName
    ) {
        if (file == null || file.isEmpty()) {

            return ResponseEntity.badRequest()
                    .body("Upload failed: no file provided");
        }

        if (imageRepository.existsByImageName(imageName)){
            return ResponseEntity.badRequest().body(" the name has already been used ");
        }

        try {
            // the image URl
            String imageUrl = s3Service.upload(file);
            // the name of the image

            Image image = new Image(imageName,imageUrl);

            imageRepository.save(image);

            return ResponseEntity.ok("Upload successful: " + imageUrl);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upload failed: " + e.getMessage());
        }
    }
    @GetMapping("image/loader/{imageName}")
    public ResponseEntity<?>getImage(@PathVariable String imageName){

        Optional<Image> optionalImage = imageRepository.findByImageName(imageName);


        if (optionalImage.isEmpty()){

            return  ResponseEntity.badRequest().body(" can not find the image");

        }

        String fileKey =  optionalImage.get().getImageURl();

        String presignedUrl = s3Service.generatePresignedUrl(fileKey);


        return ResponseEntity.ok(new JSOn_objectString(presignedUrl));
    }

    @GetMapping("image/loader/getALlNails")
    public ResponseEntity<?>getallNailImage(){

        List<Image> images  = imageRepository.findAll();

        // the list contain the map with the key is a name of the image and the value is the presigned Url
        List<Map<String,String>> NameAndImageList = new ArrayList<>();


        for (Image image:images){

            String name = image.getImageName();

            String url = s3Service.generatePresignedUrl(image.getImageURl());

            // the container of the image name and the url
            Map<String,String> NameAndUrlContainer = new HashMap<>();

            NameAndUrlContainer.put(name,url);

            NameAndImageList.add(NameAndUrlContainer);
        }

        return ResponseEntity.ok(new JSon_Object<List<Map<String,String>>>(NameAndImageList));
    }



    @DeleteMapping("{id}")
    public void DeleteImage(@PathVariable long id){

        imageRepository.deleteById(id);
    }
}

