package com.kusitms.backend.controller;

import static com.kusitms.backend.ApiDocumentUtils.getDocumentRequest;
import static com.kusitms.backend.ApiDocumentUtils.getDocumentResponse;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
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
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
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
  @DisplayName("후기 게시글 상세 조회")
  void getReviewDetail() throws Exception {
    final Long postId = 1L;

    ReviewPostDto.Response response = ReviewPostDto.Response.builder()
        .title("듀얼리 하우스 후기").content("듀얼리 하우스 너무 좋아요!")
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
                    parameterWithName("postId").description("게시글 ID")
                ),
                responseFields(
                    fieldWithPath("status").description("결과 코드"),
                    fieldWithPath("message").description("응답 메세지"),
                    fieldWithPath("data.title").description("후기 게시글 제목"),
                    fieldWithPath("data.content").description("후기 게시글 내용"),
                    fieldWithPath("data.imageUrl").description("대표 이미지 url"),
                    fieldWithPath("data.comments").description("해당 게시글의 댓글")
                )
            )
        );

  }

  @Test
  @DisplayName("후기 게시글 목록 조회")
  void getReviewPostList() throws Exception {
    final Integer page = 1;

    final List<ReviewPostDto.PreviewDto> response = new ArrayList<>();
    final ReviewPostDto.PreviewDto review1 = ReviewPostDto.PreviewDto.builder()
        .title("듀얼리 하우스 후기")
        .previewText("듀얼리 하우스 후기입니다! ...")
        .user("듀얼리 ( 작성자 닉네임 )")
        .imageUrl("image url")
        .build();
    final ReviewPostDto.PreviewDto review2 = ReviewPostDto.PreviewDto.builder()
        .title("속초 오션뷰 하우스 후기입니다.")
        .previewText("바다가 잘보이는 ... ")
        .user("따개비 ( 작성자 닉네임 )")
        .imageUrl("image url")
        .build();
    final ReviewPostDto.PreviewDto review3 = ReviewPostDto.PreviewDto.builder()
        .title("제주도 게스트 하우스")
        .previewText("제주도 게스트 하우스 너무 좋아요! ...")
        .user("돌하르방 ( 작성자 닉네임 )")
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
                    parameterWithName("page").description("페이지 번호")
                ),
                responseFields(
                    fieldWithPath("status").description("결과 코드"),
                    fieldWithPath("message").description("응답 메세지"),
                    fieldWithPath("data.[].title").description("게시글 제목"),
                    fieldWithPath("data.[].previewText").description("게시글 미리보기"),
                    fieldWithPath("data.[].user").description("작성자 닉네임"),
                    fieldWithPath("data.[].imageUrl").description("대표 이미지 url")
                ))
        );

  }


}
