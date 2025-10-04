package Main.example.controller;

import generated.jooq.tables.pojos.Orders;
import Main.example.service.OrdersService;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Orders management API")
public class OrdersController extends BaseController<Orders, OrdersService> {

    public OrdersController(OrdersService service) {
        super(service);
    }
}
