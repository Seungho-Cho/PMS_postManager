package com.mason.api.post.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 포스트에 첨부된 사진 (R2에 저장된 원본/썸네일 URL).
 */
@Entity
@Table(name = "photos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "original_url", nullable = false, length = 1000)
    private String originalUrl;

    @Column(name = "thumbnail_url", nullable = false, length = 1000)
    private String thumbnailUrl;

    /**
     * api가 R2 업로드(원본+썸네일)를 끝낸 후, 그 결과 URL로 생성한다.
     */
    public Photo(Post post, String originalUrl, String thumbnailUrl) {
        this.post = post;
        this.originalUrl = originalUrl;
        this.thumbnailUrl = thumbnailUrl;
    }
}
