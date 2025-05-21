package phong.demo.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectAclRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    @Autowired
    private  S3Client s3Client;

    @Autowired
    private S3Presigner s3Presigner;

    @Value("${application.bucket.name}")
    private String bucket;              // 4

    /**
     * the function for uploading the file to amazon
     * @param file
     * @return the url of the image
     * @throws IOException
     */
    public String upload(MultipartFile file) throws IOException {   // 5
        String key = UUID.randomUUID() + "_" + file.getOriginalFilename(); // tao uuid de hinh anh khong bi ghi de

        PutObjectRequest  request= PutObjectRequest.builder().bucket(bucket).key(key).contentType(file.getContentType()).build();

        RequestBody body = RequestBody.fromBytes(file.getBytes());

        s3Client.putObject(request,body);

        return key;
    }

    public String generatePresignedUrl(String key){

        GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucket).key(key).build();

        GetObjectPresignRequest presignedRequest = GetObjectPresignRequest.builder().signatureDuration(Duration.ofMinutes(15)).getObjectRequest(getObjectRequest).build();

        PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(presignedRequest);

        return presignedGetObjectRequest.url().toString();
    }
}

