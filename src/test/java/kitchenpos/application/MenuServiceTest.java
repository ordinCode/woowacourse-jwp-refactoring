package kitchenpos.application;

import kitchenpos.application.common.MenuFixtureFactory;
import kitchenpos.dto.menu.MenuProductDto;
import kitchenpos.dto.menu.MenuResponse;
import kitchenpos.dto.menu.menuCreateRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@Sql("/delete_all.sql")
class MenuServiceTest extends MenuFixtureFactory {
    @Autowired
    private MenuService menuService;

    @DisplayName("메뉴 생성 기능 테스트")
    @Test
    void create() {
        menuCreateRequest menuCreateRequest = makeMenuCreateRequest("추천메뉴", "양념", 13000);

        MenuResponse menuResponse = menuService.create(menuCreateRequest);

        List<MenuProductDto> savedMenuProducts = menuResponse.getMenuProducts();
        assertAll(
                () -> assertThat(menuResponse.getId()).isNotNull(),
                () -> assertThat(savedMenuProducts.get(0).getId()).isEqualTo(menuResponse.getId())
        );
    }

    @DisplayName("메뉴 생성 - price가 null일 때 예외처리")
    @Test
    void createWhenNullPrice() {
        menuCreateRequest menuCreateRequest = new menuCreateRequest("추천메뉴", null, 1L, new ArrayList<>());

        assertThatThrownBy(() -> menuService.create(menuCreateRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("메뉴 생성 - price가 0 미만일 경우 예외처리")
    @Test
    void createWhenPriceLessZero() {
        menuCreateRequest menuCreateRequest = new menuCreateRequest("추천메뉴", BigDecimal.valueOf(-1), 1L, new ArrayList<>());

        assertThatThrownBy(() -> menuService.create(menuCreateRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("메뉴 생성 - price 구성품 가격의 합보다 큰 경우 예외처리")
    @Test
    void createWhenPriceGraterSum() {
        menuCreateRequest menuCreateRequest = new menuCreateRequest("추천메뉴", BigDecimal.valueOf(90000), 1L, new ArrayList<>());

        assertThatThrownBy(() -> menuService.create(menuCreateRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("메뉴 목록 조회 기능 테스트")
    @Test
    void list() {
        menuService.create(makeMenuCreateRequest("추천메뉴", "양념", 13000));
        menuService.create(makeMenuCreateRequest("추천메뉴", "후라이드", 12000));

        assertThat(menuService.list()).hasSize(2);
    }

    @AfterEach
    void tearDown() {

    }
}
