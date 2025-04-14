package com.hambugi.batchServer.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CulturalEventRow {
    private String title;
    private String codename;
    private String guname;
    private String place;
    private String fee;
    private String mainImg;
    private String startDate;
    private String endDate;
    private String isFree;
    private String orgName;
    private String useTrgt;
    private String player;
    private String program;
    private String etcDesc;
    private String orgLink;
    private String rgstDate;
    private String ticket;
    private String themeCode;
    private String lot;
    private String lat;
    private String hmpgAddr;
    private String date;
}
