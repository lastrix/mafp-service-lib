package org.lastrix.rest.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.hamcrest.Matchers;
import org.lastrix.rest.Rest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.UnsupportedEncodingException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

@SuppressWarnings("unused")
@SpringBootTest
@AutoConfigureMockMvc
public class ControllerTest extends BaseTest {
    @Autowired
    protected MockMvc mockMvc;

    ///////////////////////////////////////// Sync Helpers /////////////////////////////////////////////////////////////
    protected final <T> T singleResultGet(Class<T> resultClass, String template, Object... urlVars) throws Exception {
        return singleResult(resultClass, get(template, urlVars));
    }

    protected final <T> T singleResultPost(Class<T> resultClass, String template, Object body, Object... urlVars) throws Exception {
        return singleResult(resultClass, withBody(post(template, urlVars), body));
    }

    protected final <T> T singleResultPut(Class<T> resultClass, String template, Object body, Object... urlVars) throws Exception {
        return singleResult(resultClass, withBody(put(template, urlVars), body));
    }

    protected final <T> T singleResultDel(Class<T> resultClass, String template, Object... urlVars) throws Exception {
        return singleResult(resultClass, delete(template, urlVars));
    }

    protected final <T> List<T> listResultGet(Class<T> resultClass, String template, Object... urlVars) throws Exception {
        return listResult(resultClass, get(template, urlVars));
    }

    protected final <T> List<T> listResultPost(Class<T> resultClass, String template, Object body, Object... urlVars) throws Exception {
        return listResult(resultClass, withBody(post(template, urlVars), body));
    }

    protected final <T> List<T> listResultPut(Class<T> resultClass, String template, Object body, Object... urlVars) throws Exception {
        return listResult(resultClass, withBody(put(template, urlVars), body));
    }

    protected final <T> List<T> listResultDel(Class<T> resultClass, String template, Object... urlVars) throws Exception {
        return listResult(resultClass, put(template, urlVars));
    }

    protected final String errorResultGet(String template, Object... urlVars) throws Exception {
        return errorResult(get(template, urlVars));
    }

    protected final String errorResultPost(String template, Object body, Object... urlVars) throws Exception {
        return errorResult(withBody(post(template, urlVars), body));
    }

    protected final String errorResultPut(String template, Object body, Object... urlVars) throws Exception {
        return errorResult(withBody(put(template, urlVars), body));
    }

    protected final String errorResultDel(String template, Object... urlVars) throws Exception {
        return errorResult(put(template, urlVars));
    }

    ///////////////////////////////////////// Sync /////////////////////////////////////////////////////////////////////

    protected final <T> T singleResult(Class<T> resultClass, MockHttpServletRequestBuilder rb) throws Exception {
        var rest = mapResponse(performSuccessRequest(rb), resultClass);
        assertNotNull(rest.getData());
        assertEquals(1, rest.getData().size());
        return rest.getData().get(0);
    }

    protected final <T> List<T> listResult(Class<T> resultClass, MockHttpServletRequestBuilder rb) throws Exception {
        var rest = mapResponse(performSuccessRequest(rb), resultClass);
        assertNotNull(rest.getData());
        return rest.getData();
    }

    protected final String errorResult(MockHttpServletRequestBuilder rb) throws Exception {
        var rest = mapResponse(performErrorRequest(rb), Boolean.class);
        assertTrue(rest.getData() == null || rest.getData().isEmpty());
        var es = rest.getErrors();
        assertNotNull(es);
        assertEquals(1, es.size());
        return es.get(0);
    }

    private MvcResult performSuccessRequest(MockHttpServletRequestBuilder requestBuilder) throws Exception {
        return mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success", Matchers.is(true)))
                .andReturn();
    }

    private MvcResult performErrorRequest(MockHttpServletRequestBuilder requestBuilder) throws Exception {
        return mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.jsonPath("$.success", Matchers.is(false)))
                .andReturn();
    }
    ///////////////////////////////////////// Async Helpers ////////////////////////////////////////////////////////////

    protected final <T> T singleResultGetAsync(Class<T> resultClass, String template, Object... urlVars) throws Exception {
        return singleResultAsync(resultClass, get(template, urlVars));
    }

    protected final <T> T singleResultPostAsync(Class<T> resultClass, String template, Object body, Object... urlVars) throws Exception {
        return singleResultAsync(resultClass, withBody(post(template, urlVars), body));
    }

    protected final <T> T singleResultPutAsync(Class<T> resultClass, String template, Object body, Object... urlVars) throws Exception {
        return singleResultAsync(resultClass, withBody(put(template, urlVars), body));
    }

    protected final <T> T singleResultDelAsync(Class<T> resultClass, String template, Object... urlVars) throws Exception {
        return singleResultAsync(resultClass, delete(template, urlVars));
    }

    protected final <T> List<T> listResultGetAsync(Class<T> resultClass, String template, Object... urlVars) throws Exception {
        return listResultAsync(resultClass, get(template, urlVars));
    }

    protected final <T> List<T> listResultPostAsync(Class<T> resultClass, String template, Object body, Object... urlVars) throws Exception {
        return listResultAsync(resultClass, withBody(post(template, urlVars), body));
    }

    protected final <T> List<T> listResultPutAsync(Class<T> resultClass, String template, Object body, Object... urlVars) throws Exception {
        return listResultAsync(resultClass, withBody(put(template, urlVars), body));
    }

    protected final <T> List<T> listResultDelAsync(Class<T> resultClass, String template, Object... urlVars) throws Exception {
        return listResultAsync(resultClass, put(template, urlVars));
    }


    protected final String errorResultGetAsync(String template, Object... urlVars) throws Exception {
        return errorResultAsync(get(template, urlVars));
    }

    protected final String errorResultPostAsync(String template, Object body, Object... urlVars) throws Exception {
        return errorResultAsync(withBody(post(template, urlVars), body));
    }

    protected final String errorResultPutAsync(String template, Object body, Object... urlVars) throws Exception {
        return errorResultAsync(withBody(put(template, urlVars), body));
    }

    protected final String errorResultDelAsync(String template, Object... urlVars) throws Exception {
        return errorResultAsync(put(template, urlVars));
    }

    ///////////////////////////////////////// Async ////////////////////////////////////////////////////////////////////

    protected final <T> T singleResultAsync(Class<T> resultClass, MockHttpServletRequestBuilder rb) throws Exception {
        var rest = mapResponse(performSuccessRequestAsync(rb), resultClass);
        assertNotNull(rest.getData());
        assertEquals(1, rest.getData().size());
        return rest.getData().get(0);
    }

    protected final <T> List<T> listResultAsync(Class<T> resultClass, MockHttpServletRequestBuilder rb) throws Exception {
        var rest = mapResponse(performSuccessRequestAsync(rb), resultClass);
        assertNotNull(rest.getData());
        return rest.getData();
    }

    protected final String errorResultAsync(MockHttpServletRequestBuilder rb) throws Exception {
        var rest = mapResponse(performErrorRequestAsync(rb), Boolean.class);
        var es = rest.getErrors();
        assertNotNull(es);
        assertEquals(1, es.size());
        return es.get(0);
    }

    private MvcResult performSuccessRequestAsync(MockHttpServletRequestBuilder requestBuilder) throws Exception {
        MvcResult mvcResult = mockMvc.perform(requestBuilder)
                .andExpect(request().asyncStarted())
                .andDo(MockMvcResultHandlers.log())
                .andReturn();

        return mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success", Matchers.is(true)))
                .andReturn();
    }

    private MvcResult performErrorRequestAsync(MockHttpServletRequestBuilder requestBuilder) throws Exception {
        MvcResult mvcResult = mockMvc.perform(requestBuilder)
                .andExpect(request().asyncStarted())
                .andDo(MockMvcResultHandlers.log())
                .andReturn();
        return mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(MockMvcResultMatchers.jsonPath("$.success", Matchers.is(false)))
                .andReturn();
    }

    ///////////////////////////////////////// Helpers //////////////////////////////////////////////////////////////////

    private <T> Rest<T> mapResponse(MvcResult response, Class<T> responseType)
            throws UnsupportedEncodingException, JsonProcessingException {
        return objectMapper.readValue(
                response.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructParametricType(Rest.class, responseType)
        );
    }

    private MockHttpServletRequestBuilder withBody(MockHttpServletRequestBuilder rb, Object body) {
        return rb.content(toJson(body))
                .contentType(MediaType.APPLICATION_JSON);
    }
}
