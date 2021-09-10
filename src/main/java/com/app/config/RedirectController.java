package com.app.config;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequiredArgsConstructor
public class RedirectController {

    private final Environment environment;

    @GetMapping("/token/auth")
    public ModelAndView redirectToGetAuthCode(ModelMap model) {
        return new ModelAndView("redirect:https://accounts.google.com/o/oauth2/auth?" +
                "scope=https://www.googleapis.com/auth/spreadsheets https://www.googleapis.com/auth/drive" +
                "&client_id=" + environment.getProperty("google-api.clientId") +
                "&redirect_uri=urn:ietf:wg:oauth:2.0:oob" +
                "&response_type=code", model);
    }
}
