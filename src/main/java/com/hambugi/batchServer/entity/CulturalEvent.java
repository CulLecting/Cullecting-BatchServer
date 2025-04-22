package com.hambugi.batchServer.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "culturalevent")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CulturalEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title; //제목
    private String codename; //분류
    private String guname; //구 이름
    private String place; //장소 이름
    private String orgName; //프로그램 제목
    @Column(length = 1000)
    private String orgLink; //프로그램 링크
    @Column(length = 1000)
    private String mainImg; //프로그램 메인 이미지
    private String themeCode; //테마분류
    private LocalDate startDate; //시작날짜
    private LocalDate endDate; //끝나는날짜
    private String date; //기간
    private String lot; //위도
    private String lat; //경도
    @Column(length = 1000)
    private String hmpgAddr; //홈페이지 링크
    private boolean isFree; //유무료 확인


    private LocalDateTime lastUpdated;

    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        this.lastUpdated = LocalDateTime.now();
    }
}
