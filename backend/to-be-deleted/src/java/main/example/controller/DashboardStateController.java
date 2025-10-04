package Main.example.controller;

import generated.jooq.tables.pojos.DashboardState;
import Main.example.service.DashboardStateService;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/dashboard-state")
@Tag(name = "Dashboard State", description = "Dashboard State management API")
public class DashboardStateController extends BaseController<DashboardState, DashboardStateService> {

    public DashboardStateController(DashboardStateService service) {
        super(service);
    }
}
