package com.mason.api.r2;

import java.net.URI;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * Cloudflare R2(S3 호환) 업로드 클라이언트. 호출자가 매번 자격증명을 넘겨 만들기 때문에 상태를 갖지 않는다.
 */
public class R2Client {

    private final S3Client s3Client;
    private final String bucket;
    private final String publicBaseUrl;

    public R2Client(String endpoint, String accessKey, String secretKey, String bucket, String publicBaseUrl) {
        this.bucket = bucket;
        this.publicBaseUrl = publicBaseUrl;
        this.s3Client = S3Client.builder()
            .endpointOverride(URI.create(endpoint))
            .region(Region.of("auto"))
            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
            .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
            .build();
    }

    /**
     * 객체를 업로드하고 공개 URL을 반환한다.
     */
    public String upload(String key, byte[] content, String contentType) {
        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build(),
            RequestBody.fromBytes(content)
        );
        return publicBaseUrl + "/" + key;
    }
}