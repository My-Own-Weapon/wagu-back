package com.chimaera.wagubook.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.chimaera.wagubook.exception.CustomException;
import com.chimaera.wagubook.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3ImageService {
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucketName}")
    private String bucketName;

    // 이미지 업로드
    // upload() -> uploadImage() -> validateImageFileExtension() -> uploadImageToS3() -> S3에 저장된 이미지의 public URL 반환
    public String upload(MultipartFile image) {
        // 요청된 파일이 없는 경우
        if (image.isEmpty() || Objects.isNull(image.getOriginalFilename())) {
            throw new CustomException(ErrorCode.EMPTY_FILE);
        }

        return this.uploadImage(image);
    }

    private String uploadImage(MultipartFile image) {
        this.validateImageFileExtension(image.getOriginalFilename());

        try {
            return this.uploadImageToS3(image);
        } catch (IOException e) {
            
            // 파일에 입출력 문제가 발생하는 경우
            throw new CustomException(ErrorCode.IO_EXCEPTION_ON_IMAGE_UPLOAD);
        }
    }

    // 파일의 확장자가 jpg, jpeg, png, gif 중에 속하는지 검증
    private void validateImageFileExtension(String filename) {
        // 파일에 사용자가 없는 경우
        if (filename.lastIndexOf(".") == -1) {
            throw new CustomException(ErrorCode.NO_FILE_EXTENSION);
        }

        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        
        // 허용된 확장자가 아닌 경우
        if (!Arrays.asList("jpg", "jpeg", "png", "gif").contains(extension)) {
            throw new CustomException(ErrorCode.INVALID_FILE_EXTENSION);
        }
    }

    private String uploadImageToS3(MultipartFile image) throws IOException {
        String originalFilename = image.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        
        // 파일 이름 랜덤 값으로 수정 (파일 이름 충돌 방지 및 정보 보호를 위함)
        // S3 Bucket 내에 파일 이름 중복을 검사한 후, 파일 이름이 중복되지 않을 때까지 UUID 생성
        String s3FileName;

        while (true) {
            s3FileName = UUID.randomUUID().toString().substring(0, 10);

            if (!amazonS3.doesObjectExist(bucketName, s3FileName)) {
                break;
            }
        }

        // 이미지를 InputStream으로 읽고, Meta Data 설정을 위해 Byte 배열로 변환
        InputStream inputStream = image.getInputStream();
        byte[] bytes = IOUtils.toByteArray(inputStream);

        // Meta Data 설정
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("image/" + extension);
        metadata.setContentLength(bytes.length);

        // Byte 배열을 S3에 업로드하기 위해 InputStream으로 다시 변환
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

        // 파일을 S3에 업로드
        try {
            PutObjectRequest putObjectRequest =
                    new PutObjectRequest(bucketName, s3FileName, byteArrayInputStream, metadata)
                            .withCannedAcl(CannedAccessControlList.PublicRead);
            amazonS3.putObject(putObjectRequest);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.FAIL_TO_UPLOAD_IMAGE);
        } finally {
            
            // 실행된 리소스 해제
            byteArrayInputStream.close();
            inputStream.close();
        }

        // S3에 저장된 Image의 public URL 반환
        return amazonS3.getUrl(bucketName, s3FileName).toString();
    }

    // 이미지 삭제
    // 이미지의 public URL을 통해 Key를 얻어와 S3에서 해당 이미지 삭제
    // deleteImageFromS3() -> getKeyFromImageAddress()
    public void deleteImageFromS3(String imageAddress) {
        String key = getKeyFromImageAddress(imageAddress);

        try {
            amazonS3.deleteObject(new DeleteObjectRequest(bucketName, key));
        } catch (Exception e){

            // 파일에 입출력 문제가 발생하는 경우
            throw new CustomException(ErrorCode.IO_EXCEPTION_ON_IMAGE_DELETE);
        }
    }

    private String getKeyFromImageAddress(String imageAddress) {
        try {
            URL url = new URL(imageAddress);
            String decodingKey = URLDecoder.decode(url.getPath(), "UTF-8");
            return decodingKey.substring(1);
        } catch (MalformedURLException | UnsupportedEncodingException e) {

            // 파일에 입출력 문제가 발생하는 경우
            throw new CustomException(ErrorCode.IO_EXCEPTION_ON_IMAGE_DELETE);
        }
    }
}
