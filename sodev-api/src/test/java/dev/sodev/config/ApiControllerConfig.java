package dev.sodev.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.sodev.SodevApiApplication;
import dev.sodev.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest(classes = SodevApiApplication.class)
@AutoConfigureMockMvc(addFilters = false)
public class ApiControllerConfig {

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected MemberService memberService;

}
