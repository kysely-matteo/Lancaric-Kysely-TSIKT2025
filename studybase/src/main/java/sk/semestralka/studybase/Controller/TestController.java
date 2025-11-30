package sk.semestralka.studybase.Controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping("/test")
    public String test() {
        return "Serus ne";
    }

    @GetMapping("/hello/{name}")
    public String hello(@PathVariable String name) {
        return "Ahoj " + name + "!";
    }
}