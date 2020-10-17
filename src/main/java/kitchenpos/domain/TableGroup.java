package kitchenpos.domain;

import kitchenpos.config.BaseEntity;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import java.time.LocalDateTime;
import java.util.List;

@AttributeOverride(name = "id", column = @Column(name = "table_group_id"))
@Entity
public class TableGroup extends BaseEntity {
    @OneToMany(mappedBy = "tableGroup")
    private List<OrderTable> orderTables;
    private LocalDateTime createdDate;

    public TableGroup() {
    }

    public TableGroup(Long id, List<OrderTable> orderTables, LocalDateTime createdDate) {
        this.id = id;
        this.orderTables = orderTables;
        this.createdDate = createdDate;
    }

    public TableGroup(List<OrderTable> orderTables, LocalDateTime createdDate) {
        this(null, orderTables, createdDate);
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public List<OrderTable> getOrderTables() {
        return orderTables;
    }
}
