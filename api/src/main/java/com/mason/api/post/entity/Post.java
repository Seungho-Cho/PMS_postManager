package com.mason.api.post.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자가 작성하는 포스팅(SNS 동시 발행 단위).
 */
@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "discord_message_id", nullable = false, unique = true)
    private String discordMessageId;

	@Column(name = "author_discord_icon", nullable = false)
	private String authorDiscordIcon;

	@Column(name = "author_discord_nickname", nullable = false)
	private String authorDiscordNickname;

    @Column(name = "author_discord_id", nullable = false)
    private String authorDiscordId;

	@Column(name = "title")
	private String title;

    @Lob
    @Column(name = "caption")
    private String caption;

    @Column(name = "maker_name")
    private String makerName;

    @Column(name = "maker_instagram_id")
    private String makerInstagramId;

    @Column(name = "maker_x_id")
    private String makerXId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostStatus status;

    @Column(name = "posted_at")
    private LocalDateTime postedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Photo> photos = new ArrayList<>();

    /**
     * Discord 사진 업로드 이벤트를 처리하면서 caption 없이 DRAFT 상태로 생성한다.
     * caption/title은 이후 사용자가 웹에서 작성한다.
     */
    public Post(String discordMessageId, String authorDiscordId, String authorDiscordIcon, String authorDiscordNickname) {
        this.discordMessageId = discordMessageId;
        this.authorDiscordId = authorDiscordId;
        this.authorDiscordIcon = authorDiscordIcon;
        this.authorDiscordNickname = authorDiscordNickname;
        this.status = PostStatus.DRAFT;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 카루셀 순서대로 사진을 추가한다.
     */
    public void addPhoto(Photo photo) {
        photos.add(photo);
    }

    /**
     * 사용자가 웹에서 작성하는 title/caption/제작자 정보를 갱신한다.
     */
    public void updateContent(String title, String caption, String makerName, String makerInstagramId, String makerXId) {
        this.title = title;
        this.caption = caption;
        this.makerName = makerName;
        this.makerInstagramId = makerInstagramId;
        this.makerXId = makerXId;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 임시저장(DRAFT) 또는 발행 예약(SCHEDULED)으로 상태를 바꾼다.
     */
    public void changeStatus(PostStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public enum PostStatus {
        DRAFT,      // 작성 중
        SCHEDULED,  // 발행 예약
        POSTED,     // 발행 완료
        FAILED      // 발행 실패
    }

}
