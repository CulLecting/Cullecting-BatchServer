package com.hambugi.batchServer.service;

import com.hambugi.batchServer.dto.CulturalEventRow;
import com.hambugi.batchServer.entity.CulturalEvent;
import com.hambugi.batchServer.repository.CulturalEventRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class CulturalEventService {
    private final CulturalEventRepository culturalEventRepository;
    private final EmailSenderService emailSenderService;

    @Value("${openapi.key}")
    private String apiKey;

    private final String BASE_URL = "http://openapi.seoul.go.kr:8088/";
    private RestTemplate restTemplate;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");


    public CulturalEventService(CulturalEventRepository culturalEventRepository, RestTemplate restTemplate, EmailSenderService emailSenderService) {
        this.culturalEventRepository = culturalEventRepository;
        this.restTemplate = restTemplate;
        this.emailSenderService = emailSenderService;
    }

    public List<CulturalEvent> getCulturalEvents() {
        return culturalEventRepository.findAll();
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void fetchAllEventDataParallel() {
        try {
            int totalCount = getTotalCount();
            int pageSize = 1000;
            int pageCount = (int) Math.ceil((double) totalCount / pageSize);

            List<CompletableFuture<List<CulturalEventRow>>> futures = new ArrayList<>();

            for (int i = 0; i < pageCount; i++) {
                int start = i * pageSize + 1;
                int end = Math.min(start + pageSize - 1, totalCount);
                futures.add(fetchPage(start, end));
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            List<CulturalEventRow> rawEvents = futures.stream()
                    .flatMap(f -> f.join().stream())
                    .collect(Collectors.toList());

            // 원하는 조건에 따라 필터링하여 저장할 데이터만 추림
            List<CulturalEvent> filtered = rawEvents.stream()
                    .filter(e -> e.getGuname() != null)
                    .map(raw -> convertToEntity(raw))
                    .toList();

            culturalEventRepository.deleteAllInBatch();
            culturalEventRepository.saveAll(filtered);

            String message = "✅ 배치 완료: 총 " + filtered.size() + "건 저장되었습니다.";
            emailSenderService.send("문화 이벤트 배치 성공", message);

            System.out.println("✅ 병렬 배치 완료: " + filtered.size() + "건 저장");

        } catch (Exception e) {
            System.out.println("배치 실패: " + e.getMessage());
            emailSenderService.send("🚨 배치 실패", e.getMessage());
        }
    }

    private int getTotalCount() {
        try {
            String url = BASE_URL + apiKey + "/json/culturalEventInfo/1/1";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JSONObject root = new JSONObject(response.getBody());
            return root.getJSONObject("culturalEventInfo").getInt("list_total_count");
        } catch (Exception e) {
            System.out.println("totalCount 조회 실패: " + e.getMessage());
            emailSenderService.send("🚨 totalCount 조회 실패", e.getMessage());
            return 0;
        }
    }

    @Async
    public CompletableFuture<List<CulturalEventRow>> fetchPage(int start, int end) {
        String url = BASE_URL + apiKey + "/json/culturalEventInfo/" + start + "/" + end;
        List<CulturalEventRow> results = new ArrayList<>();

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JSONArray rows = new JSONObject(response.getBody())
                    .getJSONObject("culturalEventInfo").getJSONArray("row");

            for (int j = 0; j < rows.length(); j++) {
                JSONObject obj = rows.getJSONObject(j);
                results.add(parseRawEvent(obj));
            }
        } catch (Exception e) {
            System.out.println("병렬 처리 중 에러: " + e.getMessage());
            emailSenderService.send("🚨 병렬 처리 중 에러", e.getMessage());
        }

        return CompletableFuture.completedFuture(results);
    }

    private CulturalEventRow parseRawEvent(JSONObject obj) {
        return CulturalEventRow.builder()
                .title(obj.optString("TITLE"))
                .codename(obj.optString("CODENAME"))
                .guname(obj.optString("GUNAME"))
                .place(obj.optString("PLACE"))
                .fee(obj.optString("USE_FEE"))
                .mainImg(obj.optString("MAIN_IMG"))
                .startDate(obj.optString("STRTDATE"))
                .endDate(obj.optString("END_DATE"))
                .isFree(obj.optString("IS_FREE"))
                .orgName(obj.optString("ORG_NAME"))
                .useTrgt(obj.optString("USE_TRGT"))
                .player(obj.optString("PLAYER"))
                .program(obj.optString("PROGRAM"))
                .etcDesc(obj.optString("ETC_DESC"))
                .orgLink(obj.optString("ORG_LINK"))
                .rgstDate(obj.optString("RGSTDATE"))
                .ticket(obj.optString("TICKET"))
                .themeCode(obj.optString("THEMECODE"))
                .lot(obj.optString("LOT"))
                .lat(obj.optString("LAT"))
                .hmpgAddr(obj.optString("HMPG_ADDR"))
                .date(obj.optString("DATE"))
                .build();
    }

    private CulturalEvent convertToEntity(CulturalEventRow raw) {
        return CulturalEvent.builder()
                .title(raw.getTitle())
                .codename(raw.getCodename())
                .guname(raw.getGuname())
                .place(raw.getPlace())
                .orgName(raw.getOrgName())
                .orgLink(raw.getOrgLink())
                .mainImg(raw.getMainImg())
                .themeCode(raw.getThemeCode())
                .startDate(parseToLocalDate(raw.getStartDate()))
                .endDate(parseToLocalDate(raw.getEndDate()))
                .date(raw.getDate())
                .lot(raw.getLot())
                .lat(raw.getLat())
                .hmpgAddr(raw.getHmpgAddr())
                .isFree("무료".equals(raw.getIsFree()))
                .build();
    }

    private LocalDate parseToLocalDate(String datetime) {
        if (datetime == null || datetime.isBlank()) return null;
        return LocalDate.parse(datetime.trim(), FORMATTER);
    }

}
