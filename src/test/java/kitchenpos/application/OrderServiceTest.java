package kitchenpos.application;

import kitchenpos.application.common.TestFixtureFactory;
import kitchenpos.application.common.TestObjectFactory;
import kitchenpos.dao.MenuDao;
import kitchenpos.dao.OrderDao;
import kitchenpos.dao.OrderLineItemDao;
import kitchenpos.domain.Order;
import kitchenpos.domain.OrderLineItem;
import kitchenpos.domain.OrderStatus;
import kitchenpos.domain.OrderTable;
import kitchenpos.dto.order.OrderCreateRequest;
import kitchenpos.dto.order.OrderLineItemDto;
import kitchenpos.dto.order.OrderResponse;
import kitchenpos.dto.order.OrderStatusChangeRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@Sql("/delete_all.sql")
class OrderServiceTest extends TestFixtureFactory {
    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private OrderLineItemDao orderLineItemDao;

    @Autowired
    private MenuDao menuDao;

    @DisplayName("주문 생성 메서드 테스트")
    @Test
    void create() {
        OrderResponse orderResponse = orderService.create(makeOrderCreateRequest());

        Order order = orderDao.findById(orderResponse.getId()).get();
        List<OrderLineItem> orderLineItems = order.getOrderLineItems();
        assertAll(
                () -> assertThat(order.getId()).isNotNull(),
                () -> assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.COOKING),
                () -> assertThat(orderLineItems).hasSize(2),
                () -> assertThat(orderLineItems.get(0).getOrder()).isEqualTo(order)
        );
    }

    @DisplayName("주문 생성 메서드 - 테이블이 빈 테이블인 경우 예외 처리")
    @Test
    void createWhenEmptyTable() {
        OrderTable orderTable = TestObjectFactory.creatOrderTable();
        OrderTable savedOrderTable = orderTableDao.save(orderTable);

        List<OrderLineItemDto> orderLineItemDtos = Arrays.asList(new OrderLineItemDto(1L, 1));
        OrderCreateRequest orderCreateRequest = new OrderCreateRequest(savedOrderTable.getId(), orderLineItemDtos);

        assertThatThrownBy(() -> orderService.create(orderCreateRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("주문 생성 메서드 - OrderLineItems가 빈 경우 예외 처리")
    @Test
    void createWhenEmptyOrderLineItems() {
        OrderTable orderTable = TestObjectFactory.creatOrderTable();
        OrderTable savedOrderTable = orderTableDao.save(orderTable);

        ArrayList<OrderLineItemDto> orderLineItemDtos = new ArrayList<>();
        OrderCreateRequest orderCreateRequest = new OrderCreateRequest(savedOrderTable.getId(), orderLineItemDtos);

        assertThatThrownBy(() -> orderService.create(orderCreateRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("주문 생성 - 주문 요청 시 orderLineItems의 menuId가 존재하지 않는 menu의 아이디일 경우 예외처리")
    @Test
    void createWhenIllegalMenuId() {
        OrderTable orderTable = TestObjectFactory.creatOrderTable();
        OrderTable savedOrderTable = orderTableDao.save(orderTable);

        ArrayList<OrderLineItemDto> orderLineItemDtos = new ArrayList<>();
        OrderCreateRequest orderCreateRequest = new OrderCreateRequest(savedOrderTable.getId(), orderLineItemDtos);

        assertThatThrownBy(() -> orderService.create(orderCreateRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("주문 목록 조회 메서드 테스트")
    @Test
    void list() {
        orderService.create(makeOrderCreateRequest());
        orderService.create(makeOrderCreateRequest());

        List<OrderResponse> list = orderService.list();
        OrderResponse orderResponse = list.get(0);
        assertAll(
                () -> assertThat(list).hasSize(2),
                () -> assertThat(orderResponse.getId()).isNotNull(),
                () -> assertThat(orderResponse.getOrderLineItems()).hasSize(2)
        );
    }

    @DisplayName("주문 상태 변경 메서드 테스트")
    @Test
    void changeOrderStatus() {
        OrderResponse savedOrder = orderService.create(makeOrderCreateRequest());
        OrderStatusChangeRequest orderStatusChangeRequest = new OrderStatusChangeRequest(OrderStatus.MEAL);

        OrderResponse changedOrder = orderService.changeOrderStatus(savedOrder.getId(), orderStatusChangeRequest);

        assertThat(changedOrder.getOrderStatus()).isEqualTo(OrderStatus.MEAL);
    }

    @DisplayName("주문 상태 변경 - 이미 완료 상태인 경우 예외 처리")
    @Test
    void changeOrderStatusWhenCompletion() {
        OrderResponse savedOrder = orderService.create(makeOrderCreateRequest());
        OrderStatusChangeRequest orderStatusChangeRequest = new OrderStatusChangeRequest(OrderStatus.COMPLETION);

        orderService.changeOrderStatus(savedOrder.getId(), orderStatusChangeRequest);

        assertThatThrownBy(() -> orderService.changeOrderStatus(savedOrder.getId(), orderStatusChangeRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @AfterEach
    void tearDown() {
        orderLineItemDao.deleteAll();
        orderDao.deleteAll();
        orderTableDao.deleteAll();
        menuDao.deleteAll();
        menuGroupDao.deleteAll();
        productDao.deleteAll();
    }
}
