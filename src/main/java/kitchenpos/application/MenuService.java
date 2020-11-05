package kitchenpos.application;

import kitchenpos.dao.MenuGroupRepository;
import kitchenpos.dao.MenuProductRepository;
import kitchenpos.dao.MenuRepository;
import kitchenpos.dao.ProductRepository;
import kitchenpos.domain.menu.Menu;
import kitchenpos.domain.menu.MenuGroup;
import kitchenpos.domain.menu.MenuProduct;
import kitchenpos.domain.product.Product;
import kitchenpos.domain.product.ProductPrice;
import kitchenpos.dto.menu.MenuProductDto;
import kitchenpos.dto.menu.MenuRequest;
import kitchenpos.dto.menu.MenuResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuService {
    private final MenuRepository menuRepository;
    private final MenuGroupRepository menuGroupRepository;
    private final MenuProductRepository menuProductRepository;
    private final ProductRepository productRepository;

    public MenuService(
            final MenuRepository menuRepository,
            final MenuGroupRepository menuGroupRepository,
            final MenuProductRepository menuProductRepository,
            final ProductRepository productRepository
    ) {
        this.menuRepository = menuRepository;
        this.menuGroupRepository = menuGroupRepository;
        this.menuProductRepository = menuProductRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public MenuResponse create(final MenuRequest menuRequest) {
        MenuGroup menuGroup = menuGroupRepository.findById(menuRequest.getMenuGroupId()).orElseThrow(IllegalArgumentException::new);
        BigDecimal productsPriceSum = calculateProductsPriceSum(menuRequest);
        final Menu savedMenu = menuRepository.save(menuRequest.toMenu(menuGroup, productsPriceSum));

        addMenuProductToMenu(menuRequest, savedMenu);

        return MenuResponse.of(savedMenu);
    }

    private BigDecimal calculateProductsPriceSum(MenuRequest menuRequest) {
        BigDecimal sum = BigDecimal.ZERO;
        final List<MenuProductDto> menuProductDtos = menuRequest.getMenuProducts();
        for (final MenuProductDto menuProductDto : menuProductDtos) {
            final Product product = productRepository.findById(menuProductDto.getProductId())
                    .orElseThrow(IllegalArgumentException::new);
            ProductPrice productPrice = product.getProductPrice();
            sum = sum.add(productPrice.multiply(menuProductDto.getQuantity()));
        }
        return sum;
    }

    private void addMenuProductToMenu(MenuRequest menuRequest, Menu menu) {
        List<Product> products = findAllProductInMenuRequest(menuRequest);

        for (Product product : products) {
            long quantity = menuRequest.getMenuProducts().stream()
                    .filter(menuProduct -> menuProduct.equalsProduct(product))
                    .findAny()
                    .orElseThrow(IllegalArgumentException::new)
                    .getQuantity();
            MenuProduct menuProductToSave = new MenuProduct(menu, product, quantity);
            menu.addMenuProduct(menuProductRepository.save(menuProductToSave));
        }
    }

    private List<Product> findAllProductInMenuRequest(MenuRequest menuRequest) {
        List<Long> productIds = menuRequest.getMenuProducts().stream()
                .mapToLong(MenuProductDto::getProductId)
                .boxed()
                .collect(Collectors.toList());
        List<Product> products = productRepository.findAllById(productIds);

        if (products.size() != productIds.size()) {
            throw new IllegalArgumentException();
        }
        return products;
    }

    public List<MenuResponse> list() {
        return MenuResponse.listOf(menuRepository.findAllFetch());
    }
}
