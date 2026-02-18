package com.cotato.itda.domain.image.service;

import com.cotato.itda.domain.image.dto.response.PresignedUrlResponse;
import com.cotato.itda.domain.image.enums.S3Folder;
import com.cotato.itda.domain.image.exception.code.ImageErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    /**
     * Presigned URL 생성
     */
    public PresignedUrlResponse getPresignedUrl(S3Folder folderName, String fileName) {

        String contentType = getContentType(fileName);
        String key = createS3Key(folderName, fileName);

        // 전체 URL 생성
        String imageUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);

        // S3에 업로드할 요청 정보 설정
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        // presigned 요청 설정 (유효기간 10분)
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(objectRequest)
                .build();

        String presignedUrl = s3Presigner.presignPutObject(presignRequest).url().toString();

        return new PresignedUrlResponse(presignedUrl, imageUrl, contentType);
    }

    /**
     *  S3 객체 키 생성
     */
    private String createS3Key(S3Folder folder, String fileName) {
        return String.format("%s/%s_%s", folder.getValue(), UUID.randomUUID(), fileName);
    }

    /**
     * 확장자 추출 및 content-type 매핑
     */
    private String getContentType(String fileName) {
        if (!fileName.contains(".") || fileName.endsWith(".")) {
            throw new BusinessException(ImageErrorCode.INVALID_FILE_NAME, Map.of("fileName", fileName));
        }

        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        return mapContentType(extension);
    }

    private String mapContentType(String extension) {
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            case "gif" -> "image/gif";
            default -> throw new BusinessException(ImageErrorCode.UNSUPPORTED_FILE_EXTENSION, Map.of("extension", extension));
        };
    }

    /**
     * 이미지 삭제
     */
    public void deleteImage(String imageUrl) {
            String key = extractKeyFromUrl(imageUrl);
            try {
                s3Client.deleteObject(DeleteObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build());
            } catch (S3Exception e) {
                throw new BusinessException(ImageErrorCode.IMAGE_DELETE_FAILED, Map.of("imageUrl", imageUrl));
            }
    }

    /**
     *  S3 URL에서 객체 키 추출
     */
    private String extractKeyFromUrl(String imageUrl) {
        String expectedHost = bucket + ".s3." + region + ".amazonaws.com";
        try {
            // URL이 올바른 버킷 주소를 포함하는지 확인
            if (!imageUrl.contains(expectedHost)) {
                throw new BusinessException(ImageErrorCode.URL_NOT_VALID, Map.of("imageUrl", imageUrl));
            }

            // 객체 키 추출
            String target = ".com/";
            return imageUrl.substring(imageUrl.indexOf(target) + target.length());

        } catch (Exception e) {
            throw new BusinessException(ImageErrorCode.URL_NOT_VALID, Map.of("imageUrl", imageUrl));
        }
    }

    /**
     * S3 객체 존재 여부 검증
     */
    public void validateImageExists(String imageUrl) {
        // null인 경우 검증 통과
        if (imageUrl == null) {
            return;
        }
        String key = extractKeyFromUrl(imageUrl);

        try {
            // S3 버킷 내 실제 객체 존재 여부 확인
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            s3Client.headObject(headObjectRequest);

        } catch (S3Exception e) {
            throw new BusinessException(ImageErrorCode.FILE_NOT_FOUND, Map.of("imageUrl", imageUrl));
        }
    }

    public PresignedGetUrlResponse getPresignedGetUrl(String objectKey) {

        if (objectKey == null || objectKey.isBlank()) {
            throw new BusinessException(ImageErrorCode.URL_NOT_VALID, Map.of("objectKey", objectKey));
        }

        // URL 전체가 들어오는 실수 방지
        if (objectKey.startsWith("http://") || objectKey.startsWith("https://")) {
            throw new BusinessException(ImageErrorCode.URL_NOT_VALID, Map.of("objectKey", objectKey));
        }

        // 경로탈출 방지
        if (objectKey.contains("..") || objectKey.contains("\\") || objectKey.startsWith("/")) {
            throw new BusinessException(ImageErrorCode.URL_NOT_VALID, Map.of("objectKey", objectKey));
        }

        Duration expires = Duration.ofMinutes(10);
        Instant expiresAt = Instant.now().plus(expires);

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
            .bucket(bucket)
            .key(objectKey)
            .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(expires)
            .getObjectRequest(getObjectRequest)
            .build();

        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(presignRequest);

        return new PresignedGetUrlResponse(
            objectKey,
            presigned.url().toString(),
            expires.toSeconds(),
            expiresAt.toString()
        );
    }

    public record PresignedGetUrlResponse(
        String objectKey,
        String url,
        long expiresInSeconds,
        String expiresAt
    ) {}

}
