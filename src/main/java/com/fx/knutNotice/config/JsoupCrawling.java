package com.fx.knutNotice.config;

import com.fx.knutNotice.domain.BoardRepository;
import com.fx.knutNotice.domain.entity.Board;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class JsoupCrawling {

    private final BoardRepository boardRepository;

    /**
     * KNUT static url
     * Please Check Git PR :)
     * 
     * by itstime0809.
     */
    private static String board_url = "https://www.ut.ac.kr/cop/bbs/BBSMSTR_000000000059/selectBoardList.do";

    @Scheduled(fixedDelay = 1000) // 1초마다 실행
    public void crawlingBoard() throws IOException {
        log.info("실행중...");

//        List<Board> boardList = new ArrayList<>();
        Document document = Jsoup.connect(board_url).get();
        Elements contents = document.select("tbody > tr");

        for (Element content : contents) {
            String nttId = content.select("input[type=hidden][name=nttId]").val();
            String boardNumber = content.select("td.problem_number").text();
            String title = content.select("td.left > div > div > form > input[type=submit]").val();
            String dept = content.select("td.problem_name").text();
            LocalDate date = LocalDate.parse(content.select("td.date").text());
            Long boardViews = Long.valueOf(content.select("td.problem_count").text());
            String file = content.select("td.problem_file > div > a > span").text();

            //nttId가 empty인 경우 [공지]로 분류되어 있으므로 제외함
            if (nttId != null && !nttId.isEmpty()) {
                Board board = Board.builder()
                        .nttId(Long.valueOf(nttId))
                        .boardNumber(Long.valueOf(boardNumber))
                        .title(title)
                        .dept(dept)
                        .date(date)
                        .boardViews(boardViews)
                        .file(file)
                        .build();
                boardRepository.save(board);
            }
        }
    }
}
