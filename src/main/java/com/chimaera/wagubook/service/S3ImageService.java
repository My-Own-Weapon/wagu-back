package com.chimaera.wagubook.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.chimaera.wagubook.exception.CustomException;
import com.chimaera.wagubook.exception.ErrorCode;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;

@Component
@RequiredArgsConstructor
public class S3ImageService {
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucketName}")
    private String bucketName;

    /* 전체적인 이미지 업로드 프로세스 */
    // 이미지 리사이징 -> BufferedImage를 통해 S3 업로드
    // 수정 시, S3에 있는 이미지를 삭제하고, 다시 이미지 리사이징 후, S3에 업로드한다.

    /* 이미지 리사이징 (원본 비율 유지) */
    public BufferedImage resizeImageWithAspectRatio(MultipartFile image, int maxWidth, int maxHeight) throws IOException {
        BufferedImage originalImage = ImageIO.read(image.getInputStream());
        originalImage = correctImageOrientation(originalImage, image);

        int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();

        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        if (originalWidth <= maxWidth && originalHeight <= maxHeight) {
            return originalImage;
        }

        double aspectRatio = (double) originalWidth / originalHeight;

        int newWidth = maxWidth;
        int newHeight = (int) (maxWidth / aspectRatio);

        if (newHeight > maxHeight) {
            newHeight = maxHeight;
            newWidth = (int) (maxHeight * aspectRatio);
        }

        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g.dispose();

        return resizedImage;
    }

    // EXIF 데이터로부터 이미지 방향을 확인하여 올바른 방향으로 회전
    private BufferedImage correctImageOrientation(BufferedImage image, MultipartFile file) throws IOException {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file.getInputStream());
            ExifIFD0Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);

            if (directory != null && directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
                int orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
                int angle = 0;

                switch (orientation) {
                    case 6 -> angle = 90;
                    case 3 -> angle = 180;
                    case 8 -> angle = 270;
                }

                if (angle != 0) {
                    return rotateImage(image, angle);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    }

    // 이미지를 주어진 각도로 회전
    private BufferedImage rotateImage(BufferedImage img, int angle) {
        double rads = Math.toRadians(angle);
        double sin = Math.abs(Math.sin(rads));
        double cos = Math.abs(Math.cos(rads));
        int w = img.getWidth();
        int h = img.getHeight();
        int newWidth = (int) Math.floor(w * cos + h * sin);
        int newHeight = (int) Math.floor(h * cos + w * sin);

        BufferedImage rotated = new BufferedImage(newWidth, newHeight, img.getType());
        AffineTransform at = new AffineTransform();
        at.translate(newWidth / 2.0, newHeight / 2.0);
        at.rotate(rads, 0, 0);
        at.translate(-w / 2.0, -h / 2.0);

        AffineTransformOp rotateOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        rotateOp.filter(img, rotated);

        return rotated;
    }

    /* 이미지 업로드 */
    // uploadImage() -> validateImageFileExtension() -> uploadImageToS3() -> S3에 저장된 이미지의 public URL 반환
    public String uploadImage(BufferedImage resizedImage, String originalFilename) {
        validateImageFileExtension(originalFilename);

        try {
            return uploadImageToS3(resizedImage, originalFilename);
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

    private String uploadImageToS3(BufferedImage resizedImage, String originalFilename) throws IOException {
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
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, extension, os);
        byte[] bytes = os.toByteArray();

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
        }

        // S3에 저장된 Image의 public URL 반환
        return amazonS3.getUrl(bucketName, s3FileName).toString();
    }

    /* 이미지 삭제 */
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
