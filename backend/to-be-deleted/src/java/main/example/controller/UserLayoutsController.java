package Main.example.controller;

import generated.jooq.tables.pojos.UserLayouts;
import Main.example.service.UserLayoutsService;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/user-layouts")
@Tag(name = "User Layouts", description = "User Layouts management API")
public class UserLayoutsController extends BaseController<UserLayouts, UserLayoutsService> {

    public UserLayoutsController(UserLayoutsService service) {
        super(service);
    }
}
