package com.kusitms.backend.controller;

import static com.kusitms.backend.ApiDocumentUtils.getDocumentRequest;
import static com.kusitms.backend.ApiDocumentUtils.getDocumentResponse;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kusitms.backend.config.JwtAccessDeniedHandler;
import com.kusitms.backend.config.JwtAuthenticationEntryPoint;
import com.kusitms.backend.config.JwtSecurityConfig;
import com.kusitms.backend.config.TokenProvider;
import com.kusitms.backend.dto.ReviewPostDto;
import com.kusitms.backend.repository.PostRepository;
import com.kusitms.backend.service.ReviewService;
import com.kusitms.backend.util.RedisClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

@ActiveProfiles("test")
@ExtendWith(RestDocumentationExtension.class)
@WebMvcTest(ReviewController.class)
@MockBean(JpaMetamodelMappingContext.class)
class ReviewControllerTest {

  final String email = "test@test.com";
  @Autowired
  MockMvc mockMvc;
  @MockBean
  PostRepository postRepository;
  @MockBean
  ReviewService reviewService;
  @MockBean
  TokenProvider tokenProvider;
  @MockBean
  RedisClient redisClient;
  @Autowired
  ObjectMapper objectMapper;
  @MockBean
  JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
  @MockBean
  JwtAccessDeniedHandler jwtAccessDeniedHandler;
  @MockBean
  JwtSecurityConfig jwtSecurityConfig;
  @MockBean
  private Authentication authentication;
  @MockBean
  private SecurityContext securityContext;

  @BeforeEach
  void setUp(WebApplicationContext webApplicationContext,
      RestDocumentationContextProvider restDocumentationContextProvider) {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
        .addFilters(new CharacterEncodingFilter("UTF-8", true))
        .apply(documentationConfiguration(restDocumentationContextProvider)
            .operationPreprocessors()
            .withRequestDefaults(prettyPrint())
            .withResponseDefaults(prettyPrint()))
        .build();
  }

  @Test
  @DisplayName("?????? ????????? ??????")
  void create() throws Exception {
    final ReviewPostDto.Request request = ReviewPostDto.Request.builder()
        .title("????????? ????????? ??????")
        .content("????????? ????????? ?????? ?????????!")
        .imageUrls(List.of(
            "image1.address",
            "image2.address"))
        .build();

    final Long response = 1L;

    when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);
    when(SecurityContextHolder.getContext().getAuthentication().getName()).thenReturn(email);

    given(reviewService.create(email, request)).willReturn(response);

    String json = objectMapper.writeValueAsString(request);

    ResultActions resultActions = mockMvc.perform(
        RestDocumentationRequestBuilders
            .post("/api/review")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
            .characterEncoding("utf-8")
            .accept(MediaType.APPLICATION_JSON)
    );
    resultActions.andExpect(status().isOk())
        .andDo(print())
        .andDo(
            document("create-review", getDocumentRequest(), getDocumentResponse(),
                requestFields(
                    fieldWithPath("title").description("?????? ????????? ??????"),
                    fieldWithPath("content").description("?????? ????????? ??????"),
                    fieldWithPath("imageUrls").description("?????? ????????? ?????? ?????? ?????? ????????? ( ?????? 5??? )")
                ),
                responseFields(
                    fieldWithPath("status").description("?????? ??????"),
                    fieldWithPath("message").description("?????? ?????????"),
                    fieldWithPath("data").description("?????? ????????? ID")
                )
            )
        );
  }


  @Test
  @DisplayName("?????? ????????? ?????? ??????")
  void getReviewDetail() throws Exception {
    final Long postId = 1L;

    ReviewPostDto.Response response = ReviewPostDto.Response.builder()
        .title("????????? ????????? ??????").content("????????? ????????? ?????? ?????????!")
        .imageUrl("imageUrl1").comments(Set.of()).build();

    given(reviewService.getReviewDetail(postId)).willReturn(response);

    ResultActions resultActions = mockMvc.perform(
        RestDocumentationRequestBuilders
            .get("/api/review/{postId}", postId)
    );

    resultActions.andExpect(status().isOk())
        .andDo(print())
        .andDo(
            document("review-detail", getDocumentRequest(), getDocumentResponse(),
                pathParameters(
                    parameterWithName("postId").description("????????? ID")
                ),
                responseFields(
                    fieldWithPath("status").description("?????? ??????"),
                    fieldWithPath("message").description("?????? ?????????"),
                    fieldWithPath("data.title").description("?????? ????????? ??????"),
                    fieldWithPath("data.content").description("?????? ????????? ??????"),
                    fieldWithPath("data.imageUrl").description("?????? ????????? url"),
                    fieldWithPath("data.comments").description("?????? ???????????? ??????")
                )
            )
        );

  }

  @Test
  @DisplayName("?????? ????????? ?????? ??????")
  void getReviewPostList() throws Exception {
    final Integer page = 1;

    final List<ReviewPostDto.PreviewDto> response = new ArrayList<>();
    final ReviewPostDto.PreviewDto review1 = ReviewPostDto.PreviewDto.builder()
        .title("????????? ????????? ??????")
        .previewText("????????? ????????? ???????????????! ...")
        .user("????????? ( ????????? ????????? )")
        .imageUrl("image url")
        .build();
    final ReviewPostDto.PreviewDto review2 = ReviewPostDto.PreviewDto.builder()
        .title("?????? ????????? ????????? ???????????????.")
        .previewText("????????? ???????????? ... ")
        .user("????????? ( ????????? ????????? )")
        .imageUrl("image url")
        .build();
    final ReviewPostDto.PreviewDto review3 = ReviewPostDto.PreviewDto.builder()
        .title("????????? ????????? ?????????")
        .previewText("????????? ????????? ????????? ?????? ?????????! ...")
        .user("???????????? ( ????????? ????????? )")
        .imageUrl("image url")
        .build();
    response.add(review1);
    response.add(review2);
    response.add(review3);

    given(reviewService.getReviewPostList(PageRequest.of(page - 1, 8))).willReturn(response);

    ResultActions resultActions = mockMvc.perform(
        RestDocumentationRequestBuilders
            .get("/api/review/list?page={page}", page)
    );
    resultActions.andExpect(status().isOk())
        .andDo(print())
        .andDo(
            document("review-list", getDocumentRequest(), getDocumentResponse(),
                requestParameters(
                    parameterWithName("page").description("????????? ??????")
                ),
                responseFields(
                    fieldWithPath("status").description("?????? ??????"),
                    fieldWithPath("message").description("?????? ?????????"),
                    fieldWithPath("data.[].title").description("????????? ??????"),
                    fieldWithPath("data.[].previewText").description("????????? ????????????"),
                    fieldWithPath("data.[].user").description("????????? ?????????"),
                    fieldWithPath("data.[].imageUrl").description("?????? ????????? url")
                ))
        );

  }
}

