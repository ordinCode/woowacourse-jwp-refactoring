package kitchenpos.application;

import kitchenpos.application.common.TestObjectFactory;
import kitchenpos.dao.OrderDao;
import kitchenpos.domain.Order;
import kitchenpos.domain.OrderStatus;
import kitchenpos.domain.OrderTable;
import kitchenpos.dto.tablegroup.OrderTableDto;
import kitchenpos.dto.tablegroup.TableGroupingRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
@Sql("/delete_all.sql")
class TableServiceTest {
    @Autowired
    private TableService tableService;

    @Autowired
    private TableGroupService tableGroupService;

    @Autowired
    private OrderDao orderDao;

    @DisplayName("테이블 생성 메서드 테스트")
    @Test
    void create() {
        OrderTable orderTable = TestObjectFactory.creatOrderTable();

        OrderTable savedOrderTable = tableService.create(orderTable);

        assertThat(savedOrderTable.getId()).isNotNull();
    }

    @DisplayName("테이블 목록 조회 기능 테스트")
    @Test
    void list() {
        tableService.create(TestObjectFactory.creatOrderTable());
        tableService.create(TestObjectFactory.creatOrderTable());

        List<OrderTable> tables = tableService.list();

        assertThat(tables).hasSize(2);
    }

    @DisplayName("테이블의 empty 상태를 변경하는 기능 테스트")
    @Test
    void changeEmpty() {
        OrderTable savedOrderTable = tableService.create(TestObjectFactory.creatOrderTable());
        OrderTable changeEmptyOrderTable = TestObjectFactory.createChangeEmptyOrderTable(false);

        OrderTable changedOrderTable = tableService.changeEmpty(savedOrderTable.getId(), changeEmptyOrderTable);

        assertAll(
                () -> assertThat(changedOrderTable.getId()).isEqualTo(savedOrderTable.getId()),
                () -> assertThat(changedOrderTable.isEmpty()).isEqualTo(false)
        );
    }

    @DisplayName("테이블의 empty 상태 변경 - 존재하지 않는 아이디를 입력받은 경우 예외 처리")
    @Test
    void changeEmptyWithNotFoundOrderTable() {
        OrderTable changeEmptyOrderTable = TestObjectFactory.createChangeEmptyOrderTable(false);

        assertThatThrownBy(() -> tableService.changeEmpty(100L, changeEmptyOrderTable))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("테이블의 empty 상태 변경 - 단체 테이블에 등록되어 있는 경우 예외 처리")
    @Test
    void changeEmptyWithRegisteredGroupTable() {
        OrderTable savedOrderTable1 = tableService.create(new OrderTable(0, true));
        OrderTable savedOrderTable2 = tableService.create(new OrderTable(0, true));
        OrderTable changeEmptyOrderTableDto = TestObjectFactory.createChangeEmptyOrderTable(false);

        TableGroupingRequest groupingRequest = new TableGroupingRequest(
                Arrays.asList(new OrderTableDto(savedOrderTable1.getId()), new OrderTableDto(savedOrderTable2.getId()))
        );
        tableGroupService.create(groupingRequest);

        assertThatThrownBy(() -> tableService.changeEmpty(savedOrderTable1.getId(), changeEmptyOrderTableDto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("테이블의 empty 상태 변경 - 주문 상태가 COOKING 혹은 MEAL 인 경우 예외 처리")
    @ParameterizedTest
    @CsvSource({"COOKING", "MEAL"})
    void changeEmptyWhenCooking(OrderStatus orderStatus) {
        OrderTable savedOrderTable = tableService.create(TestObjectFactory.creatOrderTable());

        Order order = TestObjectFactory.createOrder(savedOrderTable, orderStatus, new ArrayList<>());
        orderDao.save(order);

        OrderTable changeEmptyOrderTable = TestObjectFactory.createChangeEmptyOrderTable(false);

        assertThatThrownBy(() -> tableService.changeEmpty(savedOrderTable.getId(), changeEmptyOrderTable))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("테이블에 방문한 손님 수를 변경하는 메서드 테스트")
    @Test
    void changeNumberOfGuests() {
        OrderTable orderTable = TestObjectFactory.creatOrderTable();
        orderTable.setEmpty(false);
        OrderTable savedOrderTable = tableService.create(orderTable);

        OrderTable changedOrderTable =
                tableService.changeNumberOfGuests(savedOrderTable.getId(), TestObjectFactory.createChangeNumberOfGuestsDto(4));

        assertAll(
                () -> assertThat(changedOrderTable.getId()).isEqualTo(savedOrderTable.getId()),
                () -> assertThat(changedOrderTable.getNumberOfGuests()).isEqualTo(4)
        );
    }

    @DisplayName("테이블에 방문한 손님 수를 변경 - 빈 테이블인 경우 예외 처리")
    @Test
    void changeNumberOfGuestsWithEmptyTable() {
        OrderTable orderTable = TestObjectFactory.creatOrderTable();
        orderTable.setEmpty(true);
        OrderTable savedOrderTable = tableService.create(orderTable);

        OrderTable changeNumberOfGuestsDto = TestObjectFactory.createChangeNumberOfGuestsDto(4);

        assertThatThrownBy(() -> tableService.changeNumberOfGuests(savedOrderTable.getId(), changeNumberOfGuestsDto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("테이블에 방문한 손님 수를 변경 - 입력하려는 숫자가 0보다 작은 경우 예외 처리")
    @Test
    void changeNumberOfGuestsWithLessZeroGuests() {
        OrderTable orderTable = TestObjectFactory.creatOrderTable();
        orderTable.setEmpty(false);
        OrderTable savedOrderTable = tableService.create(orderTable);

        OrderTable changeNumberOfGuestsDto = TestObjectFactory.createChangeNumberOfGuestsDto(-1);

        assertThatThrownBy(() -> tableService.changeNumberOfGuests(savedOrderTable.getId(), changeNumberOfGuestsDto))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
