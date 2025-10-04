package Main.example.controller;

import generated.jooq.tables.pojos.OrderUserMap;
import Main.example.service.OrderUserMapService;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/order-user-map")
@Tag(name = "Order User Map", description = "Order User Map management API")
public class OrderUserMapController extends BaseController<OrderUserMap, OrderUserMapService> {

    public OrderUserMapController(OrderUserMapService service) {
        super(service);
    }
}
