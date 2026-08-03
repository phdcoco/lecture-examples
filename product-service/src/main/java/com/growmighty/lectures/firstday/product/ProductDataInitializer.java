package com.growmighty.lectures.firstday.product;

import com.growmighty.lectures.firstday.product.application.ProductService;
import com.growmighty.lectures.firstday.product.application.dto.RegisterProductCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static com.growmighty.lectures.firstday.product.domain.Product.register;

@Component
@Profile("!test")
@RequiredArgsConstructor
// 이제 각 서비스마다 자신만의 시드 데이터를 가진다.
public class ProductDataInitializer implements CommandLineRunner {

    private final ProductService productService;

    @Override
    public void run(String... args) {
        register("새틴 슬립 드레스", 89_000, 40, "은은한 광택의 새틴 소재. 결혼식, 파티 등 격식 있는 자리에 어울리는 우아한 실루엣.");
        register("플라워 패턴 미디 원피스", 59_000, 60, "화사한 꽃무늬 봄 나들이용 미디 원피스.");
        register("셔츠형 롱 원피스", 64_000, 50, "단정한 카라의 데일리 셔츠 원피스.");
        register("캐주얼 데님 원피스", 49_000, 70, "부담 없이 입는 데님 소재 원피스.");
        register("발수 코팅 맥 코트", 159_000, 30, "발수 코팅 소재로 비 오는 날 출근길에도 안심되는 미니멀 맥 코트.");
        register("빈티지 워싱 데님 자켓", 79_000, 80, "사계절 데일리로 입기 좋은 워싱 데님 자켓.");
        register("트위드 세미 포멀 자켓", 129_000, 25, "격식 있는 자리와 오피스를 모두 소화하는 트위드 자켓.");
        register("링클프리 와이드 슬랙스", 55_000, 90, "구김 걱정 없는 출근용 와이드 슬랙스.");
        register("오버핏 옥스포드 셔츠", 45_000, 100, "꾸미지 않은 듯 자연스러운 데일리 셔츠.");
        register("린넨 와이드 팬츠", 52_000, 60, "여름 휴가지에서 시원하게 입는 린넨 팬츠.");
        register("시어서커 반팔 셔츠", 39_000, 80, "통풍이 잘 되는 여름 휴양지 셔츠.");
        register("캐시미어 블렌드 니트 가디건", 98_000, 40, "간절기 데일리로 걸치기 좋은 니트 가디건.");
        register("경량 패딩 베스트", 69_000, 50, "쌀쌀한 날 겹쳐 입는 경량 조끼.");
        register("방수 하이킹 바람막이", 89_000, 45, "우천 시에도 든든한 방수 아웃도어 바람막이.");
        register("플리츠 미디 스커트", 47_000, 55, "오피스와 격식 있는 자리에 두루 어울리는 주름 스커트.");
        register("블랙 앵클 첼시 부츠", 119_000, 35, "가을 데일리 룩을 완성하는 첼시 부츠.");
        register("스트랩 플랫 샌들", 42_000, 70, "여름 휴가용 스트랩 샌들.");
        register("울 블렌드 체스터 코트", 219_000, 20, "겨울 격식 코디의 기본이 되는 체스터 코트.");
    }

    private void register(String name, long price, int stock, String desc) {
        productService.register(
            new RegisterProductCommand(1L, name, BigDecimal.valueOf(price), stock, desc));
    }
}
