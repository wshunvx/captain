package com.netflix.eureka.found;

import java.nio.charset.Charset;
import java.util.Base64;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MockServletContext.class)
@WebAppConfiguration
public class ApplicationTests {

    private MockMvc mockmvc;
	
	private HttpHeaders httpHeaders;
	
	@Autowired
    private WebApplicationContext webApplicationContext;
	
    @Before
    public void setUp() {
    	mockmvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    	httpHeaders = new HttpHeaders();
    	httpHeaders.add("Accept", "application/json, text/plain, */*");
    	httpHeaders.add("Authorization", authorization());
    }
    
    @Test
    public void test() {
    	
    }
    
    public void testLiveness() throws Exception {
        String url = "/eureka/status";
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get(url).headers(httpHeaders);
        mockmvc.perform(builder).andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print());;
    }
	
	public String authorization() {
		String auth = "wshunvx:QRVF68VKEV3B830V";
        byte[] encodedAuth = Base64.getEncoder().encode((auth.getBytes(Charset.forName("US-ASCII")))); // 进行一个加密的处理
        // 在进行授权的头信息内容配置的时候加密的信息一定要与“Basic”之间有一个空格
        return "Basic " + new String(encodedAuth);
	}

}
