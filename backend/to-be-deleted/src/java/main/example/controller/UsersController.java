package Main.example.controller;

import generated.jooq.tables.pojos.Users;
import Main.example.service.UsersService;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Users management API")
public class UsersController extends BaseController<Users, UsersService> {

    public UsersController(UsersService service) {
        super(service);
    }
}
